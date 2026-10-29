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

Der vorhandene Generic-PC-Code ist kein vollständig isolierter Hostbuild:

- `generic-pc/hardware_caps.c` ist ohne externe Medienbibliotheken nativ
  übersetzbar.
- `generic-pc/playback.cpp` ist ebenfalls als HAL-Teil übersetzbar, bleibt
  aber ein Dummy-/Diagnosepfad.
- `generic-pc/video.cpp` benötigt FFmpeg-Entwicklungsheader und
  OpenThreads; `audio.cpp` benötigt zusätzlich libao. `glfb.cpp` benötigt
  OpenGL/GLUT/GLEW und OpenThreads. Der headless Generic-PC-Zweig baut ohne
  den optionalen GLFB-Zweig; echte Fenster-/GL-Ausgabe ist damit weiterhin
  nicht belegt.

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
werden nicht verändert. GLUT/GLEW bleiben für den headless Softwarepfad außen
vor; der produktive OpenGL-Zweig wird nicht entfernt.

## Master-TODO

- [x] Native Generic-PC-Headless-Bibliothek mit FFmpeg 6.1.1 kompilieren.
- [x] FFmpeg-Kompatibilität versionsabhängig und ausschließlich im Generic-PC-
      Backend patchen.
- [x] Headless-, Core- und ASan/UBSan-Tests reproduzierbar ausführen.
- [ ] Vollständigen Neutrino-Core als native x86-64-Anwendung linken.
- [ ] Native headless Startsequenz ohne DVB-Gerät und ohne Desktop validieren.
- [x] Native pugixml-Compilerprobe und reproduzierbare Syntaxprobe des echten
      `src/neutrino.cpp`-Einstiegs ergänzen.
- [ ] Native libcurl-Entwicklungsabhängigkeit bereitstellen und den Configure-
      Pfad erneut prüfen (`libcurl4-openssl-dev` oder `libcurl4-gnutls-dev`).
- [ ] Optionalen OpenGL-Fensterpfad mit installierten GLUT/GLEW-Headern testen.

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
behauptet.

Der erste native Configure-Versuch legt den nächsten echten Host-Blocker offen:
`configure` verlangt `libcurl.pc`, im System ist nur die Laufzeitbibliothek
installiert (`curl-config` ist ebenfalls nicht vorhanden). Der reproduzierbare
Ubuntu-Paketname ist `libcurl4-openssl-dev` (alternativ
`libcurl4-gnutls-dev`); ohne eines dieser Pakete darf der Full-Core-Build nicht
als erfolgreich bezeichnet werden. Zusätzlich muss für den späteren Link ein
vollständiges natives `libstb-hal`-Archiv erzeugt werden; ein HAL-Testarchiv oder
eine leere Bibliotheksattrappe wäre kein CNeutrinoApp-Build.

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
4. **Grafikausgabe/Hardwarebeschleunigung:** nicht getestet; es gibt keinen
   Desktop- oder DRM/KMS-Startpfad.

Ein natives Full-Core-Buildziel ist der nächste separate Schritt. Dafür müssen
die vorhandenen Host-Entwicklungsabhängigkeiten reproduzierbar bereitgestellt
werden; sie werden nicht ungeprüft in das Receiver-Image übernommen. Ein
optional späteres Yocto-x86-64-Image sollte erst nach dem nativen Core-/HAL-
Build als eigener MACHINE-/BSP-Schritt folgen.

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

