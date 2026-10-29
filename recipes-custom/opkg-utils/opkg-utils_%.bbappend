### This fork has update-alternatives implemented in C.
FILESEXTRAPATHS:prepend := "${THISDIR}/opkg-utils:"
SRC_URI = "git://github.com/seife/opkg-utils;protocol=https;nobranch=1 \
           file://0001-opkg-make-index-support-checksum-options.patch"
SRCREV = "${OPKG_UTILS_SRCREV}"
LIC_FILES_CHKSUM = "file://COPYING;md5=94d55d512a9ba36caa9b7df079bae19f \
                    file://opkg.py;beginline=2;endline=18;md5=63ce9e6bcc445181cd9e4baf4b4ccc35"
PR .= ".1"

# The selected historical fork supports gzip/xz but not OE-Core's newer
# zstd/-a opkg-build command line. Keep the IPK format compatible with the
# fork while retaining the modern indexer interface below.
OPKGBUILDCMD = "opkg-build -Z gzip"

# The historical fork still installs its Python utilities with the Python 2
# interpreter name. Modern Yocto native sysroots provide Python 3 only.
do_install:append:class-native() {
	for script in opkg-build opkg-extract-file opkg-list-fields opkg-make-index opkg-compare-indexes opkg-show-deps opkg-unbuild opkg-update-index; do
		if [ -f "${D}${bindir}/$script" ]; then
			sed -i '1s@/usr/bin/env python$@/usr/bin/env python3@' "${D}${bindir}/$script"
		fi
	done
}
