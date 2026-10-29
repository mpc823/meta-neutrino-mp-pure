SUMMARY = "Port of neutrino to multiple platforms, including (but not limited to) the Tripledragon, AZbox ME/miniME and Fulan Spark/Spark7162 settop boxes and the Raspberry Pi."
DESCRIPTION = "Port of neutrino to multiple platforms."
HOMEPAGE = "https://github.com/neutrino-mp"
SECTION = "libs"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${UNPACKDIR}/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe \
"

# even if I'd like it to be otherwise, right now neutrino is machine specific, not CPU specific
PACKAGE_ARCH = "${MACHINE_ARCH}"

PREFERRED_PROVIDER_virtual/stb-hal-libs ?= "libstb-hal"

DEPENDS += " \
	update-rc.d-native \
	virtual/stb-hal-libs \
	curl \
	freetype \
	giflib \
	libpng \
	jpeg \
	libdvbsi++ \
	ffmpeg \
	openthreads \
	lua5.2 \
	luaposix \
	libsigc++-2.0 \
	pugixml \
"

RDEPENDS:${PN} += " \
	tzdata \
	luaposix \
"

# maybe this should rather be in the image dependencies? it is not needed for building...
DEPENDS:append:tripledragon = " \
	td-dvb-wrapper \
"
# need to check ffmpegdec on triple...
DEPENDS:append:tripledragon = " \
	tremor \
	flac \
	libid3tag \
	libmad \
	libvorbis \
"
RDEPENDS:${PN}:append:tripledragon = " kernel-module-td-dvb-frontend"
RDEPENDS:${PN}:append:spark = " spark-fp"

RCONFLICTS:${PN} = "neutrino-hd2"

# loosely follow the tuxbox versioning
N_MAJOR = "3"
N_MINOR = "7"
N_MICRO = "1"

SRCREV = "${NEUTRINO_MP_SRCREV}"
PV = "${N_MAJOR}.${N_MINOR}.${N_MICRO}+git${SRCPV}"
#PR = "r2"

NEUTRINO_MP_GIT ?= "git://github.com/neutrino-mp/neutrino-mp.git;protocol=https;nobranch=1"

SRC_URI = " \
	${NEUTRINO_MP_GIT} \
	file://neutrino.init \
	file://timezone.xml \
	file://custom-poweroff.init \
	file://mount.mdev \
	file://COPYING.GPL \
	file://0001-timerdclient-include-time-header.patch \
	file://0002-tuxtxt-include-pthread-header.patch \
	file://0003-dvbsubtitle-include-pthread-header.patch \
	file://0004-rcinput-support-time64-input-event.patch \
	file://0005-helpers-fix-ramfs-case-type.patch \
	file://0006-eitd-make-set-comparators-const.patch \
	file://0007-eitd-include-ctime.patch \
	file://0008-controlapi-use-time-t-for-stop-time.patch \
	file://0009-configure-do-not-embed-build-paths.patch \
"

# the build system is not really broken wrt separate builddir,
# but I want it to build inside the source for various reasons :-)
inherit autotools-brokensep pkgconfig update-rc.d

INITSCRIPT_PACKAGES   = "${PN}"
INITSCRIPT_NAME:${PN} = "neutrino"
INITSCRIPT_PARAMS:${PN} = "start 99 5 . stop 20 0 1 2 3 4 6 ."

include neutrino-mp.inc

do_configure:prepend() {
	INSTALL="`which install` -p"
	export INSTALL
}

do_compile () {
	oe_runmake clean
	oe_runconf
	PKG_VER="$(sed -n '/^#define PACKAGE_VERSION/s/#define PACKAGE_VERSION //p' config.h)"
	if test $PKG_VER != '"${N_MAJOR}.${N_MINOR}.${N_MICRO}"'; then
		bbwarn "Package version $PKG_VER not consistent with neutrino_mp.bb"
		bbwarn "configured version is ${N_MAJOR}.${N_MINOR}.${N_MICRO}"
	fi
	# unset CFLAGS CXXFLAGS LDFLAGS
	oe_runmake CFLAGS="${N_CFLAGS}" CXXFLAGS="${N_CXXFLAGS}" LDFLAGS="${N_LDFLAGS}"
}

do_install:prepend () {
	install -d ${D}/${sysconfdir}/init.d
	install -m 755 ${UNPACKDIR}/neutrino.init ${D}/${sysconfdir}/init.d/neutrino
	install -m 755 ${UNPACKDIR}/custom-poweroff.init ${D}/${sysconfdir}/init.d/custom-poweroff
	install -m 644 ${UNPACKDIR}/timezone.xml ${D}/${sysconfdir}/timezone.xml
	install -d ${D}/lib/mdev/fs
	install -m 755 ${UNPACKDIR}/mount.mdev ${D}/lib/mdev/fs/mount
	install -d ${D}/var/cache
	install -d ${D}/var/tuxbox/config/
	install -d ${D}/var/tuxbox/plugins/
	echo "version=1${N_MAJOR}${N_MINOR}${N_MICRO}`date +%Y%m%d%H%M`" > ${D}/.version
	echo "creator=${MAINTAINER}"             >> ${D}/.version 
	echo "imagename=Neutrino-MP"             >> ${D}/.version 
	echo "homepage=${HOMEPAGE}"              >> ${D}/.version 
	update-rc.d -r ${D} custom-poweroff start 89 0 .
}

# compatibility with binaries hand-built with --prefix=
do_install:append() {
	install -d ${D}/share/
	ln -s ../usr/share/tuxbox ${D}/share/
	ln -s ../usr/share/fonts  ${D}/share/
	_VC=${D}/var/tuxbox/config
	cp -p ${_VC}/bad_package_pattern.list.sample ${_VC}/bad_package_pattern.list
	# do not package the default config
	rm -f ${_VC}/tuxtxt/tuxtxt2.conf
}

# disarm all automatic restart stuff, or we will blow up
# when updating from the GUI...
# stock postrm is "mostly harmless"...
updatercd_prerm () {
}

updatercd_preinst() {
if type update-rc.d >/dev/null 2>/dev/null; then
	if [ -n "$D" ]; then
		OPT="-f -r $D"
	else
		OPT="-f"
	fi
	update-rc.d $OPT ${INITSCRIPT_NAME} remove
fi
}

updatercd_postinst() {
if type update-rc.d >/dev/null 2>/dev/null; then
	if [ -n "$D" ]; then
		OPT="-r $D"
	fi
	update-rc.d $OPT ${INITSCRIPT_NAME} ${INITSCRIPT_PARAMS}
fi
}


FILES:${PN} += "\
	/.version \
	${sysconfdir} \
	/usr/share \
	/usr/share/tuxbox \
	/usr/share/iso-codes \
	/usr/share/fonts \
	/usr/share/tuxbox/neutrino \
	/usr/share/iso-codes \
	/usr/share/fonts \
	/lib/mdev/fs/mount \
	/share/fonts \
	/share/tuxbox \
	/var/cache \
	/var/tuxbox/plugins \
"

CONFFILES:${PN} += " \
	/var/tuxbox/config/bad_package_pattern.list \
	/var/tuxbox/config/cables.xml \
	/var/tuxbox/config/radio-stations.xml \
	/var/tuxbox/config/satellites.xml \
	/var/tuxbox/config/terrestrial.xml \
	/var/tuxbox/config/nhttpd.conf \
	/var/tuxbox/config/tobackup.conf \
"

pkg_postinst:${PN} () {
	update-alternatives --install /bin/backup.sh backup.sh /usr/bin/backup.sh 100
	update-alternatives --install /bin/install.sh install.sh /usr/bin/install.sh 100
	update-alternatives --install /bin/restore.sh restore.sh /usr/bin/restore.sh 100
	# pic2m2v is only available on platforms that use "real" libstb-hal
	if which pic2m2v >/dev/null 2>&1; then
		# neutrino icon path
		I=/usr/share/tuxbox/neutrino/icons
		pic2m2v $I/mp3.jpg $I/radiomode.jpg $I/scan.jpg $I/shutdown.jpg $I/start.jpg
	fi
	# after updating neutrino, it should be restarted...
	touch /tmp/.restart
}
