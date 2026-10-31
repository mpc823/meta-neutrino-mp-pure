##
## TODO: spark rules are only build tested
##

SUMMARY = "Library to abstract STB hardware. Supports Tripledragon, AZbox ME, Fulan Spark boxes as well as generic PC hardware and the Raspberry Pi right now."
DESCRIPTION = "Library to abstract STB hardware."
HOMEPAGE = "https://github.com/neutrino-mp"
SECTION = "libs"
LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://${THISDIR}/libstb-hal/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe"

# hack: make sure we do not try to build on coolstream
COMPATIBLE_MACHINE:coolstream = "none"
#
# this stuff really is machine specific, not CPU specific
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS = "\
	openthreads \
	ffmpeg \
"

# on coolstream, the same is provided by cs-drivers pkg (libcoolstream)
PROVIDES += "virtual/stb-hal-libs"

DEPENDS:append:spark = "tdt-driver libass"
DEPENDS:append:raspberrypi = "virtual/egl"
DEPENDS:append:raspberrypi2 = "virtual/egl"
DEPENDS:append:tripledragon = "directfb triple-sdk"

RDEPENDS:${PN} = "ffmpeg"
RDEPENDS:${PN}:append:hd51 = " gstreamer1.0-plugins-base gstreamer1.0-plugins-bad"
RDEPENDS:${PN}-dev:append:spark = " tdt-driver-dev"
RDEPENDS:${PN}-dev:append:tripledragon = " triple-sdk-dev"

SRCREV = "${LIBSTB_HAL_SRCREV}"
PV = "0.1+git${SRCPV}"

# prepend, or it will end up in -bin package...
PACKAGES:prepend:spark = "spark-fp "
# libstb-hal-bin package for testing binaries etc.
PACKAGES += "${PN}-bin"

LIBSTB_HAL_GIT ?= "git://github.com/neutrino-mp/libstb-hal.git;protocol=https;nobranch=1"

# The historical Broadcom ARM-box backend is maintained in the pinned
# cst-next tree.  It is the only verified libstb-hal source here that
# contains the HD51 / Nexus / /proc/stb implementation.  Keep it scoped to
# HD51; all other machines continue to use the common upstream tree below.
LIBSTB_HAL_GIT:hd51 = "git://github.com/TangoCash/libstb-hal-cst-next.git;protocol=https;nobranch=1"

SRC_URI = " \
	${LIBSTB_HAL_GIT} \
	file://blank_480.mpg \
	file://blank_576.mpg \
	file://timer-wakeup.init \
	file://0001-raspi-implement-get-video-system.patch \
	file://0002-add-video-display-capabilities.patch \
	file://0003-generic-pc-ffmpeg6-compat.patch \
	file://0004-generic-hd51-boxmodel.patch \
"

SRC_URI:hd51 = "${LIBSTB_HAL_GIT};destsuffix=git file://COPYING.GPL file://0005-armbox-dvb-uapi-compat.patch file://0006-armbox-gstreamer-pkgconfig.patch"
SRCREV:hd51 = "e93920680b66ddc356ebf8c97d86df660f167616"
LIC_FILES_CHKSUM:hd51 = "file://${UNPACKDIR}/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe"
S:hd51 = "${UNPACKDIR}/git${@''}"

# the build system is not really broken wrt separate builddir,
# but I want it to build inside the source for various reasons :-)
inherit autotools-brokensep pkgconfig neutrino-architecture-check

# CFLAGS:append = " -Wall -W -Wshadow -g -O2 -fno-strict-aliasing -rdynamic -DNEW_LIBCURL"

CFLAGS_spark += "-funsigned-char"
CPPFLAGS_tripledragon += "-I${STAGING_DIR_HOST}/usr/include/hardware"

LDFLAGS = " -Wl,-rpath-link,${STAGING_DIR_HOST}/usr/lib -L${STAGING_DIR_HOST}/usr/lib"

EXTRA_OECONF += "\
	--enable-maintainer-mode \
	--disable-silent-rules \
	--enable-shared \
"

EXTRA_OECONF:append:spark = "--with-boxtype=spark"
EXTRA_OECONF:append:raspberrypi = "--with-boxtype=generic --with-boxmodel=raspi"
EXTRA_OECONF:append:raspberrypi2 = "--with-boxtype=generic --with-boxmodel=raspi"
EXTRA_OECONF:append:raspberrypi = " --disable-clutter"
EXTRA_OECONF:append:raspberrypi2 = " --disable-clutter"
EXTRA_OECONF:append:tripledragon = "--with-boxtype=tripledragon"

# cst-next selects the verified Broadcom ARM-box HAL.  Do not pull desktop
# OpenGL, GLUT, GLEW or libao into this embedded target.
EXTRA_OECONF:hd51 = "--enable-maintainer-mode --disable-silent-rules --enable-shared --with-boxtype=hd51 --with-boxmodel=hd51 --enable-gstreamer_10"
DEPENDS:append:hd51 = " alsa-lib gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-bad openssl"

do_configure:prepend() {
	export AUTOMAKE="automake --foreign"
}

do_install:append() {
	install -d ${D}/${datadir}
}

do_install:append:tripledragon() {
	install -D -m 0644 ${WORKDIR}/blank_576.mpg ${D}/${datadir}/tuxbox/blank_576.mpg
	install -D -m 0644 ${WORKDIR}/blank_480.mpg ${D}/${datadir}/tuxbox/blank_480.mpg
}

do_install:append:spark() {
	install -D -m 0755 ${WORKDIR}/timer-wakeup.init ${D}/${sysconfdir}/init.d/timer-wakeup
	# neutrino is 99, so we put this at 98.
	update-rc.d -r ${D} timer-wakeup start 98 5 .
}

# pic2m2v is included in lib package, because it is always needed,
# libstb-hal-bin contains all other binaries, which are rather for testing only
FILES:${PN} = "\
	${libdir}/* \
	${bindir}/pic2m2v \
	${datadir} \
"

FILES:${PN}-dev += "${includedir}/libstb-hal/*"

FILES:spark-fp = " \
	${bindir}/spark_fp \
	${sysconfdir} \
"

pkg_postinst:${PN} () {
	# after updating libstb-hal, neutrino should be restarted.
	touch /tmp/.restart
}
