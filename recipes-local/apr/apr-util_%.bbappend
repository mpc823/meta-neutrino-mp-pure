# This does not belong here, but remains required by the historical ABI.
do_configure:append:class-native() {
	grep ^LTFLAGS ${B}/build/rules.mk
	sed -i 's,^LTFLAGS[[:space:]]*= --silent,LTFLAGS = --tag=CC --silent,' ${B}/build/rules.mk
	grep ^LTFLAGS ${B}/build/rules.mk
}
