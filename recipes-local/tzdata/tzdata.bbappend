CONFFILES:${PN} += " \
	${sysconfdir}/timezone \
	"

do_install:append() {
	rm ${D}${sysconfdir}/localtime
}

pkg_postinst:${PN}:append() {
	if [ ! -e "${etc_lt}" ]; then
		ln -s "${datadir}/zoneinfo/${tz}" "${etc_lt}"
	fi
}
