### This fork has update-alternatives implemented in C.
FILESEXTRAPATHS:prepend := "${THISDIR}/opkg-utils:"
SRC_URI = "git://github.com/seife/opkg-utils;protocol=https;nobranch=1"
SRCREV = "${OPKG_UTILS_SRCREV}"
PR .= ".1"
