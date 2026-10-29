# Master-TODO: native x86-64-Entwicklung

## Erledigt

- [x] Generic-PC-HAL als headless statisches Archiv bauen.
- [x] FFmpeg-6.1.1-Kompatibilität versionsabhängig ergänzen.
- [x] Native HAL-, OSD-, Eingabe-, Konfigurations- und monotonic-time-Tests.
- [x] Startfähigen `native-neutrino-headless`-Bootstrap mit kontrolliertem
      Shutdown und ASan/UBSan-Test.
- [x] Raspberry-Pi-2-`libstb-hal`- und `neutrino-mp`-Compile-Regression.

## Offen

- [ ] Den vollständigen `CNeutrinoApp`-Stack für Generic-PC ohne DVB-Gerät,
      Audio-Hardware und Display initialisieren.
- [ ] Receiver-unabhängigen Headless-Framebuffer und echte Neutrino-
      Nachrichten-/Timer-Schnittstelle integrieren.
- [ ] Native Senderlisten-/Zapit-Konfiguration ohne Hardware testen.
- [ ] Optionalen OpenGL-Fensterpfad und echte Video-/Audio-Wiedergabe testen.
- [ ] Native Full-Core-Linkage ohne ARM-/Yocto-Sysroot-Abhängigkeiten herstellen.

## CNeutrinoApp-Analyse

- [x] Echter Einstieg in `src/neutrino.cpp` und Initialisierungsreihenfolge
      geprüft.
- [ ] Native Syntaxkompilierung mit vollständig verfügbaren Host-Includes
      durchführen (aktuell fehlt libcurl-dev).
- [x] Native pugixml-Entwicklungsdateien geprüft und die reproduzierbare
      Syntaxprobe für den produktiven `CNeutrinoApp`-Einstieg als
      `scripts/native-cneutrino-syntax` dokumentiert; der Lauf stoppt derzeit
      korrekt am fehlenden `libcurl`-Hostpaket.
- [ ] Native `CNeutrinoApp`-Ausführung: der nächste reproduzierbare Blocker
      ist der fehlende Host-Entwicklungsbaustein `libcurl` (Header und
      `libcurl.pc`); danach müssen native libstb-hal-, Zapit-, VFD-, Audio-,
      Video-, RCInput- und Daemon-Bibliotheken separat gelinkt werden.

Der Bootstrap ist ein echter, gelinkter Core-/HAL-Testprozess, aber bewusst
noch kein vollständiger `CNeutrinoApp`-Start und wird nicht als solcher
ausgegeben. `scripts/native-cneutrino-syntax` prüft zusätzlich den echten
Produktionsquelltext von `src/neutrino.cpp`, ohne Zielbibliotheken zu linken.
