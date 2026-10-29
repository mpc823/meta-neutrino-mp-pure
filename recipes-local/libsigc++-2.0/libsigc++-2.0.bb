SUMMARY = "A library for loose coupling of C++ method calls"
SECTION = "libs"
LICENSE = "LGPL-2.1-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=d8045f3b8f929c1cb29a1e3fd737b499"

PV = "2.3.2"

DEPENDS = "mm-common"

SRC_URI = "https://download.gnome.org/sources/libsigc++/2.3/libsigc++-${PV}.tar.xz"
SRC_URI[md5sum] = "e75fbd6f5cc34d058a9dabec96245dc8"
SRC_URI[sha256sum] = "f0305bb6d2185de1513b35843f3d4a85abfec7c969034140d56cf14ce70aa411"

S = "${UNPACKDIR}/libsigc++-${PV}"

inherit autotools

EXTRA_AUTORECONF = "--exclude=autoheader"

FILES:${PN}-dev += "${libdir}/sigc++-*/"
FILES:${PN}-doc += "${datadir}/devhelp"

BBCLASSEXTEND = "native"
