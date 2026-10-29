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

## Offen

- [ ] Den vollständigen `CNeutrinoApp`-Stack für Generic-PC ohne DVB-Gerät,
      Audio-Hardware und Display initialisieren.
- [ ] Receiver-unabhängigen Headless-Framebuffer und echte Neutrino-
      Nachrichten-/Timer-Schnittstelle integrieren.
- [ ] Native Senderlisten-/Zapit-Konfiguration ohne Hardware testen.
- [ ] Optionalen OpenGL-Fensterpfad unter einem echten X-Server/Xvfb und echte
      Video-/Audio-Wiedergabe testen; der Link ist abgeschlossen.
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

- [x] Echter Einstieg in `src/neutrino.cpp` und Initialisierungsreihenfolge
      geprüft.
- [x] Native Syntaxkompilierung mit pugixml und libcurl auf dem Host.
- [x] Native pugixml- und libcurl-Entwicklungsdateien geprüft und die
      reproduzierbare Syntaxprobe für den produktiven Einstieg dokumentiert.
- [x] Echter nativer `neutrino`-Link mit Generic-PC-HAL, Zapit, Audio-,
      Video-, RCInput- und Daemon-Bibliotheken abgeschlossen.
- [ ] Kontrollierter CNeutrinoApp-Lauf unter Xvfb oder realem X-Server; auf
      dem aktuellen Host ist kein X-Server verfügbar.

## Host-Abhängigkeiten und Nachweise

- [x] `libcurl4-openssl-dev`, `libpugixml-dev`, `libgif-dev`, `freeglut3-dev`
      und `libglew-dev` mit `pkg-config`/Headerproben bestätigt.
- [x] Der native Link verwendet ausschließlich Host-Bibliotheken und einen
      separat gebauten x86-64-`libstb-hal`-Anteil; Yocto-ARM-Artefakte wurden
      durch die zentrale Architekturprüfung ausgeschlossen.

Der Bootstrap ist ein echter, gelinkter Core-/HAL-Testprozess, aber bewusst
noch kein vollständiger `CNeutrinoApp`-Start und wird nicht als solcher
ausgegeben. `scripts/native-cneutrino-syntax` prüft zusätzlich den echten
Produktionsquelltext von `src/neutrino.cpp`, ohne Zielbibliotheken zu linken.
