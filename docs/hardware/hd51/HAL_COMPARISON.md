# HD51 HAL comparison

| Function | Existing Neutrino HAL | HD51 evidence | Required work |
|---|---|---|---|
| DVB/demux | Common `libstb-hal`/Zapit contracts | `/dev/dvb`, `hd51_1..4.ko`, DVB recipe | Adapt device/module startup and validate ABI |
| Video decode | Legacy receiver-specific HALs | Broadcom vendor kernel/Nexus stack | HD51 backend; do not use generic PC/OpenMAX path |
| Audio | Common audio contract | `/proc/stb/audio/*`, vendor decoder | Map controls and clock behaviour |
| OSD/framebuffer | Common video/OSD interfaces | `/proc/stb/fb/*`, framebuffer and Nexus | Vendor-backed OSD implementation |
| V3D/EGL | Generic PC/OpenGL and legacy backends | `hd-v3ddriver-hd51`, `libnxpl`, `libnexus` | Link only against matched closed provider |
| Remote input | Common RCInput contract | HD51 vendor module/front-panel stack | Identify device/event mapping on target image |
| HDMI/CEC | Common capability model | `/proc/stb/hdmi/*`, kernel/driver controls | Runtime adapter and safe fallback |
| Boot/shutdown | Image/init recipes | eMMC slots, `STARTUP`, `hd51_4.boxmode` | Preserve multiboot layout; no flash tests yet |

The common Neutrino core remains shared. Only the hardware contract and
vendor-backed HAL are target-specific; no No-DVB core is introduced.
