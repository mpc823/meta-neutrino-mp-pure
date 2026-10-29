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

## Testkriterien

- API-Header kompiliert mit C11, `-Wall -Wextra -Werror`.
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

