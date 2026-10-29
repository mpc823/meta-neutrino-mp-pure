# Technische Bestandsaufnahme der Neutrino-MP-Pure-Hardwarearchitektur

Stand: 2026-10-29  
Gegenstand: Repository `meta-neutrino-mp-pure`, Branch `codex/yocto-modernize`  
Zweck: Architekturvergleich und belastbare Ausgangsbasis vor einer späteren Implementierung.

## Abgrenzung und Beleglage

Diese Datei ist ausschließlich eine Bestandsaufnahme. Es wurden keine Rezepte,
Plattformkonfigurationen oder Quelltexte geändert und kein neuer Build gestartet.
Die Untersuchung umfasst das Layer, die im erfolgreichen Raspberry-Pi-2-Build
entpackten Quellen von `neutrino-mp` und `libstb-hal` sowie die KAS-Konfiguration.

Die Aussagen sind gekennzeichnet:

- **Belegt**: direkt aus Dateiinhalt, Rezept, Buildkonfiguration oder Quelltext
  dieses Checkouts abgeleitet.
- **Abgeleitet**: technische Schlussfolgerung aus mehreren belegten Stellen.
- **Unverifiziert**: benötigt reale Hardware, einen passenden BSP oder einen
  Laufzeittest; ein Build allein beweist diese Funktion nicht.

## Gesicherter Ausgangsstand

Vor Beginn wurde die Sicherung unverändert geprüft:

- Branch: `codex/yocto-modernize`
- Remote: `https://github.com/mpc823/meta-neutrino-mp-pure.git`
- lokaler Commit und `origin/codex/yocto-modernize`:
  `6e416d0beb73abaa539baaa33fce1da9329c2542`
- Tag `rpi2-first-successful-build` lokal und remote zeigt auf denselben Commit
- Arbeitsbaum war vor dieser Dokumentation sauber

Der Raspberry-Pi-2-Build ist damit als Git-Stand gesichert. Diese Audit-Datei
ist die einzige absichtlich neu entstehende lokale Änderung und wurde noch
nicht gepusht.

## 1. Plattforminventar

Das Layer ist primär ein Rezept- und Integrationslayer. Es enthält keine
`conf/machine`-Dateien. Die eigentlichen Maschinen und BSPs kommen von
anderen Layern oder von historischen, externen Receiver-BSPs. Daher bedeutet
eine Plattformbedingung im Layer nicht automatisch, dass heute ein vollständiger
reproduzierbarer Build möglich ist.

| Plattform | Im Quellstand belegte Unterstützung | Build-/BSP-Status dieses Checkouts | Einordnung |
|---|---|---|---|
| Raspberry Pi 1 (`raspberrypi`) | `neutrino-mp.inc`, `libstb-hal_git.bb`, Raspberry-Pi-BSP | Rezeptpfade vorhanden; nicht als aktuelle Referenz gebaut | historisch/unterstützt, Hardwaretest offen |
| Raspberry Pi 2 (`raspberrypi2`) | eigene Machine-Overrides, `meta-raspberrypi`, KAS `machine: raspberrypi2` | erfolgreicher Referenz-Build, Tag `rpi2-first-successful-build` | aktivste und einzige belegte Referenzplattform |
| spätere Raspberry Pis | gemeinsame `raspberrypi*`-Idee, aber keine expliziten Machine-Dateien im Layer | kein konkreter Machine-/BSP-Nachweis im Repository | prinzipiell ableitbar, nicht belegt |
| x86-64 / Generic PC | Backend `libstb-hal/generic-pc`, Neutrino-Konfigurationspfad `generic` | keine x86-Machine, kein Host-/Fenster-Image und kein vollständiger Desktop-Startpfad gefunden | Quellbackend vorhanden, Plattform nur teilweise |
| Coolstream | `coolstream`-Overrides, `cs-drivers`/`libcoolstream`, Vendor-Header in Neutrino | `libstb-hal` wird für Coolstream maskiert; externer Vendor-BSP erforderlich | historischer Receiverpfad, nicht reproduzierbar im Layer allein |
| Spark | `spark`-Overrides und `libstb-hal/libspark` | externer SH4-/TDT-BSP und `tdt-driver` erforderlich; Rezept kommentiert Buildtest als unvollständig | historisch/teilweise vorhanden |
| Spark7162 | eigene `spark7162`-Overrides vor allem in Local-Rezepten und Neutrino-Konfiguration | kein vollständiger Machine-/BSP-Nachweis im Layer | historisch/teilweise vorhanden |
| Triple Dragon | `tripledragon`-Overrides, `libstb-hal/libtriple`, DirectFB und `triple-sdk` | alter Kernel, externe SDK-/BSP-Abhängigkeiten; kein Machine-File im Layer | historisch, technisch eigenständiger Legacypfad |
| AZbox ME/miniME | Neutrino-Summary, `libstb-hal/azbox`, `HAVE_AZBOX_HARDWARE` | kein Machine-File und kein BSP im geprüften Projekt | Quellbackend vorhanden, Buildstatus offen |

Zusätzliche historische Präprozessorpfade wie `HAVE_COOL_HARDWARE`,
`BOXMODEL_CS_HD1/HD2`, `HAVE_DBOX2` und `BOXTYPE_ARMBOX` belegen Erhaltungs- und
Kompatibilitätscode, sind aber kein Nachweis eines aktuellen Zielsystems.

Die KAS-Referenz pinnt BitBake, OpenEmbedded-Core, meta-openembedded,
meta-yocto und meta-raspberrypi auf feste Revisionen. Das ist für Raspberry Pi
2 reproduzierbar; für die Receiverplattformen fehlen im KAS-Stand die
entsprechenden externen BSP-Layer.

## 2. Gemeinsames Architekturmodell

Die belegte Struktur ist bereits eine teilweise gemeinsame HAL:

```text
Neutrino GUI / zapit / timerd / record / streamts
                 |
       gemeinsame Neutrino-Klassen und APIs
                 |
  +--------------+------------------+
  |                                 |
  | USE_STB_HAL                     | HAVE_COOL_HARDWARE
  |                                 |
  libstb-hal                        libcoolstream / Vendor-API
  |                                 |
  +-- raspi                         +-- Coolstream HD1/HD2
  +-- generic-pc
  +-- libspark
  +-- libtriple
  +-- azbox
```

Die zentrale Auswahl geschieht im Neutrino-Build über `--with-boxtype` und
`--with-boxmodel`. Raspberry Pi wird als `--with-boxtype=generic
--with-boxmodel=raspi` gebaut. `libstb-hal_git.bb` liefert
`virtual/stb-hal-libs`; Coolstream liefert denselben virtuellen Vertrag über
die externen `cs-drivers` und wird deshalb von `libstb-hal` ausgeschlossen.

Wesentliche gemeinsame Schnittstellen in `libstb-hal/include`:

- `audio_hal.h`: `cAudio` mit Start/Stop, Pause, Lautstärke, Streamtyp,
  Synchronmodus, Bitstream-/Clip-Ausgabe.
- `video_hal.h`: `cVideo` mit Start/Stop, Pause, Blank, Streamtyp,
  Synchronmodus, Videoformat, PIG und Screenshots.
- `dmx_hal.h`: `cDemux` mit Öffnen, Start/Stop, PES-/Section-Filter,
  PID-Verwaltung, Quelle und STC-Abfrage.
- `playback_hal.h`: `cPlayback` für Datei-/Streamwiedergabe, PIDs,
  Position, Geschwindigkeit, Untertitel und Titel.
- `record_hal.h`: `cRecord` für PID-Auswahl, Start/Stop, Status und
  Transponderwechsel.
- `hardware_caps.h`: `get_hwcaps()` für Fähigkeiten.

Auf Neutrino-Seite liegen die gemeinsamen höheren Schnittstellen in
`src/driver/framebuffer.h`, `rcinput.h`, `record.h` und `streamts.h`
sowie in `src/zapit/include/audio.h`, `video.h` und `dmx.h`. Die Zapit-Header
wählen zwischen `HAVE_COOL_HARDWARE` und `USE_STB_HAL`; ohne eine dieser
Varianten bricht die Konfiguration absichtlich ab.

## 3. Subsysteme und Plattformunterschiede

### Video-Decoder und Hardwarebeschleunigung

**Raspberry Pi:** Das Backend `libstb-hal/raspi` enthält `avdec.*`,
`avcodec_omx.c`, `codec.c`, `omx_utils.c` und `video.cpp`. Der Quelltext
verwendet Broadcom-OpenMAX-Komponenten wie `OMX.broadcom.video_decode`,
`video_render`, Scheduler und Clock-Tunnel. Die KAS-/Buildkonfiguration nutzt
den historischen `userland`-EGL/GLES-Anbieter und entfernt `vc4graphics`.
Das ist eine belegte OpenMAX-/VideoCore-Pipeline, nicht DRM/KMS und nicht
GStreamer. Die historische Aussage, sie sei grundsätzlich funktionsfähig,
ist damit technisch plausibel, aber Stabilität und A/V-Verhalten sind hier
nicht erneut auf echter Hardware gemessen.

**Generic PC:** `generic-pc/video.cpp`, `playback.cpp`, `dmx.cpp` und
`glfb.cpp` sind ein vorhandener Backendpfad. Eine konkrete Decoderbibliothek,
Fensterinitialisierung oder x86-64-Imagekonfiguration ist im Projekt nicht
als vollständiges Ziel belegt.

**Spark, Triple Dragon, AZbox:** Die Verzeichnisse `libspark`, `libtriple`
und `azbox` implementieren jeweils `video`, `playback` und `dmx` mit
plattformnahen Geräten bzw. Vendor-SDKs. Beim Triple Dragon ist DirectFB und
`triple-sdk` als Abhängigkeit belegt; beim Spark sind TDT-Treiber und SH4-
Umgebung erforderlich. Die tatsächlichen Decoderchips und Formate sind ohne
die jeweiligen Geräte/BSPs nicht belastbar zu verifizieren.

**Coolstream:** Der Quellbaum `neutrino-mp/lib/hardware/coolstream` enthält
Vendor-Header für Audio, Demux, Playback, Video, Record, TS-Routing und
Framebuffer. Das eigentliche Backend stammt aus `cs-drivers/libcoolstream`.
Es ist damit ein eigenständiger Vendorpfad, kein `libstb-hal`-Backend.

### Demultiplexer, Zeitstempel und A/V-Synchronisation

`src/zapit` enthält Frontend-, PMT-, CAPMT-, Scan- und Demuxsteuerung;
`frontend.cpp`, `femanager.cpp`, `zapit.cpp`, `capmt.cpp` und `scanpmt.cpp`
bilden den DVB-Steuerpfad. `cDemux` vereinheitlicht PES-/Section-Filter,
PIDs und STC-Zugriff. `CStreamInstance`/`CStreamManager` in `streamts.*`
und `CRecordInstance`/`CRecordManager` in `record.*` sind die gemeinsamen
höheren Streaming-/Aufnahmeschichten.

Die eigentliche Uhr und Synchronisation bleibt backendabhängig: Raspberry Pi
konfiguriert OpenMAX Clock/Scheduler und reicht den Synchronmodus an `cAudio`,
`cVideo` und `cDemux` weiter. Legacy-Receiver delegieren an Vendor-Player,
Treiber und Hardware-STC. Ein einheitliches modernes Clock-/Frame-Timing-
Modell über alle Plattformen ist nicht belegt. Dieser Teil muss auf realer
Hardware mit Kanalwechsel, PCR-Sprüngen, Pause und Timeshift geprüft werden.

### Audio

Die gemeinsamen Neutrino-Schichten sind `audioplay.*`, `audiofile.*`,
`audiometadata.*` und `src/driver/audiodec`, einschließlich FFmpeg-Decoder.
Die HAL-Schnittstelle ist `cAudio` in `audio_hal.h`.

Das RPi-Backend nutzt OpenMAX-Audio-Render; `generic-pc/audio.cpp` bildet den
Hostpfad ab. Spark, Triple Dragon und AZbox haben getrennte `audio.cpp`-
Implementierungen mit ihren jeweiligen Geräten/SDKs. Coolstream nutzt die
Vendor-Schnittstellen. Ausgabe, Passthrough, Lautstärke und Synchronität sind
daher nicht automatisch portabel, obwohl die Bedien-API gleich aussieht.

### Framebuffer, OSD und Videobild-Komposition

`CFrameBuffer` in `src/driver/framebuffer.h` und die gemeinsamen Zeichen-
operationen bilden das OSD-Modell. Die konkreten Beschleuniger sind per
`src/driver/Makefile.am` getrennt:

- Coolstream: `fb_accel_cs_hdx.cpp`, HD1/HD2 und Vendor-Framebuffer.
- Triple Dragon: `fb_accel_td.cpp`, `newclock.cpp`, `lcdd.cpp`.
- Spark: `fb_accel_sti.cpp`, `simple_display.cpp`.
- AZbox: `simple_display.cpp`.
- Generic/Raspberry Pi: `fb_accel_glfb.cpp`, `simple_display.cpp` sowie
  `libstb-hal`-`glfb.cpp`.

Das trennt OSD und Videodecoder nicht überall sauber auf moderne Weise:
Beim Raspberry Pi läuft das Video in OpenMAX, während das OSD über den
Framebuffer/GL-Pfad kommt. DirectFB beim Triple Dragon ist zugleich ein
Ausgabestack, aber nicht automatisch eine gemeinsame Video-Pipeline.
DRM/KMS-Planes oder ein gemeinsamer Compositor wurden im Quellstand nicht
gefunden.

### DVB, IPTV und Streaming

DVB-Tuner und Demux werden über `zapit`, Frontends, private DVB-Header und die
HAL-/Vendor-Demuximplementierungen bedient. Die Layer-Rezepte bringen zudem
`minisatip`, `udpxy`, `xupnpd`, `libupnp`, `djmount`, `curl`, FFmpeg und
Plugins mit. `netfile.cpp` und `streamts.*` liefern Netzwerk-/TS-Funktionen.
Eine zentrale, einheitliche IPTV- und Decoderpipeline ist nicht belegt;
IPTV ist derzeit eine Kombination aus Diensten, Dateistreaming und den
jeweiligen Player-Backends.

### Fernbedienung und Eingabe

`CRCInput` in `src/driver/rcinput.*` vereinheitlicht Geräteöffnung,
Key-Übersetzung, Wiederholung, Timer und eine Nachrichtenwarteschlange. Die
konkrete Eingabe ist aber plattformabhängig:

- Spark: `libspark/lirmp_input.*` und `irmp.c`.
- Triple Dragon: `libtriple/lt_dfbinput.*`.
- Coolstream: Nevis-/CS-IR-Vendorheader und bedingte HD1/HD2-Pfade.
- Generic/Raspberry Pi: Linux-Eingabegeräte beziehungsweise konfigurierter
  mdev-/Keyboardpfad; ein vollständiger Desktop-Input-Adapter ist nicht
  nachgewiesen.

### Aufnahme und Timeshift

`CRecordManager`, `CRecordInstance`, `CStreamRec` und `CStreamManager` sind
die gemeinsamen Aufnahme-/Timeshift-Schichten. PID-Auswahl, Dateinamen,
Status und Start/Stop sind dort gekapselt. Die eigentliche Aufnahme geht
über `cRecord` und damit auf unterschiedliche HAL-/Vendor-Demux- und
Treiberpfade. Timeshift und gleichzeitige Aufnahme/Wiedergabe müssen deshalb
pro Gerät getestet werden; ein erfolgreicher Compile reicht nicht.

### Boot und Start

`recipes-images/images/neutrino-image-base.inc` und die Init-Skripte bilden
den Image-/Startpfad. Es gibt belegte Plattformsonderfälle: auf Coolstream
werden Kernelmodule anders behandelt, beim Triple Dragon wird wegen des alten
Kernels `kernel-image` ausgelassen und `udev-extraconf` nicht verwendet.
`neutrino.init` enthält Coolstream-Reboot-/Treibermaßnahmen und Spark-
Frontpanel-/HDMI-Erkennung. Das bestätigt mehrere unterschiedliche Boot-
und Recovery-Annahmen. Ein gemeinsamer systemd-Startpfad für alle Receiver
ist nicht belegt.

## 4. Vergleichsmatrix

| Plattform | Decoder/Video | Audio | OSD/Komposition | Tuner/Demux | Eingabe | Aufnahme/Timeshift | Belegter Status |
|---|---|---|---|---|---|---|---|
| RPi 2 | OpenMAX VideoCore, `raspi/avcodec_omx.c` | OpenMAX Audio Render | FB/GLFB-OSD, getrennt vom OpenMAX-Video | Linux-DVB/HAL-Demux | Linux/mdev/Neutrino-Input | gemeinsame Record-Schicht + HAL | Build belegt, Laufzeit offen |
| RPi 1 | gleicher konfigurierter Pfad | gleicher HAL-Vertrag | gleicher Pfad | gleicher Softwarepfad, BSP abhängig | BSP abhängig | BSP abhängig | Rezeptpfad, kein aktueller Test |
| spätere Pi | nicht automatisch belegt; möglicher BSP-/Treiberwechsel | abhängig vom HAL | möglicher DRM/KMS-Wechsel | Linux-DVB möglich | Linux-Eingabe | prinzipiell gemeinsame Schicht | Hardware/BSP offen |
| x86-64 PC | `generic-pc`, konkrete Decoder-/Fensterkette fehlt | Host-Backend vorhanden | `glfb`/`clutterfb`, kein vollständiges Fensterziel | kein PC-DVB-Ziel im Layer belegt | Linux/Keyboard möglich | gemeinsame Schicht, Backend offen | Teilimplementierung |
| Coolstream | Vendor-Video/Playback | Vendor-Audio | Vendor-FB, `fb_accel_cs_*` | Vendor-Demux/TS-Routing | Vendor-IR | Vendor-Aufnahme + Neutrino | externes BSP nötig |
| Spark | SH4-/TDT-Backend | `libspark/audio.cpp` | STI/`simple_display` | SH4/TDT-Demux | LIRMP/IRMP | `libspark/record.cpp` | historisch, Build unvollständig |
| Spark7162 | Spark-Konfiguration; Hardwarepfad nicht separat belegt | Spark | Spark | BSP abhängig | Spark | Spark | keine vollständige Machine |
| Triple Dragon | Vendor-/DirectFB-/SDK-Pfad | `audio_td.cpp` | DirectFB, TD-FB/LCD | TD-DVB-Frontend-Modul | DirectFB-Input | `record_td.cpp` | historisch, alter Kernel |
| AZbox | `azbox/video.cpp`/Vendorgeräte | OSS-/Vendor-ioctl in `azbox/audio.cpp` | `simple_display` | Vendor-/BSP abhängig | BSP abhängig | Backend vorhanden | kein Machine/BSP |

## 5. Bereits gemeinsam, doppelt, veraltet und fehlend

### Bereits gemeinsam

- Neutrino-GUI, Zapit, Frontendverwaltung, Stream-/Record-Manager und große
  Teile des OSD-Zeichnens.
- HAL-Verträge für Audio, Video, Demux, Playback, Record und Capabilities.
- `virtual/stb-hal-libs` als austauschbarer Provider; Coolstream bleibt als
  Adapter außerhalb von `libstb-hal`.
- Gemeinsame Configure-Auswahl über Boxtyp/Boxmodell.

### Doppelt implementiert

Audio, Video, Demux, Playback und Record werden in fast jedem HAL-Backend
erneut implementiert. Auch OSD-Beschleunigung und Eingabe haben je Plattform
eigene Dateien. Das ist für Gerätezugriff notwendig, vergrößert aber die
Fehler- und Synchronisationsfläche.

### Veraltet oder eng gekoppelt

- Raspberry-Pi-OpenMAX/VideoCore (`avcodec_omx.c`, `omx_utils.c`) ist eine
  historische, hardwaregebundene Schnittstelle.
- Triple-Dragon-DirectFB und die `triple-sdk`-/Kernelannahmen sind Legacy.
- Coolstream nutzt proprietäre Header und externe Treiber statt des normalen
  `libstb-hal`-Quellbaums.
- SH4-/Spark-Pfade hängen an alten TDT-/LIRMP-/Treiberkonventionen.
- Bedingte Präprozessorzweige verteilen Hardwarewissen bis in GUI-, Zapit-
  und Systemcode.

### Nicht vorhanden oder nicht nachgewiesen

Im geprüften Layer und den entpackten Quellen wurden keine Implementierungen
für GStreamer, V4L2 als Medienpipeline oder DRM/KMS gefunden. Ebenso wurde
kein vollständiger x86-64-Fenster-Target mit X11/Wayland/SDL-Start, kein
aktueller SH4-/PowerPC-/MIPS-BSP und kein hardwareübergreifender moderner
Clock-/Compositor-Vertrag gefunden. Diese Technologien sind daher Optionen,
keine vorhandenen Lösungen.

## 6. Konkrete Alternativen

| Baustein | Zuständigkeit | Stärken | Grenzen/Risiken |
|---|---|---|---|
| bestehende HAL | Gerätezugriff und Player-Vertrag | höchste Wiederverwendung, Legacy bleibt bootfähig | viele getrennte Implementierungen, uneinheitliche Timingmodelle |
| GStreamer | Demux, Decoder, Filter, A/V-Pipeline | ARM/x86-portabel, Hardwareplugins und Tests vorhanden | nicht selbst OSD-/Fenster-/DRM-Konzept; Plugins, Latenz, Lizenz-/Abhängigkeitsumfang und SH4-Tauglichkeit müssen geprüft werden |
| V4L2 | Geräte-, Decoder- oder Capture-API | Linux-Standard für bestimmte Videohardware | kein vollständiger Demux, kein OSD-Compositor, historische Receiver haben oft keinen V4L2-Treiber |
| DRM/KMS | Display, Planes, Modesetting, Buffer | moderner Pi-/PC-Ausgabepfad, headless und effizient | kein Decoder und kein Audio; Plane-/Overlay-/Treiberverhalten muss je SoC getestet werden |
| DirectFB/Framebuffer | ältere Ausgabe und OSD | auf vorhandenen Receivern verfügbar | Legacy, geringe gemeinsame Zukunft für PC/Pi |
| X11/Wayland/SDL-Fenster | Desktop-/Fensterausgabe | macht Neutrino als normale Anwendung möglich | zusätzlicher Window-System-Adapter, Input-/Scaling-/Fullscreen-Fragen |

Decoder, Videoausgabe, OSD-Komposition und Fenstersystem sind getrennte
Probleme. GStreamer kann Decoder/Demux/Clock liefern; DRM/KMS kann Video- und
OSD-Buffer anzeigen; Wayland/X11/SDL kann ein Fenster bereitstellen. Keine
dieser Komponenten ersetzt allein die anderen drei.

## 7. Architekturvarianten

### A: Bestehende HAL minimal erweitern

Die vorhandenen `audio_hal`, `video_hal`, `dmx_hal`, `playback_hal` und
`record_hal` bleiben der Vertrag. Für Raspberry Pi und Generic PC werden
optionale moderne Backends ergänzt, zunächst außerhalb der Legacypfade:

1. RPi-Ausgabe wahlweise über einen getesteten modernen Decoder-/Clockpfad
   und DRM/KMS, OSD zunächst über einen klar getrennten Bufferpfad.
2. PC-Ausgabe über einen Fensteradapter; Decoder/Audio zunächst mit einer
   hostgeeigneten Pipeline.
3. Coolstream, Spark, Triple Dragon und AZbox bleiben unverändert über
   Adapter und erhalten nur notwendige Capability-Abbildungen.

**Aufwand:** mittel. **Risiko:** mittel; vor allem Clock, Bufferformat,
Passthrough und OSD-Overlay. **Wiederverwendung:** hoch. **Kompatibilität:**
am besten, da alte Backends nicht ersetzt werden. **Nachteil:** Der HAL-
Vertrag bleibt in Teilen hardwarehistorisch und kann weitere Sonderfälle
ansammeln.

### B: Gemeinsame Multimedia-Abstraktion

Eine neue, explizite Schicht trennt `MediaSource/Demux`, `Decoder`, `Clock`,
`AudioSink`, `VideoSink`, `OSD/Compositor`, `Input` und `Window/Display`.
Die alten HALs werden als Adapter eingebunden; moderne Backends können
GStreamer, DRM/KMS und ein Desktop-Fenster kombinieren.

**Aufwand:** hoch. **Risiko:** hoch; besonders bei Kanalwechsel, CA/PMT,
Timeshift, STC/PCR, Audio-Passthrough und Receiver-Recovery. **Wiederverwendung:**
langfristig hoch, kurzfristig gering. **Kompatibilität:** erreichbar, aber nur
mit stabilen Legacy-Adaptern und umfangreichen Hardwaretests. **Vorteil:**
saubere Trennung der vier Ausgabeschichten und bessere PC-/Pi-Portierbarkeit.

### Bewertung

Für den Bestand ist A der sichere erste Schritt. B ist als Zielarchitektur
plausibel, aber erst nach einer präzisen Capability-/Timing-Spezifikation und
einem funktionierenden RPi-/PC-Prototypen verantwortbar. Eine gestufte
Kombination ist realistisch: zuerst A ohne Verhaltensänderung, danach B nur an
den nachweislich gemeinsamen Grenzen. Das ist eine Empfehlung aus der
Bestandslage, kein bereits getesteter Implementierungsplan.

## 8. Priorisierte nächste Schritte nach der Bestandsaufnahme

1. Plattform- und Capability-Matrix einfrieren: Decoderformate, Audio-
   Bitstream, OSD-Auflösung/Alpha, Tuner, CA, Aufnahme, Standby und Input.
2. Für jede Legacyplattform BSP, Kernel, Treiberrevision und reale Hardware
   dokumentieren. Ohne diese Nachweise bleiben SH4, PowerPC, MIPS und
   Coolstream nur Quellkompatibilität.
3. Die vorhandene RPi-2-Konfiguration als funktionale Referenz aufbewahren;
   zusätzlich A/V-Sync, OSD über Video, Kanalwechsel und Timeshift auf echter
   Hardware testen.
4. Einen separaten Generic-PC-Prototypen mit Fenster, Tastatur/IR-Eingabe,
   Audio, Video und OSD definieren; noch keine Legacypfade entfernen.
5. Für moderne Pfade zuerst Decoder/Clock, dann Videoausgabe, dann OSD und
   zuletzt Fensterintegration einzeln testen. GStreamer und DRM/KMS sind
   getrennte Experimente.
6. Paritätstests für Start/Stop/Pause, PCR-Sprünge, Audio-Passthrough,
   Scaling, OSD-Alpha, Tunerwechsel, Aufnahme und Timeshift festlegen.
7. Erst nach diesen Tests entscheiden, ob ein gemeinsamer Multimediavertrag
   (Variante B) die HAL an definierten Punkten ergänzt.

## 9. Verifikationsplan und Grenzen

Der erfolgreiche RPi-2-Yocto-Build belegt Rezeptauflösung, Kompilierung und
Packaging für diese Konfiguration. Er belegt nicht Decoderstabilität,
Hardware-A/V-Sync, DVB-Empfang, CA, Fernbedienung, Aufnahme oder Standby.

Für jede Plattform sind mindestens erforderlich:

- Boot bis Neutrino und sauberer Neustart/Standby;
- Tunerinitialisierung, Scan, Kanalwechsel und verschlüsselte/freie PMTs;
- 25/50-Hz-Video, Auflösungswechsel, PCR-/PTS-Sprünge und längerer Lauf;
- PCM, MPEG/AC3/DTS-Passthrough, Lautstärke und Mute;
- OSD über laufendem Video, Transparenz, Scaling und Screenshot;
- Aufnahme, Timeshift, gleichzeitige Wiedergabe und Dateisystemfehler;
- IR/Keyboard, Repeat, Frontpanel und Wakeup;
- Netzwerkstreaming/IPTV mit Verlust und Wiederverbindung.

Keine dieser Hardwaretests wurde im Rahmen dieses Audits simuliert oder
behauptet. Besonders die Hinweise zur historischen OpenMAX-Stabilität und zu
GStreamer sind als historische Hinweise beziehungsweise mögliche Optionen
zu lesen, nicht als verifizierte Diagnosen.

## Schlussfolgerung

„Einer für alle, alle für einen“ ist im Bestand teilweise erreicht: Neutrino
und `libstb-hal` teilen bereits die wesentlichen Audio-, Video-, Demux-,
Playback- und Record-Verträge. Die Receiver-Hardware steckt jedoch weiterhin
in vielen separaten Implementierungen und externen BSPs. Raspberry Pi 2 ist
aktuell die einzige reproduzierbar gebaute Referenz; Generic PC ist nur als
Quellbackend vorhanden, die übrigen Receiver sind überwiegend historische
oder externe Zielpfade.

Die belastbarste Richtung ist deshalb eine konservative, adapterbasierte
Erweiterung der bestehenden HAL mit getrennten modernen Pi-/PC-Ausgabewegen.
Eine vollständige gemeinsame Multimediaabstraktion bleibt das langfristig
sauberere Ziel, darf aber erst nach realen Timing-, OSD-, Audio- und
Aufnahmetests eingeführt werden. Alte ARM-, SH4-, PowerPC- und MIPS-Pfade
bleiben dabei erhalten; aus dem vorliegenden Quellstand lässt sich keine
verantwortbare Entfernung ableiten.

