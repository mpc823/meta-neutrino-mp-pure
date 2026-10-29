# Receiver inventory: Mut@nt HD51

Status: **experimental, inventory verified; no Neutrino image booted**.

## Access and safety

The device at `192.168.0.201` answered ICMP and exposed Telnet on port 23.
SSH on port 22 refused the connection.  The inventory was collected through a
read-only root shell.  No files were changed, no modules were loaded or
removed, no service was stopped, and the receiver was not rebooted or
flashed.  The login password is intentionally not recorded here.

## Verified identity and hardware

- Model: `hd51`, Mut@nt HD51; banner `hd51.NI-432E51`.
- SoC: Broadcom `bcm7251s`; device-tree model `BCM97251SSV`.
- CPU: two ARMv7 processor entries, Cortex-A15-class tuning used by the
  matching BSP (`cortexa15hf-neon-vfpv4`), NEON/VFP available.
- Memory: 1,027,592 kB RAM and 502,780 kB swap reported by the receiver.
- Storage: eMMC root on `/dev/mmcblk0p5`, boot partition `/dev/mmcblk0p1`,
  additional eMMC slot partitions, and an external `/dev/sda1` mount.
- Boot arguments use `hd51_4.boxmode=1` and a 440 MiB + 192 MiB Broadcom CMA
  split.
- Network and USB are provided by the running kernel; the inventory did not
  alter either subsystem.

## Operating system and boot

The receiver runs Linux `4.10.12`, ARMv7, with an OpenPLi-derived image
release `30d241f`, version `2`, and a permanently loaded `hd51` family module.
`/boot/STARTUP` selects eMMC kernel slots; the observed entries use
`emmcflash0.kernel1` through `kernel4` with root partitions p3, p5, p7 and p9.
This is a multiboot/eMMC layout.  No bootloader or partition operation was
performed.  A future test must use the receiver's documented recovery or
multiboot path and preserve the existing slot; this inventory is not a flash
authorization.

The OpenPLi machine definition confirms the intended HD51 BSP contract:
Linux 4.10.12, `bcm7251s`, `cortexa15hf-neon-vfpv4`, and an eMMC multiboot
image class.  The matching external layer is `meta-gfutures`; its current
source revision inspected for this inventory is
`3256ee6f239a89e57d24153e06361643bd5d3441`.

## DVB, video, audio and OSD interfaces

The running system exposes one DVB adapter in sysfs:

- `/sys/class/dvb/dvb0.frontend0`
- `demux0` through `demux7`, `dvr0` through `dvr7`
- `video0`, `audio0`, and conditional-access devices `ca0` through `ca7`

The `/proc/stb` interface exposes vendor-specific controls for audio mute and
delay, HDMI/video mode and aspect, framebuffer/3D mode, deinterlacing,
transport-stream muxing, front-panel/LNB sensing, VFD and HDMI settings.
These are not generic Linux APIs and require an HD51-specific HAL backend or
an adapter around the existing vendor libraries.

The running image contains `/usr/bin/neutrino` and `/usr/bin/enigma2`.
Presence of these programs is not a test of the Neutrino-MP-Pure image or of
functional video/audio output.  No satellite lock was tested during the
read-only inventory.

## Build integration in this repository

`conf/machine/hd51.conf` now records the verified CPU, kernel/provider and
external-BSP contract.  The common `libstb-hal` recipe selects the existing
generic HAL build interface with `--with-boxmodel=hd51`; this is compile-time
preparation only and does not claim support for the proprietary video/OSD
driver.

The current project manifest is Wrynose, while the inspected OpenPLi
`meta-gfutures` layer declares Scarthgap compatibility.  Mixing those layer
generations is unsafe.  `scripts/setup-build hd51` therefore stops with an
explicit diagnostic instead of creating a misleading image.  A bootable HD51
build requires a separately pinned, Wrynose-compatible BSP migration (or a
coordinated project migration to the BSP's release), plus the proprietary
kernel module and V3D recipes supplied by that BSP.

## Reusable versus missing Neutrino pieces

Reusable now:

- shared Neutrino core, DVB/Zapit model, stream manager and configuration;
- existing `libstb-hal` build contract and architecture checking;
- generic recipe dependencies where they match the target sysroot.

Still required for real HD51 operation:

- an HD51 HAL backend for vendor `/proc/stb`, framebuffer/OSD, audio/video
  decoder control and remote input;
- the matching `linux-hd` source/configuration and `hd-dvb-modules-hd51`;
- the proprietary `hd-v3ddriver-hd51`/Nexus EGL stack;
- image generation for the HD51 eMMC/multiboot layout;
- real hardware tests with a preserved recovery path.

No No-DVB build variant is introduced.  DVB remains part of the common core;
availability of local or network tuners is a runtime resource decision.

## Reproducible checks

```sh
./scripts/validate-layer
./scripts/preflight --quick --machine hd51
./scripts/setup-build hd51   # intentionally stops until the BSP contract is met
```

The first two commands validate repository metadata and report the missing
external BSP explicitly.  They do not claim an image build or hardware boot.

## References

The receiver identity and BSP relationship were cross-checked against the
official OpenPLi receiver/download and source metadata.  The external BSP is
not copied into this repository and no firmware blob or credential is stored.
