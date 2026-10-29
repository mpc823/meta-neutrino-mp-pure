# HD51 version matrix

| Stack | Yocto/OE basis | Kernel | Machine/ABI | DVB/V3D | Image format | Assessment |
|---|---|---|---|---|---|---|
| Current project | Wrynose | Raspberry Pi baseline; HD51 provider external | ARMv7, `cortexa15hf-neon-vfpv4` | OE-Alliance `meta-gfutures` | HD51 eMMC multiboot | Selected path, parse/build pending |
| OE-Alliance 6.0 | Wrynose-compatible | 4.10.12 `linux-gfutures` | ARM hard-float NEON | kernel-matched closed DVB modules, closed Nexus/V3D | `hdemmc_image_multiboot` | Closest practical source |
| `meta-hd51` | Gatesgarth/Poky 3.2 | 4.10.12 `linux-gfutures` | ARM hard-float NEON | 2019 closed modules/V3D | legacy `hd-emmc` | Historical, not mixable |
| OpenPLi | Scarthgap | 4.10.12 `linux-hd` | ARM hard-float NEON | 2025 closed modules/V3D | eMMC/multiboot | Cross-check, not mixable |
| DDT | Make-based legacy builder | 4.10.x patches | ARM | vendor modules | disk/multi-rootfs | Layout/patch reference only |

The CPU architecture is not sufficient for module compatibility. The DVB
archives are tied to kernel 4.10.12 and vendor configuration; the V3D/Nexus
userspace is tied to the vendor ABI and cannot be replaced by Mesa without a
different HAL path.
