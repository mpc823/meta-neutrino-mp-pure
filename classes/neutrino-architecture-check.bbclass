SUMMARY = "Fail target builds when stale or foreign ELF artifacts are present"

# This class checks target artifacts only. Native/cross helper recipes are not
# inherited by the two target recipes below, so host tools remain separate.
NEUTRINO_ARCH_CHECK_SCRIPT ?= "${NEUTRINO_LAYERDIR}/scripts/check-build-architecture"
NEUTRINO_ARCH_CHECK_COMPILER ?= "${TARGET_PREFIX}gcc"

neutrino_architecture_check() {
	[ -x "${NEUTRINO_ARCH_CHECK_SCRIPT}" ] || bbfatal "Architekturprüfer fehlt: ${NEUTRINO_ARCH_CHECK_SCRIPT}"
	command -v "${NEUTRINO_ARCH_CHECK_COMPILER}" >/dev/null || \
		bbfatal "Zielcompiler für Architekturprüfung fehlt: ${NEUTRINO_ARCH_CHECK_COMPILER}"
	[ -d "${1}" ] || return 0
	"${NEUTRINO_ARCH_CHECK_SCRIPT}" --compiler "${NEUTRINO_ARCH_CHECK_COMPILER}" "${1}"
}

# The prepend check catches stale objects before a linker can consume them.
do_compile:prepend() {
	neutrino_architecture_check "${B}"
}

# The append check covers freshly compiled objects and archives.
do_compile:append() {
	neutrino_architecture_check "${B}"
}

# `do_package` is a Python task in modern OE-Core; checking `${B}` after
# `do_compile` covers all target ELF inputs without replacing that task.
