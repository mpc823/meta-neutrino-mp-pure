# HD51 porting history

1. The real Mut@nt HD51 was inventoried read-only: BCM7251S, ARMv7, Linux
   4.10.12, eMMC multiboot and proprietary `/proc/stb`/Nexus interfaces.
2. An experimental local MACHINE contract was added without fake kernel or
   driver recipes.
3. Historical Gatesgarth `meta-hd51` and OpenPLi Scarthgap were compared.
4. OE-Alliance `6.0` was then verified as the practical current source because
   its `meta-gfutures` layer explicitly supports Wrynose.
5. The dedicated `kas/hd51-wrynose.yml` path now pins that source. The next
   step is metadata parsing and a controlled keep-going build; no receiver
   write operation is part of this port.
