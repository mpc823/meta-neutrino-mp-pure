# Master-TODO: native x86-64-Entwicklung

## Erledigt

- [x] Generic-PC-HAL als headless statisches Archiv bauen.
- [x] FFmpeg-6.1.1-Kompatibilität versionsabhängig ergänzen.
- [x] Native HAL-, OSD-, Eingabe-, Konfigurations- und monotonic-time-Tests.
- [x] Startfähigen `native-neutrino-headless`-Bootstrap mit kontrolliertem
      Shutdown und ASan/UBSan-Test.
- [x] Raspberry-Pi-2-`libstb-hal`- und `neutrino-mp`-Compile-Regression.
- [x] Vollständiger nativer x86-64-Compile-/Linklauf des echten `neutrino`-
      Programms mit Generic-PC-HAL, common-HAL und OpenGL/GLUT/GLEW.
- [x] FFmpeg-6-Kompatibilität für Audio-Decoder, Aufnahme und Stream-TS
      versionsabhängig ergänzt und mit Host-FFmpeg 6.1.1 kompiliert.
- [x] Raspberry-Pi-2-Regression nach dem FFmpeg-Typfix mit
      `bitbake -k -c compile neutrino-mp libstb-hal` und Exitcode 0 verifiziert.
- [x] Native OpenGL-Full-Core-Linkzeile reproduzierbar mit Make-Variablen
      ausgeführt; x86-64-ELF-Prüfung und kontrollierter Display-Fehlerfall
      verifiziert.
- [x] Echter nativer CNeutrinoApp-GUI-Start auf dem vorhandenen X11-Display
      mit Generic-PC-HAL, OpenGL/GLUT/GLEW, OSD und Font-/Logo-Ressourcen.
- [x] Kontrollierter GUI-Shutdown über `/control/shutdown` sowie
      Tastatureingaben über den produktiven `rcsim`-Event-Socket mit
      `scripts/native-gui-smoke` reproduzierbar getestet.
- [x] Gemeinsamen Core für Generic-PC mit und ohne lokales Frontend erhalten:
      fehlende DVB-Hardware wird zur Laufzeit als Ressourcenstatus behandelt;
      es gibt keine separate No-DVB-Programmvariante. EIT/Zeitsuche und
      historisches Host-Automount werden nur bei passenden Laufzeitressourcen
      beziehungsweise Nicht-PC-Zielen aktiviert.
- [x] USB-DVB-S2-Frontend PCTV 461e (2013:0258) am Host erkannt; Kernel bindet
      `em28xx`/`em28xx_dvb`/`m88ds3103` und legt die DVB-Geräte an.

## Offen

- [ ] Vierfachtest der gemeinsamen Generic-PC-Laufzeitmatrix (Fenster/Receiver
      jeweils mit/ohne lokalen Tuner) abschließen. Der Fensterpfad mit
      angeschlossenem PCTV-461e und der kontrollierte Runtime-No-DVB-Pfad
      erreichen reproduzierbar den GUI-Marker, bedienen Menü/Exit und fahren
      per HTTP herunter; der dedizierte Receiver-Modus sowie ein echter
      physischer Geräteausbau sind noch nicht getestet.
- [ ] DVB-Tuner, Live-Video/Audio und physische Eingabegeräte auf echter
      Hardware beziehungsweise mit einem vollständigen virtuellen DVB-/AV-
      Backend testen.
- [x] Firmware `dvb-demod-m88ds3103.fw` installiert und nach USB-Neuinitialisierung
      per Kernelmeldung geladen; Version `3.B`, 8192 Bytes, SHA-256
      `4767ab80ceba4a66315cbec2a07ae1f7ebbd19c5758fd098b932e02c9108eff9`.
- [ ] Echten Transponder-Lock mit Satellitenanlage testen. Der Frontend-Probe
      meldet DVB-S/DVB-S2-Unterstützung, aber ohne LNB/Signal `FE_READ_STATUS=0x00`
      (`locked=no`); erfolgreicher Empfang ist damit nicht nachgewiesen.
- [ ] Betriebszustände im Tuner-HAL explizit unterscheiden: keine Quelle,
      Frontend vorhanden, Firmware/Frontend nicht betriebsbereit, lokaler
      Empfang, SAT>IP/Remote-Tuner und IPTV.
- [ ] Remote-Tuner als eigener späterer Meilenstein: Servererkennung,
      Fähigkeiten, Reservierung/Freigabe, TS-Transport, Reconnect und
      Kombination lokaler/entfernter Tuner.
- [ ] Receiver-unabhängigen Headless-Framebuffer und echte Neutrino-
      Nachrichten-/Timer-Schnittstelle integrieren.
- [ ] Native Senderlisten-/Zapit-Konfiguration ohne Hardware testen.
- [x] Zapit-Startursache `live_fe == NULL` vor `zapit_server.prepare()`
      nachgewiesen; Generic-PC-Idle-Quellenmodus, leere Senderlisten- und
      Eingabe-Nullpointerpfade mit gezielten Patches abgesichert.
- [x] Optionalen OpenGL-Fensterpfad unter dem vorhandenen X11-Server mit
      automatisiertem Start-/Eingabe-/Shutdown-Smoke-Test validieren.
- [x] GUI-Smoke-Test um echte X11-Fensteraufnahme nach `KEY_MENU` erweitert;
      `xwininfo`/`xwd` bestätigen Fenstername `Neutrino`, 1280x720-Auflösung
      und ein plausibles XWD-Bildartefakt.
- [ ] OpenGL-Fensterpfad zusätzlich unter Xvfb validieren, sobald Xvfb auf dem
      Host verfügbar ist; echte Video-/Audio-Wiedergabe bleibt offen.
- [ ] Native Startumgebung ohne X-Server als separaten Headless-Framebuffer
      ausführen; das gelinkte GUI-Programm meldet ohne `DISPLAY` korrekt den
      fehlenden Displayzugang.

## Buildkonsistenz und Zielarchitektur

- [x] Zentrale `scripts/check-build-architecture`-Prüfung für ELF-Dateien,
      statische Archive, ELF-Klasse und Endianness ergänzt.
- [x] Native Generic-PC- und Headless-Wrapper prüfen ihre erzeugten Artefakte
      verbindlich vor der Erfolgsmeldung.
- [x] Yocto-Target-Rezepte `libstb-hal` und `neutrino-mp` prüfen alte Artefakte
      vor dem Kompilieren sowie neue Artefakte vor Packaging.
- [x] Negativtest für ein absichtlich gemischtes Archiv erfolgreich ausgeführt.
- [ ] SH4-, MIPS-, PowerPC- und historische ARM-Toolchains mit der zentralen
      Prüfung ausführen; diese Toolchains sind auf dem Host derzeit nicht
      vollständig verfügbar und daher nicht als getestet markiert.

## CNeutrinoApp-Analyse

## Debugging und Remote-GDB

- [x] Host-Debugwerkzeuge `gdb`, `gdbserver` und `gdb-multiarch` verfügbar;
      GDB bleibt Ergänzung zu Logs, Instrumentierung, strace und Sanitizern.
- [ ] Bei echtem Zielhardwarefehler Debug-Symbole und passenden Sysroot für
      Raspberry Pi/SH4/MIPS/PPC bereitstellen und `gdbserver` ausschließlich
      über einen SSH-Tunnel verwenden. Keine Debugports in Produktionsnetzen
      öffnen und Debugwerkzeuge nicht standardmäßig ins Release-Image nehmen.
- [ ] Remote-GDB erst beim ersten reproduzierbaren Zielhardwarefehler aktiv
      einsetzen; HTTP-Steuerung kann dann zur reproduzierbaren Auslösung
      kombiniert werden.

- [x] Echter Einstieg in `src/neutrino.cpp` und Initialisierungsreihenfolge
      geprüft.
- [x] Native Syntaxkompilierung mit pugixml und libcurl auf dem Host.
- [x] Native pugixml- und libcurl-Entwicklungsdateien geprüft und die
      reproduzierbare Syntaxprobe für den produktiven Einstieg dokumentiert.
- [x] Echter nativer `neutrino`-Link mit Generic-PC-HAL, Zapit, Audio-,
      Video-, RCInput- und Daemon-Bibliotheken abgeschlossen.
- [x] Kontrollierter CNeutrinoApp-Lauf unter dem vorhandenen realen X-Server;
      Xvfb ist auf dem Host nicht installiert und daher nicht als getestet
      markiert.

## Host-Abhängigkeiten und Nachweise

- [x] `libcurl4-openssl-dev`, `libpugixml-dev`, `libgif-dev`, `freeglut3-dev`
      und `libglew-dev` mit `pkg-config`/Headerproben bestätigt.
- [x] Der native Link verwendet ausschließlich Host-Bibliotheken und einen
      separat gebauten x86-64-`libstb-hal`-Anteil; Yocto-ARM-Artefakte wurden
      durch die zentrale Architekturprüfung ausgeschlossen.

Der Bootstrap bleibt ein isolierter Core-/HAL-Testprozess. Zusätzlich ist der
vollständige native `neutrino`-Link nun bis zum echten CNeutrinoApp-GUI-Start
verifiziert: GLFB initialisiert den OSD-Puffer, Fonts/Logo werden geladen,
`rcsim`-Eingaben werden über den Event-Socket zugestellt und der bestehende
HTTP-Control-Endpunkt löst den regulären Shutdown aus. Bei `NEUTRINO_PC_NO_DVB=1`
bleibt die GUI auch ohne Frontend verfügbar; `CStreamManager` für Netzwerk- und
IPTV-Pfade bleibt aktiv. Ohne erkannte Firmware ist ein Frontend dagegen nur
erkannt, aber noch kein belastbarer Empfangsnachweis. Der reproduzierbare Test
ist:

```sh
NEUTRINO_NATIVE_BIN=/pfad/zur/installierten/bin/neutrino \
NEUTRINO_NATIVE_RCSIM=/pfad/zur/installierten/bin/rcsim \
  ./scripts/native-gui-smoke
```
