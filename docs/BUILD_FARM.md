# Neutrino-Buildfarm

Der ThinkCentre bleibt die führende Arbeitskopie. Eine optionale externe
Buildfarm kann als separater Build-Arbeiter verwendet werden. Ihre Adresse,
Benutzerkennung und SSH-Identität gehören ausschließlich in eine lokale,
ignorierte Konfiguration (`.buildfarm/local.conf`) oder in
`NEUTRINO_BUILDFARM_*`-Umgebungsvariablen. Das öffentliche Repository enthält
keine konkrete Worker-Konfiguration.

## Speicher und Versionen

Auf dem Arbeiter liegen Quellen, Builddaten, Logs und Maschinen-Locks unter
den in der lokalen Worker-Konfiguration festgelegten Pfaden. Die KAS-Dateien
pinnen BitBake und alle
OE-/BSP-Layer auf feste Revisionen.

Externe Layerverzeichnisse aus dem ThinkCentre-Arbeitsbaum werden nicht kopiert.
KAS holt sie anhand der festgeschriebenen Revisionen. Uncommittete Änderungen
werden nur mit `--working-tree` übertragen; ohne diese Option verweigert der
Sync einen Dirty-Worktree.

## Synchronisieren und testen

```sh
# Lokale Workerwerte setzen oder .buildfarm/local.conf anlegen.
scripts/buildfarm-sync --dry-run
scripts/buildfarm-sync --working-tree
```

Der Working-Tree-Modus ist für den aktuellen lokalen Entwicklungsstand gedacht.
Vor reproduzierbaren Release-Builds muss der Worktree sauber sein und der Sync
ohne `--working-tree` erfolgen. Der Sync schreibt Commit, Branch und Dirty-Status
in `.buildfarm/manifest.txt` auf dem Arbeiter.

Der absichtliche Fehlertransport wird so geprüft:

```sh
ssh -i "$NEUTRINO_BUILDFARM_IDENTITY" \
  "$NEUTRINO_BUILDFARM_USER@$NEUTRINO_BUILDFARM_HOST" \
  "$NEUTRINO_BUILDFARM_ROOT/bin/buildfarm-runner" fail-test
ssh -i "$NEUTRINO_BUILDFARM_IDENTITY" \
  "$NEUTRINO_BUILDFARM_USER@$NEUTRINO_BUILDFARM_HOST" \
  "$NEUTRINO_BUILDFARM_ROOT/bin/buildfarm-runner" status
```

Der erste Befehl muss mit Exitcode 42 und `INTENTIONAL_BUILD_FAILURE` im Log
enden. Ein echter Auftrag wird erst danach gestartet:

```sh
ssh -i "$NEUTRINO_BUILDFARM_IDENTITY" \
  "$NEUTRINO_BUILDFARM_USER@$NEUTRINO_BUILDFARM_HOST" \
  "$NEUTRINO_BUILDFARM_ROOT/bin/buildfarm-runner" run
```

Pro MACHINE gibt es nur einen `flock`; ein zweiter Auftrag wird abgewiesen.
Der ThinkCentre verwendet das Profil `conf/buildfarm/thinkcentre.conf` mit
`BB_NUMBER_THREADS=2` und `PARALLEL_MAKE=-j2`. Der iMac-Runner verwendet
`conf/buildfarm/imac.conf` mit `BB_NUMBER_THREADS=6` und
`PARALLEL_MAKE=-j6`. Die Werte werden als zusätzliche BitBake-Konfiguration
mit `-R` gesetzt und gelten erst für neue Aufträge. Ein Exitcode 0 bedeutet
nicht automatisch ein gültiges Image; Deploy-Artefakte müssen zusätzlich geprüft
werden.

## Einheitlicher lokaler Einstieg

Das Repository stellt einen seriellen Einstieg über `Makefile` bereit:

```sh
make                 # alle aktivierten, verfügbaren Ziele
make config          # Plattformauswahl ändern und speichern
make show-config     # gespeicherte Auswahl anzeigen
make raspberrypi2    # Raspberry-Pi-2-Referenz
make hd51            # HD51, sofern BSP/Provider verfügbar
make coolstream      # Coolstream-Familie prüfen/builden
```

`Makefile` enthält keine eigene BitBake-Logik; es delegiert an
`scripts/setup-build`. Dadurch bleiben die zentrale `bitbake -k`-, KAS- und
Lock-Logik erhalten. Nicht verfügbare Familien werden vor dem Build mit dem
konkreten fehlenden BSP/Sysroot/Provider gemeldet. Eine Profilabfrage wird erst
erforderlich, wenn mehrere technisch inkompatible und tatsächlich buildbare
MACHINEs derselben Familie vorhanden sind. Tuner-, VFD- und HDD-Varianten sind
keine eigenen Core-Builds, sondern Laufzeit-Capabilities.

KAS läuft in einer isolierten Umgebung des jeweiligen Arbeiters. Die externen
Layer werden nicht aus dem ThinkCentre kopiert, sondern von KAS mit den
Revisionen aus `kas/hd51-wrynose.yml` bzw. `kas/wrynose.yml` geholt.
