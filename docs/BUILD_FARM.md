# Neutrino-Buildfarm

Der ThinkCentre bleibt führende Arbeitskopie. Der iMac (`192.168.0.68`) ist
ein separater Build-Arbeiter. Synchronisation und Aufträge verwenden den
bestätigten SSH-Hostschlüssel und `~/.ssh/id_ed25519_imac_build`.

## Speicher und Versionen

Auf dem iMac liegen Quellen unter `/srv/neutrino-build/work/meta-neutrino-mp-pure`,
Builddaten unter `/srv/neutrino-build/work/.neutrino-build`, Logs unter
`/srv/neutrino-build/jobs` und Maschinen-Locks unter
`/srv/neutrino-build/locks`. Die KAS-Dateien pinnen BitBake und alle
OE-/BSP-Layer auf feste Revisionen.

Externe Layerverzeichnisse aus dem ThinkCentre-Arbeitsbaum werden nicht kopiert.
KAS holt sie anhand der festgeschriebenen Revisionen. Uncommittete Änderungen
werden nur mit `--working-tree` übertragen; ohne diese Option verweigert der
Sync einen Dirty-Worktree.

## Synchronisieren und testen

```sh
scripts/buildfarm-sync --dry-run
scripts/buildfarm-sync --working-tree
```

Der Working-Tree-Modus ist für den aktuellen lokalen Entwicklungsstand gedacht.
Vor reproduzierbaren Release-Builds muss der Worktree sauber sein und der Sync
ohne `--working-tree` erfolgen. Der Sync schreibt Commit, Branch und Dirty-Status
in `.buildfarm/manifest.txt` auf dem Arbeiter.

Der absichtliche Fehlertransport wird so geprüft:

```sh
ssh -i ~/.ssh/id_ed25519_imac_build codex@192.168.0.68 \
  /srv/neutrino-build/bin/buildfarm-runner fail-test
ssh -i ~/.ssh/id_ed25519_imac_build codex@192.168.0.68 \
  /srv/neutrino-build/bin/buildfarm-runner status
```

Der erste Befehl muss mit Exitcode 42 und `INTENTIONAL_BUILD_FAILURE` im Log
enden. Ein echter Auftrag wird erst danach gestartet:

```sh
ssh -i ~/.ssh/id_ed25519_imac_build codex@192.168.0.68 \
  /srv/neutrino-build/bin/buildfarm-runner run
```

Pro MACHINE gibt es nur einen `flock`; ein zweiter Auftrag wird abgewiesen.
Der Runner verwendet `bitbake -k`, `BB_NUMBER_THREADS=2` und
`PARALLEL_MAKE=-j2` aus der HD51-KAS-Konfiguration. Ein Exitcode 0 bedeutet
nicht automatisch ein gültiges Image; Deploy-Artefakte müssen zusätzlich geprüft
werden.

KAS 5.5 läuft isoliert unter `/srv/neutrino-build/kas-venv/bin/kas`. Die
externen Layer werden nicht aus dem ThinkCentre kopiert, sondern von KAS mit den
Revisionen aus `kas/hd51-wrynose.yml` bzw. `kas/wrynose.yml` geholt.
