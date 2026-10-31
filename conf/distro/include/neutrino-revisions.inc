# Keep source revisions in one place. Production recipes must use full commit IDs.
NEUTRINO_MP_LAYER_REVISION = "4a2455e"
NEUTRINO_BSP_LOCKFILE ?= "${TOPDIR}/../.neutrino-build/yocto-revisions.lock"

# Verified HTTPS GitHub refs (2026-10-28). Do not replace these with AUTOREV.
NEUTRINO_MP_SRCREV = "7e3d4266426726aeab3233bf22caf215e119138e"
NEUTRINO_HD_PLUGINS_SRCREV = "a87d5d51baa4e1ebcf72f9742e641199aff2af31"
LIBSTB_HAL_SRCREV = "b1f5e8046267d1f5463211cc2151487b6aa9cbef"
COOLITHEK_SRCREV = "2c2d6692301b3dc548d7834335da2d01e1bd5ed6"
NEUTRINO_MEDIATHEK_SRCREV = "4b1916c4b8994b63e5cd66abfdf80f4564d2daa3"
MINISATIP_SRCREV = "fbdeca128851435289b2538e667650234491a742"
OPKG_UTILS_SRCREV = "5e2d056ed245cd453a801d9bb2ec0707010b7038"
XUPNPD_SRCREV = "33ce60eadf4930542a0f005eff606e978baf89ff"

# OE-Alliance recipes that are parsed by the HD51 layer but otherwise leave
# their Git branch floating.  These are verified HTTPS branch heads and keep
# metadata parsing reproducible; the image only consumes the DVB media sink.
SRCREV:pn-aio-grab = "bbc6b34e03d0bd42a84368e74608fbef6327d4be"
SRCREV:pn-gstreamer1.0-plugin-dvbmediasink = "ff41cc6d77b69fb833f409f7eb0d4ebdab8e4e7f"
SRCREV:pn-enigma2-plugin-extensions-epgimport = "b9ff4f96623a1594a8472751fd60bcaaa8d68c19"
SRCREV:pn-enigma2-plugin-extensions-tuxcom = "7a7f98818ddeb09bb91be1e46859a8591bdd7ffe"
SRCREV:pn-enigma2-plugin-extensions-tuxterm = "46c3d9e43ec97ba07398e08fac0241cc9076ab94"
SRCREV:pn-enigma2-plugin-skins-pli-hd = "3a0030415a6f08c5387485959a508b8277f8a623"
SRCREV:pn-enigma2-skins = "472e9d9257dbef16205b00b3dc748eeddc10e3aa"
