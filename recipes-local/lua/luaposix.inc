DESCRIPTION = "luaposix is a POSIX binding, including curses, for Lua 5.1 and 5.2."
LICENSE = "MIT"
HOMEPAGE = "https://github.com/luaposix/luaposix"

DEPENDS += "lua5.2-native lua5.2 ncurses"

SRC_URI = "git://github.com/luaposix/luaposix.git;protocol=https;nobranch=1"
SRCREV = "2f6b3f687dfdf5b43ec2e51575e1329be9dda09c"

inherit autotools pkgconfig

TARGET_CC_ARCH += " -fPIC ${LDFLAGS}"
EXTRA_OEMAKE = "'CC=${CC} -fPIC' 'MYCFLAGS=${CFLAGS} -DLUA_USE_LINUX -fPIC' MYLDFLAGS='${LDFLAGS}'"
# luaposix-31's generated documentation rules are not parallel-safe: two
# targets create the same files and the helper is referenced relative to the
# source tree although the build is out-of-tree. Keep the normal global -j2
# policy, but serialize this legacy recipe's make phase after fixing the path.
EXTRA_OEMAKE += "-j1"
EXTRA_OECONF += "--datadir=${datadir}/lua/5.2 --libdir=${libdir}/lua/5.2"

do_configure:append() {
    cp ${S}/build-aux/make_lcurses_doc.pl ${B}/make_lcurses_doc.pl
    sed -i \
        -e "s#'ext/curses/curses.c'#'${S}/ext/curses/curses.c'#" \
        -e "s#'doc/curses_c.html'#'${B}/doc/curses_c.html'#" \
        -e "s#'doc/curses.html'#'${B}/doc/curses.html'#" \
        ${B}/make_lcurses_doc.pl
    sed -i \
        -e 's#$(PERL) build-aux/make_lcurses_doc.pl#$(PERL) $(builddir)/make_lcurses_doc.pl#g' \
        ${B}/Makefile
}

BBCLASSEXTEND = "native"

FILES:${PN}-dbg += "${libdir}/lua/5.2/.debug"
FILES:${PN}-dev += "${libdir}/lua/5.2/*.la"
FILES:${PN} += "${libdir}/lua/5.2/*.so"
FILES:${PN} += "${datadir}/lua/5.2/*.lua"
