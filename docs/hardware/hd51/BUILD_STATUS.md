# HD51 build status

Status: **experimental; Wrynose metadata path prepared, image not yet built**.

## Implemented

- `conf/machine/hd51.conf` selects the verified OE-Alliance provider contract.
- `kas/hd51-wrynose.yml` pins BitBake/OE-Core, official support layers,
  OE-Alliance `6.0`, and separate HD51 download/sstate directories.
- `MACHINEBUILD=mutant51` selects the Mut@nt HD51 identity inside the
  `meta-gfutures` OEM mapping.
- `scripts/setup-build hd51` selects the dedicated manifest.
- `scripts/preflight --quick --machine hd51` validates the manifest pin.

## Verified tests

- repository static layer validation: passed;
- shell syntax and `git diff --check`: passed;
- source repository and manifest revision checks: passed;
- KAS manifest validation/dump: passed;
- KAS checkout of all pinned repositories: passed;
- proprietary source URLs and recipe checksums: inspected, not downloaded.
- The central wrapper now prints and executes `bitbake -k neutrino-image`.

## Not yet verified

- BitBake metadata parse: reached the BitBake host preflight but is blocked by
  missing host tools `python`, `sshpass`, `svn`, `chrpath`, `diffstat` and
  `gawk`; non-interactive sudo was unavailable;
- Full-image attempt: command `bitbake -k neutrino-image` was invoked through
  KAS and exited 1 before task execution for the same host-tool blocker.
- proprietary driver fetch/license acceptance: not performed;
- kernel, Neutrino cross-compilation and image creation: not performed;
- hardware boot, OSD, DVB lock, audio/video and recovery: not performed.

The first real build must run with `bitbake -k neutrino-image`,
`BB_NUMBER_THREADS=2` and `PARALLEL_MAKE=-j2`, through the existing watchdog.
