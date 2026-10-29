FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
DEPENDS:append = " libtirpc"
CFLAGS:append = " -I${STAGING_INCDIR}/tirpc"
EXTRA_OEMAKE:append = " CONFIG_EXTRA_LDLIBS='tirpc'"

# PRINC is now deprecated...
PR .= ".2"

SRC_URI += " \
	file://neutrino-busybox.cfg \
	file://telnetd.busybox \
	file://hostname.script \
"

PACKAGES:prepend = "${PN}-inetd ${PN}-telnetd "
FILES:${PN}-inetd = " \
	/etc/init.d/inetd.busybox \
	/etc/inetd.conf \
"
FILES:${PN}-telnetd = " \
	/etc/init.d/telnetd.busybox \
"
RRECOMMENDS:${PN} += "${PN}-inetd ${PN}-telnetd"
INITSCRIPT_PACKAGES += "${PN}-inetd ${PN}-telnetd"
INITSCRIPT_NAME:${PN}-inetd = "inetd.busybox"
INITSCRIPT_NAME:${PN}-telnetd = "telnetd.busybox"
INITSCRIPT_PARAMS:${PN}-inetd = "defaults"
INITSCRIPT_PARAMS:${PN}-telnetd = "defaults"
# The upstream BusyBox recipe installs /etc/init.d/syslog.  Keep the
# init-script metadata aligned with that file so update-rc.d can create the
# links during image construction.
INITSCRIPT_NAME:${PN}-syslog = "syslog"
ALTERNATIVE:busybox-syslog:remove = "syslog-init"
RCONFLICTS:${PN}-syslog = "sysklogd"

do_install:append() {
	if grep "CONFIG_TELNETD=y" ${B}/.config; then
		install -m 0755 ${UNPACKDIR}/telnetd.busybox ${D}${sysconfdir}/init.d/telnetd.${BPN}
	fi
	if grep "CONFIG_UDHCPC=y" ${B}/.config; then
		install -m 0755 ${UNPACKDIR}/hostname.script ${D}${sysconfdir}/udhcpc.d/51hostname
	fi
}

pkg_prerm:${PN}-telnetd () {
#!/bin/sh
exit 0
}

pkg_preinst:${PN}-telnetd() {
#!/bin/sh
if test "x$D" != "x"; then OPT="-r $D"; fi
if type update-rc.d >/dev/null 2>/dev/null; then update-rc.d -f $OPT telnetd.busybox remove; fi
exit
}

pkg_postinst:${PN}-telnetd() {
#!/bin/sh
if test "x$D" != "x"; then OPT="-r $D"; fi
if type update-rc.d >/dev/null 2>/dev/null; then update-rc.d $OPT telnetd.busybox defaults; fi
exit
}

pkg_prerm:${PN}-syslog() {
	if test "x$D" = "x" && test "$1" = "upgrade" -o "$1" = "remove"; then /etc/init.d/syslog.busybox stop; fi
	update-alternatives --remove syslog-conf /etc/syslog.conf.busybox
	update-alternatives --remove syslog-startup-conf /etc/syslog-startup.conf.busybox
	exit 0
}

pkg_prerm:${PN}:append() {
	UA=`type -p update-alternatives`
	if [ -n "$UA" ]; then sed -n '2,$p' $UA > $tmpdir/update-alternatives; chmod 755 $tmpdir/update-alternatives; fi
}
