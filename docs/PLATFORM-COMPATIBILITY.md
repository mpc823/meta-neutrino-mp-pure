# Neutrino-MP platform inventory

This matrix is based on the complete `4a2455e` layer, its history, recipes and
`doc/Makefile.SDK`. A Raspberry Pi build is not evidence for another row.

| Historical target / receiver family | CPU / ABI | Historical BSP dependency | Wrynose BSP status | Build status | Restriction |
|---|---|---|---|---|---|
| `raspberrypi` (Pi 1) | ARMv6 hard-float | `meta-raspberrypi`, userland | available upstream | not run | old VideoCore/userland path must be audited |
| `raspberrypi2` (Pi 2) | ARMv7 hard-float | `meta-raspberrypi`, userland | available upstream | build running | reference platform; image, boot and hardware smoke test pending |
| `coolstream` (Neo/HD family) | ARMv6, vendor ABI | Coolstream `cs-drivers`, `virtual/stb-hal-libs` | no maintained public Wrynose BSP found here | blocked | proprietary `libcoolstream` and vendor kernel/userspace |
| `spark` (Fulan Spark) | SH-4 | `meta-stlinux`, `tdt-driver` | no maintained public Wrynose BSP found here | blocked | old SH-4 kernel, proprietary driver and `spark_fp` |
| `spark7162` | SH-4 | `meta-stlinux`, `tdt-driver` | no maintained public Wrynose BSP found here | blocked | same vendor toolchain/driver problem |
| `tripledragon` | PowerPC | `meta-magpie`/Tripledragon SDK, `td-dvb-wrapper` | no maintained public Wrynose BSP found here | blocked | obsolete kernel, DirectFB, binary SDK and DVB module |
| AZbox ME / miniME | MIPS (historical) | libstb-hal generic/AZbox support; no machine here | no machine/BSP present | unverified | recipe text names it, but no build target or pinned BSP |
| generic / `--with-boxmodel=raspi` | BSP-dependent | libstb-hal generic backend | not a receiver target | unverified | compatibility mode, not hardware support |

“Blocked” means this checkout cannot produce a complete image from checked-in
sources alone. It does not remove the target. Unblocking requires a maintained
BSP, exact kernel/bootloader revisions and legally redistributable drivers (or
documented binary inputs), plus a separate build result for that row.

This repository has no own `conf/machine` files. Historical machine names came
from external layers and are recorded explicitly rather than aliased silently.
