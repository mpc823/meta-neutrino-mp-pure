# HAL-Modernisierungsplan

Stand: 2026-10-29. Dieses Dokument beschreibt die erste additive
Modernisierungsstufe nach der Plattformbestandsaufnahme. Legacy-Backends und
der Raspberry-Pi-OpenMAX-Pfad bleiben unverändert aktiv.

## Bestand und Entscheidung

Die bestehende Abstraktion ist in `libstb-hal/include` bereits brauchbar:

- `cVideo`, `cAudio`, `cDemux`, `cPlayback` und `cRecord` bilden den
  gemeinsamen Gerätevertrag.
- `hardware_caps.h` liefert bisher nur Anzeige-, Gehäuse- und Energie-
  Eigenschaften.
- Der konkrete Backendpfad wird über `--with-boxtype` und `--with-boxmodel`
  gewählt. Raspberry Pi 2 verwendet `generic/raspi`, nicht einen neuen HAL.
- Neutrino bindet diese Verträge über `src/zapit/include/{audio,video,dmx}.h`
  und `src/driver/framebuffer.h`, `record.h`, `streamts.h` ein.

Der erste Baustein erweitert deshalb nur `hw_caps_t` um additive Bitmaps für
Video- und Displayfähigkeiten. Die alten Felder bleiben an gleicher Position;
neue Felder stehen am Ende der Struktur. Ein Backend darf nur Fähigkeiten
melden, die sein ausgewählter Codepfad grundsätzlich bereitstellt. Das ist
keine erfolgreiche Hardware- oder Codecprobe.

Gemeldet werden derzeit konservativ:

- Raspberry Pi: vorhandene OpenMAX-Hardwarebeschleunigung, H.264 und
  OpenGL/Framebuffer-Ausgabe. MPEG-2 wird nicht pauschal behauptet, weil die
  Pi-Unterstützung von Lizenz, Firmware und Laufzeitkonfiguration abhängt.
- Generic PC: vorhandene Framebuffer/OpenGL-Ausgabe. Kein Fenster-, DRM/KMS-
  oder Decoderfähigkeitsbit wird vorweggenommen.
- Coolstream, Spark, Spark7162, Triple Dragon und AZbox: neue Felder bleiben
  null, bis die jeweiligen externen Treiber/BSPs und reale Hardware geprüft
  sind. Dadurch wird kein Legacyverhalten verändert.

## Raspberry-Pi-2-Treiberbefund

Der verwendete Buildkernel ist im vorhandenen Buildtree Linux 6.18.33. Die
generierte Konfiguration enthält unter anderem:

- `CONFIG_MEDIA_SUPPORT=m`, DVB-Core und V4L2-Memory-to-Memory-Unterstützung;
- `CONFIG_VIDEO_BCM2835_UNICAM` als Kamerapfad, nicht als TV-Decoder;
- `CONFIG_DRM=m`, `CONFIG_DRM_KMS_HELPER=m` und `CONFIG_DRM_VC4=m`;
- die Buildkonfiguration entfernt jedoch `vc4graphics` aus den Machine
  Features und verwendet `userland` für EGL/GLES.

Damit sind V4L2 und DRM/KMS im Kernel grundsätzlich vorhanden, aber im
aktuellen Neutrino-Image nicht als Medienpipeline aktiviert. Der aktive
Userspace nutzt weiterhin OpenMAX/VideoCore und Framebuffer/GL. MPEG-2- und
H.264-Chipfähigkeit, Kernelzugriff, Firmware/Lizenz und tatsächlich getestete
Wiedergabe müssen getrennt bewertet werden. Für Pi 1 darf aus Pi-2-Ergebnissen
nichts automatisch abgeleitet werden; dort sind SoC-, Firmware- und BSP-
Unterschiede gesondert zu prüfen.

## Moderne Alternativen

GStreamer könnte Demux, Decoder und Clock vereinheitlichen, ersetzt aber weder
OSD noch DRM/KMS oder ein Desktopfenster. V4L2 ist ein Linux-Gerätevertrag für
bestimmte Decoder/Capture-Pfade und kein vollständiger Mediengraph. DRM/KMS
regelt Display, Buffer, Planes und Modesetting, aber weder Demux noch Audio.
Wayland/X11/SDL wäre zusätzlich für den Fensterbetrieb nötig. Keine dieser
Abhängigkeiten wird in dieser Stufe in das produktive Image aufgenommen.

## Migrationsschritte

1. Capability-Bitmap kompilieren und in den vorhandenen Backends neutral
   initialisieren.
2. Für jede Plattform reale Capabilitywerte anhand von BSP, Gerätedateien,
   Treibern und Laufzeittests nachtragen; keine Chipfähigkeit aus Dateinamen
   ableiten.
3. RPi-2-Referenztests für H.264, MPEG-2 mit/ohne Lizenz, A/V-Sync, OSD,
   Bildkopien, CPU/RAM und Decoder-Latenz erfassen.
4. Einen getrennten PC-Fensterprototypen evaluieren, ohne den Receiverpfad
   umzuschalten.
5. Erst danach optional Decoder/Clock/VideoSink/OSD/Window als getrennte
   moderne Adapter definieren. Legacy-HALs bleiben Adapterziele.

## Keep-Going-Aufruf und Fehleraggregation

Der zentrale Wrapper verwendet bereits `exec bitbake -k neutrino-image`;
mit gesetztem `NEUTRINO_BITBAKE_BIN` lautet er
`$NEUTRINO_BITBAKE_BIN -k neutrino-image`. `-k` ist BitBakes
Keep-Going-Option. `--continue` wird nicht zusätzlich gesetzt, weil es eine
andere Fortsetzungssemantik hat. Es gibt weiterhin genau einen Buildprozess;
der Wrapper weist einen zweiten `neutrino-image`-BitBake ab. Erfolgreiche
Tasks und SState bleiben erhalten.

Bei einem nicht erfolgreichen Lauf schreibt der Wrapper neben dem vollständigen
Run-Log eine deduplizierte Fehlerliste `<run>.log.failures`. Der Exitcode
bleibt maßgeblich; `-k` darf niemals einen unvollständigen Image-Build als
Erfolg melden. Die Fehlerliste ist die gemeinsame Eingabe für die spätere
Ursachenanalyse.

## Native x86-64-Headless-Stufe

### Verbindliche Architekturtrennung

Alle nativen und Yocto-Target-Artefakte müssen vor Verwendung und Auslieferung
gegen das tatsächliche Compiler-Target geprüft werden. Die zentrale Logik liegt
in `scripts/check-build-architecture`; sie ermittelt das Target mit
`compiler -dumpmachine`, prüft ELF-Machine, ELF-Klasse und Endianness und
untersucht bei `.a`-Archiven jedes enthaltene Objekt. Nicht-ELF-Dateien werden
als legitime Daten/Firmware übersprungen.

Die nativen Wrapper `native-generic-pc` und `native-x86-headless` rufen diese
Prüfung verbindlich auf. Für Yocto wird sie über
`classes/neutrino-architecture-check.bbclass` in `libstb-hal` und
`neutrino-mp` vor und nach `do_compile` sowie vor `do_package` ausgeführt.
Damit werden veraltete Objekte mit falscher Architektur vor dem Link erkannt.
Native PC-Arbeitskopien müssen frisch erzeugt und von alten Yocto-Objekten
bereinigt werden; ein kopierter In-Source-Baum darf nicht als sauber gelten.

Der dauerhafte Negativtest ist:

```sh
NEUTRINO_ARCH_WRONG_CC=/pfad/zum/fremden-cross-gcc \
  ./scripts/check-build-architecture --self-test --compiler c++ /bin/true
```

Auf dem Referenzhost wurde damit ein gemischtes x86-/ARM-Archiv abgelehnt.
Für SH4, MIPS, PowerPC und historische ARM-Ziele ist die gemeinsame Prüfung
vorbereitet, aber mangels installierter Toolchains nicht als echte Plattform-
Prüfung ausgegeben.

Der vorhandene Generic-PC-Code ist kein vollständig isolierter Hostbuild:

- `generic-pc/hardware_caps.c` ist ohne externe Medienbibliotheken nativ
  übersetzbar.
- `generic-pc/playback.cpp` ist ebenfalls als HAL-Teil übersetzbar, bleibt
  aber ein Dummy-/Diagnosepfad.
- `generic-pc/video.cpp` benötigt FFmpeg-Entwicklungsheader und
  OpenThreads; `audio.cpp` benötigt zusätzlich libao. `glfb.cpp` benötigt
  OpenGL/GLUT/GLEW und OpenThreads. Der headless Generic-PC-Zweig baut ohne
  den optionalen GLFB-Zweig; der separate Full-Core-GUI-Smoke-Test deckt den
  produktiven GLFB-/OpenGL-Pfad auf dem X11-Host ab.

Der reproduzierbare erste Testpfad ist:

```sh
cd ~/Projekte/neutrino/meta-neutrino-mp-pure
./scripts/native-x86-headless
```

Optional kann der entpackte, auf feste `LIBSTB_HAL_SRCREV` bezogene HAL-Quellbaum
explizit gewählt werden:

```sh
NEUTRINO_LIBSTB_HAL_SOURCE=/pfad/zu/libstb-hal \
  ./scripts/native-x86-headless
```

Mit dem vorhandenen Yocto-Quellbaum prüft dasselbe Skript zusätzlich den
gemeinsamen Neutrino-Core (`lib/libconfigfile/configfile.cpp` und
`src/driver/abstime.c`). Der Pfad kann reproduzierbar gesetzt werden:

```sh
NEUTRINO_CORE_SOURCE=/pfad/zu/neutrino-mp ./scripts/native-x86-headless
NEUTRINO_SANITIZE=1 ./scripts/native-x86-headless
./scripts/native-generic-pc
```

Der zweite Befehl führt den isolierten Headless-Test zusätzlich mit
ASan/UBSan aus. `native-generic-pc` wendet den FFmpeg-Kompatibilitätspatch
mit `--fuzz=0` in einer temporären Kopie an, kompiliert alle sieben
Generic-PC-C++-Einheiten plus `hardware_caps.c` und erzeugt ein statisches
Headless-Archiv. Die Host-Pakete sind auf dem geprüften System installiert:
FFmpeg 6.1.1, OpenThreads 3.6.5, libao 1.2.2, sigc++ 2.12.1 und Freetype
2.13.2. Der frühere Installationsbefehl bleibt zur Reproduktion dokumentiert:

```sh
sudo apt-get update
sudo apt-get install libsigc++-2.0-dev libfreetype-dev libpng-dev libjpeg-dev \
  libavformat-dev libavcodec-dev libavutil-dev libswresample-dev libswscale-dev \
  libopenthreads-dev libao-dev liblua5.2-dev
```

Die FFmpeg-Anpassung liegt in
`recipes-neutrino/libstb-hal/libstb-hal/0003-generic-pc-ffmpeg6-compat.patch`.
Sie betrifft ausschließlich `generic-pc/*`, verwendet für alte FFmpeg-Majors
weiterhin die historischen Decoderaufrufe und schaltet `av_register_all()` nur
für alte Versionen frei. Raspberry-Pi-, SH4-, MIPS- und sonstige Legacy-Dateien
werden nicht verändert. Für den vollständigen nativen Link werden zusätzlich
die gemeinsamen `common`-HAL-Objekte und der produktive OpenGL-Zweig mit
GLUT/GLEW gebaut. Der OpenGL-Zweig bleibt optional; der headless Softwarepfad
wird dadurch nicht ersetzt.

## Master-TODO

- [x] Native Generic-PC-Headless-Bibliothek mit FFmpeg 6.1.1 kompilieren.
- [x] FFmpeg-Kompatibilität versionsabhängig und ausschließlich im Generic-PC-
      Backend patchen.
- [x] Headless-, Core- und ASan/UBSan-Tests reproduzierbar ausführen.
- [x] Vollständigen Neutrino-Core als native x86-64-Anwendung linken.
- [x] Native GUI-Startsequenz ohne DVB-Gerät mit isoliertem Zustand und
      kontrolliertem Shutdown validieren; der separate headless Bootstrap
      bleibt weiterhin erhalten.
- [x] Native pugixml-Compilerprobe und reproduzierbare Syntaxprobe des echten
      `src/neutrino.cpp`-Einstiegs ergänzen.
- [x] Native libcurl-Entwicklungsabhängigkeit und Configure-Pfad prüfen.
- [x] OpenGL-/GLUT-/GLEW-Pfad kompilieren, linken und ELF-Architektur prüfen.
- [ ] OpenGL-Fenster unter einem realen X-Server oder Xvfb starten.

## Native-Neutrino-Headless-Bootstrap

`./scripts/native-neutrino-headless` baut und startet einen echten kleinen
Prozess aus `lib/libconfigfile/configfile.cpp`, `src/driver/abstime.c` und
`generic-pc/hardware_caps.c`. Er prüft Konfigurations-Roundtrip, Generic-PC-
Capabilities, ein Eingabeereignis über `poll()` sowie kontrollierten Ablauf
über `timerfd`. Mit `NEUTRINO_SANITIZE=1` wird derselbe Prozess zusätzlich mit
ASan/UBSan ausgeführt.

Das ist absichtlich kein vollständiger `CNeutrinoApp`-Start: dessen realer
Einstieg bindet GUI, Zapit, Daemons, DVB, Audio, Framebuffer und zahlreiche
receiverabhängige Bibliotheken ein. Der aktuelle Bootstrap beweist daher
Start/Shutdown und gemeinsame Core-/HAL-Bausteine, nicht bereits DVB-,
Video-, Audio-, OSD- oder vollständige Neutrino-Menüfunktion.

Die direkte Host-Syntaxprobe von `src/neutrino.cpp` bestätigt den realen
Einstieg (`main()` → `CNeutrinoApp::getInstance()` → `run()`). Der Konstruktor
initialisiert bereits den Framebuffer; `run()` ruft anschließend unter anderem
`cs_api_init()`, Locale/Fonts, VFD, Zapit, Audio, Video, RCInput und den
Timer-Daemon auf. `pugixml.hpp` ist inzwischen als native Host-Abhängigkeit
mit Version 1.14 verifiziert. `scripts/native-cneutrino-syntax` wiederholt
diese Prüfung reproduzierbar und verwendet dabei ausschließlich Host-Header;
die ARM-Yocto-Sysroot wird nicht verwendet. Ein vollständiger
CNeutrinoApp-Headlessstart ist deshalb weiterhin nicht erreicht und wird nicht
behauptet; der GUI-Start ist davon getrennt unter X11 verifiziert.

Der native Configure-Pfad verwendet inzwischen `libcurl4-openssl-dev` und
`libpugixml-dev`; beide sind per pkg-config und Compilerprobe verifiziert.
Für den Full-Core-Link wird ein vollständiges natives `libstb-hal`-Archiv aus
`common` und Generic-PC/OpenGL gebaut. Ein HAL-Testarchiv oder eine leere
Bibliotheksattrappe wird nicht als CNeutrinoApp-Build gewertet.

Das Skript kompiliert nativ `generic-pc/hardware_caps.c` und
`generic-pc/playback.cpp`, prüft die Capability-API und führt einen
deterministischen Software-OSD-Hash sowie einen headless Eingabeereignistest
über `poll()`/Pipe aus. Es erzeugt keine dauerhaften Testbilder. Der
deterministische Test ist ein Rendering-Regressionsvektor, nicht die echte
GLFB-Ausgabe.

Bewusst getrennte Statusklassen:

1. **Nativ kompiliert:** HAL-Teilobjekte und Testprogramm bauen auf x86-64.
2. **Headless bestanden:** Capability-, Software-OSD- und Pipe-Eingabetests
   laufen erfolgreich.
3. **Video/Audio funktional:** aktuell nicht belegt; dafür fehlen Host-
   Entwicklungsheader und echte Medien-/Audioausgabe.
4. **Grafikausgabe/Hardwarebeschleunigung:** OpenGL/GLUT/GLEW-GLFB und OSD
   sind unter X11 gestartet; DRM/KMS sowie echte Receiver-Hardware bleiben
   ungetestet.

Das native Full-Core-Buildziel ist gelinkt; der kontrollierte Programmstart
erreicht die echte HAL-/OpenGL-Initialisierung. Ohne `DISPLAY` beendet GLUT den
Prozess korrekt mit „failed to open display“. Ein
optional späteres Yocto-x86-64-Image sollte erst nach dem nativen Core-/HAL-
Build als eigener MACHINE-/BSP-Schritt folgen.

Der reproduzierbare Full-Core-Link verwendet die Host-Pakete `libdvbsi++`,
`lua5.2`, `libswscale`, `libao`, `freeglut3-dev` und `libglew-dev`; entscheidend
ist die Make-Variablenform `make ... CPPFLAGS=... LDFLAGS=... LIBS=...`, da das
Autotools-Makefile ein Umgebungs-`LIBS` überschreiben kann. Danach prüft
`scripts/check-build-architecture` das gelinkte `src/neutrino` als x86-64.
Die Raspberry-Pi-2-Regression `bitbake -k -c compile neutrino-mp libstb-hal`
lief mit Exitcode 0 durch. Die FFmpeg-Kompatibilität verwendet für historische
Toolchains versionsabhängige `AVCodec`- und `AVOutputFormat`-Typen; der
OpenMAX-/Legacy-Pfad bleibt unverändert.

## Native GUI-Startstufe

Der native Full-Core ist inzwischen tatsächlich bis zum GUI-Zustand gestartet:
GLFB initialisiert den OpenGL/OSD-Puffer, Neutrino lädt Fonts und Logo,
`rcsim` liefert Menü-/Exit-Eingaben über den produktiven Event-Socket und
`/control/shutdown` beendet die Anwendung regulär. Das ist ein echter
`CNeutrinoApp`-Lauf, aber noch kein Nachweis für DVB-Tuner, Live-Video oder
Audio auf dem PC.

### Tunerzustände und PCTV 461e

`CZapit::Start()` benötigt für den historischen Live-Pfad ein Frontend. Ohne
`/dev/dvb/adapter*/frontend0` liefert der Frontend-Manager `false`; das war die
konkrete Ursache des früheren fatalen Dialogs, nicht ein fehlender Zapit- oder
HTTP-Socket. Der gemeinsame Core behandelt diesen Zustand zur Laufzeit als
fehlende Empfangsquelle: GUI, OSD, Timer, HTTP und `CStreamManager` bleiben
nutzbar. Der gemeinsame EIT-Dienst darf starten und idle bleiben; die eigentliche
DVB-Suche beziehungsweise das Tuning benötigt eine betriebsbereite Quelle.
`HAVE_GENERIC_HARDWARE` kennzeichnet ausschließlich das
Generic-PC-HAL/Plattformziel; es erzeugt keine zweite DVB-Programmvariante.
`NEUTRINO_PC_NO_DVB=0` ist nur ein Test-Override für die Laufzeitprüfung. Der
native GUI-Smoke-Marker wird explizit per `fflush(stdout)` veröffentlicht; zuvor
konnte der Prozess bereits weiterlaufen, während der Marker wegen Datei-
Pufferung unsichtbar blieb. Die aktuelle Diagnose grenzt einen verbleibenden
Startstau auf die Pre-Menu-Initialisierung ein; ein Status 130 stammt dabei vom
kontrollierten Testabbruch, nicht von BitBake oder einem ungeplanten Neutrino-
Exit. Die
Receiver-Builds behalten ihre historischen Plattformpfade und erhalten keine
PC-Sonderlogik.

Der Entwicklungs-PC erkennt den PCTV Systems DVB-S2 Stick 461e (`2013:0258`)
über `em28xx`, `em28xx_dvb` und `m88ds3103`; die Kernelgeräte existieren. Die
Firmware ist inzwischen unter `/lib/firmware` installiert; nach USB-
Neuinitialisierung meldet der Kernel Version `3.B`. Damit ist die
Firmwareinitialisierung nachgewiesen, aber noch kein Empfangstest. Das Ubuntu-
`linux-firmware`-Paket enthält diese Datei auf dem Host nicht. Hauppauge nennt
für den alten 461e ausdrücklich die 3103-/3103b-Firmware; die passende 3103-Datei
ist 8192 Bytes groß und wurde aus der von Hauppauge referenzierten OpenELEC-
Firmwarequelle verifiziert. Die Installation bleibt eine bewusst manuelle
Hostaktion nach Prüfung der SHA-256-Summe; die Firmware ist kein Projektartefakt.
Für den auf diesem Host identifizierten alten Stick ist die geprüfte Datei
`dvb-demod-m88ds3103.fw`, 8192 Bytes, SHA-256
`4767ab80ceba4a66315cbec2a07ae1f7ebbd19c5758fd098b932e02c9108eff9`.
Ein direkter Frontend-Probe öffnet `/dev/dvb/adapter0/frontend0`, listet DVB-S
und DVB-S2 und liefert ohne bekannte Satellitenparameter
`FE_READ_STATUS=0x00` (`locked=no`); ein Lock wird daher nicht behauptet.

### Zapit-Initialisierung: nachgewiesene Ursache und Korrektur

Der frühere Dialog wird in `src/neutrino.cpp` in `CNeutrinoApp::run()` durch
`DisplayErrorMessage("Zapit initialization failed...")` ausgelöst, wenn
`CZapit::Start()` false liefert. Die konkrete historische Ursache lag in
`src/zapit/src/zapit.cpp`, Funktion `CZapit::Start()`: Nach
`CFEManager::getInstance()->Init()` wurde `live_fe = getFE(0)` gesetzt und bei
`live_fe == NULL` sofort `false` zurückgegeben. Dieser Rückweg lag vor
`zapit_server.prepare(ZAPIT_UDS_NAME)`. Ohne lokales Frontend entstand daher
kein `/tmp/zapit.sock`; das war ein Frontend-Voraussetzungsfehler, kein
Socket-Timeout und kein Zapit-Absturz.

Die Patchfolge korrigiert diesen Ablauf für `HAVE_GENERIC_HARDWARE` dauerhaft:
`0018` lässt Zapit ohne lokales Frontend in einen ausdrücklich protokollierten
Idle-Quellenzustand weiterlaufen, damit der Dienst seinen Socket und Thread
bereitstellt. Historische Receiver behalten den ursprünglichen Rückweg.
`0019` entfernt eine versehentlich verbliebene unbedingte Dialogzeile in der
vorherigen Generic-PC-Patchfolge. `0020` schützt die Initialisierung vor einer
leeren Senderliste, `0021` verhindert spätere Nullzeigerdereferenzierungen bei
Tastenpfaden wie Numeric-/Quick-Zap und Info. Diese Korrekturen ändern nicht
den gemeinsamen DVB-Core und erzeugen keine No-DVB-Kompilierungsvariante.

Mit angeschlossenem PCTV-461e wurden Zapit-Socket, `menue setup`,
`[neutrino] initialized everything`, `KEY_MENU`, `KEY_EXIT` und der reguläre
HTTP-Shutdown nachgewiesen. Der kontrollierte Generic-PC-Test mit
`NEUTRINO_PC_NO_DVB=1` erzwingt den Laufzeitpfad ohne lokale Senderliste, ohne
das physische Gerät zu entfernen; deshalb ist er ein deterministischer
Laufzeit-/GUI-Test, aber kein Beweis für einen realen Geräteausbau. Ein echter
Satellitenlock bleibt wegen fehlender Transponder-/LNB-Parameter offen.

Der GUI-Smoke-Test nimmt nach `KEY_MENU` zusätzlich das tatsächliche GLUT-
Fenster mit `xwininfo`/`xwd` auf und prüft XWD-Format, Fenstername, Auflösung
und eine plausible Dateigröße. Damit ist die sichtbare X11-Fensterausgabe
getrennt von bloßen Startmarkern nachweisbar; die temporäre Aufnahme bleibt
außerhalb des Git-Arbeitsbaums.

## Preflight und Clean-Room-Reproduzierbarkeit

`scripts/preflight --quick` prüft Werkzeuge, native pkg-config-Module,
Python/YAML, KAS-Pins, Layerregeln, lokale `SRC_URI`-Dateien sowie Speicher,
Inodes, RAM und Swap. `--full` initialisiert zusätzlich die vorhandene
Yocto-Quelle und führt `bitbake -p neutrino-image` aus; `--fetch` ergänzt
`bitbake -c fetch neutrino-image` einschließlich Prüfsummen-/Fetchprüfung.
Die Prüfung verändert keine Quelltexte und startet keinen Build.

`scripts/bootstrap-ubuntu-24.04 [--install]` dokumentiert beziehungsweise
installiert die Ubuntu-24.04-Hostpakete, erzeugt eine isolierte KAS-Python-
Umgebung und startet anschließend den vollständigen Preflight. Eine echte
frische Ubuntu-VM beziehungsweise ein Clean-Room-Image wurde auf dem
Entwicklungs-PC noch nicht ausgeführt; der aktuelle Nachweis stammt daher
weiterhin von Linux Mint 22.3 und ist kein Clean-Room-Buildnachweis.

Die Native-PC-Smoke-Prüfung deckt nun neben X11-Fenster, OSD, Eingaben und
Shutdown auch die hardwareunabhängigen HTTP-Endpunkte `getchannel` und
`getbouquets` ab. Ohne lokalen Tuner werden dabei keine Empfangsfunktionen
vorgetäuscht.

## Zukünftige Plattformen

AX/Mut@nt HD51 und NVIDIA Shield TV sind als mögliche spätere HAL-Ziele
vorgemerkt. Es existieren dafür weder aktive MACHINE-Konfigurationen noch
Portierungen oder Firmwareänderungen.
Die `m88ds3103c`-Datei (16384 Bytes) gehört zu einer anderen Chiprevision und
wird für USB-ID `2013:0258` nicht verwendet.

Langfristig werden Tuner als Ressourcen modelliert: lokal betriebsbereit,
vorhanden aber nicht betriebsbereit, keine Quelle, SAT>IP/Remote-Tuner und
IPTV. Ein Remote-Tuner mit Erkennung, Capability-Austausch, Reservierung,
Transportstream und Reconnect ist ein separater späterer Meilenstein und wird
hier zunächst nur architektonisch vorgemerkt.

## HD51-BSP-Befund

Das reale Gerät `hd51` ist ein Broadcom-`bcm7251s`-Receiver mit ARMv7 und
Linux 4.10.12. Die vorhandene gemeinsame HAL und der Neutrino-Core bleiben
die Integrationsbasis; ein fehlender lokaler oder proprietärer Treiber darf
keine zweite No-DVB-Variante erzeugen. Die MACHINE-Vorbereitung liegt in
`conf/machine/hd51.conf`, die generische HAL-Auswahl wird mit dem Boxmodell
`hd51` vorbereitet.

Das passende OpenPLi-`meta-gfutures`-BSP liefert jedoch nicht nur eine
MACHINE-Datei, sondern auch einen gepatchten `linux-hd`-Kernel, binäre
`hd-dvb-modules-hd51`- und `hd-v3ddriver-hd51`-Pakete sowie die eMMC-/Multiboot-
Imageklassen. Das geprüfte BSP deklariert Scarthgap, während dieses Repository
Wrynose-Metadaten verwendet. Deshalb verweigert `scripts/setup-build hd51`
den Start momentan ausdrücklich. Ein Parse- oder Imageerfolg wäre ohne eine
kompatible Layerbasis irreführend.

Die nächste sichere Reihenfolge ist: BSP-Releasebasis festlegen, Layer und
Quellen reproduzierbar pinnen, `bitbake -p` und Fetch-Prüfung ausführen, dann
den gemeinsamen Core und die HD51-HAL bauen. Erst danach folgen Hardwaretests
über einen erhaltenen Multiboot-Slot. Es wurden keine proprietären Dateien,
Passwörter oder Receiveränderungen in das Projekt übernommen.

Der reproduzierbare Test `scripts/native-gui-smoke` verlangt eine installierte
Full-Core-Binary in `NEUTRINO_NATIVE_BIN`, prüft deren x86-64-ELF sowie `rcsim`
mit der zentralen Architekturprüfung, validiert die OpenGL/X11-Anzeige, startet
mit isoliertem HOME/XDG-Zustand, sendet `KEY_MENU` und `KEY_EXIT` und fordert
anschließend den vorhandenen HTTP-Control-Endpunkt `/control/shutdown` an.
Ein Exitcode kleiner 128 zusammen mit GL-Thread- und Streaming-Shutdown gilt
als kontrolliertes Beenden; der native Prozess liefert dabei aktuell Exitcode 1
aus `ExitRun(1)`, was kein Signalabbruch ist.

## Testkriterien

- API-Header kompiliert mit C11, `-Wall -Wextra -Werror`.
- `./scripts/native-x86-headless` kompiliert den Generic-PC-HAL-Teil nativ und
  besteht die Capability-, Software-OSD- und Eingabetests.
- `libstb-hal:do_compile` und anschließend `neutrino-mp:do_compile` laufen
  für Raspberry Pi 2 durch.
- OpenMAX-Objekte und die bisherige `generic/raspi`-Auswahl bleiben im
  Buildlog erhalten.
- Keine neuen GStreamer-, V4L2- oder DRM/KMS-Laufzeitabhängigkeiten.
- Vollständiger `bitbake -k neutrino-image` endet mit Exitcode 0; Run-ID,
  Log und Imageartefakte werden über Wrapper/Watchdog verifiziert.
- Auf echter Hardware folgen Video-, Audio-, OSD-, DVB-, Aufnahme-, Timeshift-
  und Standby-Tests. Diese sind durch einen Yocto-Build nicht ersetzt.

## Risiken

Die Erweiterung der Struktur erfordert, dass alle Backendquellen mit dem neuen
Header neu kompiliert werden. Unveränderte externe Coolstream-Bibliotheken
können die Felder ignorieren. Die Bitmaps sind bewusst optional und werden
nicht zur automatischen Auswahl eines Backends verwendet. Das verhindert
Fehlentscheidungen durch unvollständige Hardwareerkennung.

