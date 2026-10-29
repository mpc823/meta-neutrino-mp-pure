HOMEPAGE = "http://git.coolstreamtech.de/"
DEPENDS = "lua5.2"
RDEPENDS:${PN} = "lua-json luaposix"

#SRCREV = "${AUTOREV}"
SRCREV = "2b0687ac88ee15f33653976c9e74842bba054b52"
PV = "0.0+git${SRCPV}"

# all plain LUA, so no -dev and -dbg needed
PACKAGES = "${PN}"
inherit allarch

SRC_URI = " \
	git://coolstreamtech.de/cst-public-plugins-scripts-lua.git;nobranch=1 \
"

S = "${UNPACKDIR}/git/plugins"

FILES:${PN} = " \
	${libdir}/tuxbox/plugins \
"
