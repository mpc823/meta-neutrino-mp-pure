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
- [x] Native Syntaxprobe mit Host-Includes durchgeführt.
- [ ] Native `CNeutrinoApp`-Ausführung: aktuell blockiert bereits der erste
      fehlende Host-Baustein `pugixml.hpp`; danach folgen VFD, Zapit, Audio,
      Video, RCInput und Daemon-Abhängigkeiten.

Der Bootstrap ist ein echter, gelinkter Core-/HAL-Testprozess, aber bewusst
noch kein vollständiger `CNeutrinoApp`-Start und wird nicht als solcher
ausgegeben.
