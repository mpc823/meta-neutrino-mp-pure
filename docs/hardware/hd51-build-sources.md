# HD51 build-source comparison

This records the source review performed before extending the HD51 target. It
is a provenance and compatibility record, not permission to copy closed
binaries into this repository.

## Revisions examined

| Project | Branch | Revision | Result |
| --- | --- | --- | --- |
| `neutrino-hd/meta-hd51` | `gatesgarth` | `260fa831d8a4d523710839f66a2eaa9fa2df24bb` | HD51 MACHINE, Linux 4.10.12 and Neutrino integration |
| `neutrino-hd/meta-neutrino` | `gatesgarth` | `64eb565ba25f8c1c22ee3da27b34f8cb7b6df7fa` | Matching historical Neutrino layer |
| `KittyBua/buildenv` | `3.2` | `55d9f0252230774e5dedc24cec179d6b7db2c215` | Poky 3.2/Gatesgarth build orchestration |
| `Duckbox-Developers/buildsystem-ddt` | `master` | `7c1e26c5b2175f8b658658ce632e200090d8171f` | Legacy kernel/image builder and HD51 references |
| `OpenPLi/openpli-oe-core` | `scarthgap` | `3256ee6f239a89e57d24153e06361643bd5d3441` | Newer equivalent BSP, incompatible release basis |

This repository is Wrynose. None of these revisions is a Wrynose BSP layer.
Gatesgarth is the closest complete Neutrino solution, but still requires a
deliberate release migration or an actual metadata port.

## Technical solutions found

The Gatesgarth `meta-hd51` layer provides:

- `conf/machine/hd51.conf` and Cortex-A15 hard-float NEON tuning;
- `linux-gfutures_4.10.12.bb`, with the HD51 kernel archive, defconfig,
  Device Tree/kernel patches and initramfs helper;
- `gfutures-dvb-modules-hd51.bb`, kernel-matched DVB modules;
- `hd-v3ddriver-hd51.bb` and V3D headers, supplied as closed downloads;
- an eMMC multiboot image class and `STARTUP` generation;
- Neutrino HAL configure fragments selecting `armbox`/`hd51`.

`buildsystem-ddt` independently confirms ARM, 4.10-kernel DVB/BLINDSCAN
patches, reserved DVB adapter handling, Broadcom MMC handling and four
kernel/rootfs slots. Its image code is a legacy Make implementation, not a
drop-in BitBake layer.

OpenPLi's `meta-gfutures` contains the same hardware family but uses
`linux-hd_4.10.12.bb`, `hd-dvb-modules-hd51.bb` and equivalent closed V3D
providers under Scarthgap. It is a cross-check, not a Wrynose dependency.

## Sources and licensing

The `meta-hd51` README declares BSD-2-Clause for the layer, but the checkout
has no standalone license file and points to the Yocto common-licenses copy.
This must be resolved before redistributing copied metadata.

The kernel recipe is GPL; DVB modules and V3D/Nexus recipes declare
`proprietary` or `CLOSED`. Their URLs are reachable and recipe checksums are
present, but availability is not redistribution permission. No closed archive,
firmware, driver or header was copied into this project.

The historical kernel, DVB-driver, V3D-driver and OpenPLi kernel URLs were
checked with HTTP HEAD requests and responded successfully. The large files
were not downloaded into the repository.

## Porting decision

The local `conf/machine/hd51.conf` follows the Gatesgarth Neutrino provider
names (`linux-gfutures`, `gfutures-dvb-modules-hd51`) and records the exact
external layer revision. The generic HAL is prepared with the HD51 box model,
but no fake hardware implementation is introduced.

Not ported yet:

- old `_append`/`_prepend` syntax and Gatesgarth-only classes;
- closed DVB/V3D archives;
- the legacy DDT Makefile image builder;
- a kernel or Device Tree without a release-compatible BSP layer.

The current implementation milestone is the pinned OE-Alliance Wrynose
manifest in `kas/hd51-wrynose.yml`. It uses the open machine/image metadata
and declares the closed BSP providers as explicit external fetches. The
broader Neutrino ecosystem review is maintained in
`docs/NEUTRINO_ECOSYSTEM_REUSE.md`; its pinned repositories are comparison
sources and are not silently added as extra Wrynose layers.

## References

- [meta-hd51](https://github.com/neutrino-hd/meta-hd51)
- [meta-neutrino](https://github.com/neutrino-hd/meta-neutrino)
- [buildsystem-ddt](https://github.com/Duckbox-Developers/buildsystem-ddt)
- [buildenv](https://github.com/KittyBua/buildenv)
- [OpenPLi openembedded overlay](https://github.com/OpenPLi/openpli-oe-core)
