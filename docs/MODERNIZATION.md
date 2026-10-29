# Modernization status and build contract

The selected base is Yocto Project 6.0 LTS (Wrynose), using the official
`yocto-6.0` revisions of BitBake 2.18, OE-Core and meta-yocto, plus matching
Wrynose branches of meta-openembedded and meta-raspberrypi. Wrynose is
supported until 2030-04; Scarthgap 5.0 is the fallback if a vendor BSP needs it.

The historical layer has no machine configuration, uses pre-Honister overrides,
`AUTOREV`, insecure/obsolete source transports, FFmpeg 1.x/2.x recipes and
binary-only dependencies. Those are migration items, not build successes.

`scripts/setup-build raspberrypi2` keeps downloads, sstate and build output
outside Git. Each machine requires its own `bitbake neutrino-image` result and,
where hardware exists, boot/log/package smoke tests. A Raspberry Pi result is
never copied to another platform's status.

## Current execution (2026-10-28)

The first real Wrynose/BitBake 2.18 build for `raspberrypi2` is running on the
Linux-Mint host. It has passed layer parsing, dependency resolution, host-tool
setup and is compiling the native toolchain. The build uses fixed layer commits
and fixed Git revisions for all six former `AUTOREV` recipes plus the migrated
xupnpd source. Final image, boot and hardware smoke-test results are still
pending.

The host has no root permission, so `chrpath`, `diffstat`, `gawk` and gawk's
`libsigsegv` dependency are installed in a separate user-owned host-tools
directory by the build wrapper. No generated downloads, sstate or build output
are stored in Git.
## Unbeaufsichtigter Buildbetrieb

Der Raspberry-Pi-2-Referenzbuild kann dauerhaft über den Watchdog gestartet und
abgefragt werden:

```text
scripts/build-watchdog start raspberrypi2
scripts/build-watchdog status raspberrypi2
```

Für unbeaufsichtigte Läufe ist zusätzlich der persistente User-Service
`neutrino-build-watchdog.service` aktiviert. User-Linger hält ihn auch nach
Abmeldung aktiv; der Dienst übernimmt einen bereits laufenden Wrapper, startet
keinen zweiten BitBake-Prozess und prüft alle fünf Minuten den Live-Status.

Der Watchdog verwendet `systemd-inhibit` gegen automatisches Suspendieren,
überwacht die Wrapper- und BitBake-PIDs, schreibt Logs unter
`.neutrino-build/monitor/raspberrypi2/` und startet unerwartet beendete Läufe
mit Cache höchstens dreimal neu. Identische Fehler ohne Fortschritt oder nicht
sicher automatisierbare Rezeptfehler werden nur innerhalb der ausdrücklich versionierten
`repair_known_failure`-Regeln erneut gestartet. Unbekannte oder wiederholte Fehler führen
nach höchstens drei Neustarts zu `BLOCKIERT`; es gibt keine blinde Endlosschleife.

Der Watchdog überwacht den echten Wrapper-/BitBake-Prozess, Exit-Code, Logfortschritt,
Ressourcen und verwaiste PIDs. Prozessende wird standardmäßig innerhalb von zwei Sekunden
erkannt, während Fortschrittsmeldungen höchstens alle fünf Minuten erscheinen. `BLOCKIERT`
und `ERFOLGREICH` sind persistente Zustände; ein manueller `start` setzt einen solchen
Nachtlauf bewusst neu.

Wichtige Grenze: Nach Ende einer Codex-Sitzung kann der Watchdog nur überwachen, benachrichtigen
und einen unerwartet abgebrochenen Build mit vorhandenem Cache neu starten. Er kann unbekannte
Quellcode- oder Rezeptfehler nicht selbst analysieren oder dauerhaft reparieren. Solche Fehler
werden sicher als `BLOCKIERT` gemeldet und müssen in einer aktiven Codex-/Administratorsitzung
behoben, gezielt getestet und danach neu gestartet werden.
