# HD51 source index

Last reviewed: 2026-10-29.  A source is marked **verified** only when the
repository, revision and relevant files were inspected locally.  A candidate
is not used in the build merely because its name contains `hd51`.

## Verified repositories

| Project | URL | Branch | Revision | Use |
|---|---|---|---|---|
| OE-Alliance core | https://github.com/oe-alliance/oe-alliance-core | `6.0` | `9f7fcf3093635182d51cb14c6206c83b36af4d26` | Wrynose-compatible `meta-brands/meta-gfutures`, kernel/DVB/V3D/image metadata |
| Neutrino HD BSP | https://github.com/neutrino-hd/meta-hd51 | `gatesgarth` | `260fa831d8a4d523710839f66a2eaa9fa2df24bb` | Historical MACHINE and Neutrino integration |
| Neutrino layer | https://github.com/neutrino-hd/meta-neutrino | `gatesgarth` | `64eb565ba25f8c1c22ee3da27b34f8cb7b6df7fa` | Historical matching Neutrino recipes |
| Build environment | https://github.com/KittyBua/buildenv | `3.2` | `55d9f0252230774e5dedc24cec179d6b7db2c215` | Poky 3.2/Gatesgarth orchestration |
| DDT build system | https://github.com/Duckbox-Developers/buildsystem-ddt | `master` | `7c1e26c5b2175f8b658658ce632e200090d8171f` | Legacy kernel patches and image layout reference |
| OpenPLi core | https://github.com/OpenPLi/openpli-oe-core | `scarthgap` | `3256ee6f239a89e57d24153e06361643bd5d3441` | Independent Scarthgap cross-check; not used by Wrynose manifest |

## Candidate repositories

| Candidate | Finding | Status |
|---|---|---|
| OE-Alliance core | Current `6.0` branch lists AX/Mut@nt HD51 and contains `meta-gfutures`. | Verified and selected |
| OpenATV / OE-Alliance consumers | HD51 machines are represented through OE-Alliance layers; no separate Wrynose-compatible HD51 BSP was found in the examined OpenATV repository. | Reference only |
| NI / Neutrino Images | Relevant plugins and historical image work exist, but the inspected HD51 machine/build basis is the Gatesgarth `meta-hd51` path above. | Reference only |
| Broadcom/Nexus/V3D standalone sources | No redistributable open HD51 Nexus/V3D implementation was found. | Proprietary dependency |

## Release conclusion

The selected Wrynose path is OE-Alliance `6.0` at the pinned revision. Its
`meta-gfutures/conf/layer.conf` explicitly declares Wrynose compatibility.
The older `meta-hd51`/`meta-neutrino` pair remains useful for historical
comparison, but must not be mixed into the current layer stack.
