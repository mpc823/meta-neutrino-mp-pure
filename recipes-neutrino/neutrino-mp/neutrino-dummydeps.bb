DESCRIPTION = "dummy dependency package for setting up the SDK."
SUMMARY = "Package that has all the dependencies of neutrino-mp / libstb-hal to properly set up the SDK."
SECTION = "libs"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://${WORKDIR}/COPYING.GPL;md5=751419260aa954499f7abaabaa882bbe \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/neutrino-mp:"

# even if I'd like it to be otherwise, right now neutrino is machine specific, not CPU specific
PACKAGE_ARCH = "${MACHINE_ARCH}"

DEPENDS += " \
	curl \
	libid3tag \
	libmad \
	freetype \
	giflib \
	libpng \
	jpeg \
	libdvbsi++ \
	ffmpeg \
	flac \
	tremor \
	openthreads \
	lua5.2 \
	luaposix \
	libsigc++-2.0 \
"

RDEPENDS:${PN} += " \
	curl \
	libid3tag \
	libmad \
	freetype \
	giflib \
	libpng \
	jpeg \
	libdvbsi++ \
	ffmpeg \
	flac \
	tremor \
	openthreads \
	lua5.2 \
	luaposix \
	libsigc++-2.0 \
"

DEPENDS:append:coolstream = "\
	virtual/stb-hal-libs \
"

DEPENDS:append:raspberrypi = "\
	virtual/egl \
"

DEPENDS:append:raspberrypi2 = "\
	userland \
"

RDEPENDS:${PN}:append:coolstream = "\
	virtual/stb-hal-libs \
"


RDEPENDS:${PN} += " \
	tzdata \
	luaposix \
"

# maybe this should rather be in the image dependencies? it is not needed for building...
DEPENDS:append:tripledragon = " \
	td-dvb-wrapper \
"
RDEPENDS:${PN}:append:tripledragon = " kernel-module-td-dvb-frontend"

##### from libstb-hal:
RDEPENDS:${PN}:append:spark = " tdt-driver libass"
### does not work, not RPROVIDED
###RDEPENDS:${PN}:append:raspberrypi += " virtual/egl"
RDEPENDS:${PN}:append:tripledragon = " directfb triple-sdk"

RDEPENDS:${PN} += "ffmpeg"
RDEPENDS:${PN}-dev:append:spark = " tdt-driver-dev"
RDEPENDS:${PN}-dev:append:tripledragon = " triple-sdk-dev"

PV = "0.1"

SRC_URI = " \
	file://COPYING.GPL \
"

do_install() {
	install -d ${D}/usr/bin
	cat >${D}/usr/bin/neutrino-dummy <<-EOF
	#!/bin/sh
	echo "this is not neutrino."
	EOF
	chmod 755 ${D}/usr/bin/neutrino-dummy
}
