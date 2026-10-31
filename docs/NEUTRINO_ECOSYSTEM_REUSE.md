# Neutrino-Ökosystem: Quellenprüfung und Wiederverwendungsregeln

Stand der Prüfung: 2026-10-31. Die Revisionen und Branches in diesem Dokument
wurden per `git ls-remote` gegen die öffentlichen HTTPS-Repositories geprüft.
Ein HEAD ist kein zeitloser Release; für reproduzierbare Builds muss er als
Commit in einer Manifest-/Lockdatei festgehalten werden.

## Verbindlicher Grundsatz

Wir übernehmen eine vorhandene, passende und lizenzrechtlich nutzbare Lösung,
bevor wir eine zweite Implementierung beginnen. Eine Übernahme erfolgt erst
nach Prüfung von API, Zielplattform, Kernel-/Treiber-ABI, Buildrelease,
Lizenz und realer Buildbarkeit. Fremde proprietäre Treiber, Firmware oder
Binärarchive werden nicht ungeprüft kopiert. Der gemeinsame Neutrino-Core
bleibt von der plattformspezifischen HAL getrennt.

## Verifizierte Quellen

| Quelle | Branch / Commit am Prüftag | Lizenz-Metadatum | Nachweisbarer Nutzen |
|---|---|---|---|
| [tuxbox-neutrino/neutrino](https://github.com/tuxbox-neutrino/neutrino) | `master` / `23662e1a2ec45bdc1e5711abbbac6057d0586655` | GPL-2.0 | aktueller gemeinsamer Neutrino-Core |
| [tuxbox-neutrino/libstb-hal](https://github.com/tuxbox-neutrino/libstb-hal) | `mpx` / `7411648ec0380fe2629b9b699c49b72e3f526432` | GPL-2.0 | HAL für ARM/MIPS, PC und Raspberry Pi; API- und Backendvergleich |
| [tuxbox-neutrino/neutrino-generic-build](https://github.com/tuxbox-neutrino/neutrino-generic-build) | `master` / `15af8e819bb3564789ca40b7f8fecb8441ff9962` | GPL-2.0 | nativer PC-/AppImage-Buildpfad |
| [tuxbox-neutrino/build-environment](https://github.com/tuxbox-neutrino/build-environment) | `master` / `84d3d7739c9f27a4279eedcb472750ad68ecef69` | im API-Metadatum nicht angegeben | Yocto/OpenEmbedded-Umgebung; Lizenz der enthaltenen Einzelkomponenten separat prüfen |
| [KittyBua/buildenv](https://github.com/KittyBua/buildenv) | `3.2` / `55d9f0252230774e5dedc24cec179d6b7db2c215` | im API-Metadatum nicht angegeben | historischer Poky-3.2-/Maschinen-Setup, darunter `hd51`; nicht Wrynose-kompatibel ohne Portierung |
| [Duckbox-Developers/buildsystem-ddt](https://github.com/Duckbox-Developers/buildsystem-ddt) | `master` / `7c1e26c5b2175f8b658658ce632e200090d8171f` | im API-Metadatum nicht angegeben | historische Release-/Rootfs-Logik, HD51-Startdateien und Multiboot-Struktur |
| [oe-alliance/oe-alliance-core](https://github.com/oe-alliance/oe-alliance-core) | `6.0` / `af2e1875d43f8fa94721ac57eee33e7b57deb6c4` | GPL-2.0 | aktuelle MACHINE-Liste, `mutant51`, Broadcom-/GFutures-BSP-Anknüpfung |
| [neutrino-hd/meta-hd51](https://github.com/neutrino-hd/meta-hd51) | `gatesgarth` / `260fa831d8a4d523710839f66a2eaa9fa2df24bb` | im API-Metadatum nicht angegeben | HD51-BSP-Kandidat; Gatesgarth, nicht direkt in Wrynose einmischen |
| [neutrino-hd/meta-neutrino](https://github.com/neutrino-hd/meta-neutrino) | `gatesgarth` / `64eb565ba25f8c1c22ee3da27b34f8cb7b6df7fa` | im API-Metadatum nicht angegeben | Neutrino-Rezepte für den historischen HD51-Stack |
| [OpenPLi/openpli-oe-core](https://github.com/OpenPLi/openpli-oe-core) | `scarthgap` / `3256ee6f239a89e57d24153e06361643bd5d3441` | GPL-2.0 | GFutures-/OpenPLi-Vergleich; Scarthgap, eigene ABI-/Layer-Matrix erforderlich |
| [vitmod/neutrinohd2-1](https://github.com/vitmod/neutrinohd2-1) | `master` / `98fbe99b8df2f0d3d05d5e22d6a0469a925332b7` | im API-Metadatum nicht angegeben | breite historische SH4-, MIPS-, ARM- und PC-Abdeckung; API-/Fallbackvergleich |

Die Suche nach `openpli/meta-gfutures` ergab am Prüftag ein nicht existentes
Repository. Die in unserem Build verwendete GFutures-Quelle ist daher nicht
daraus abzuleiten; sie bleibt im gepinnten OE-Alliance-Quellenarchiv vermerkt.

## Technische Ergebnisse

* `oe-alliance-core` bestätigt `mutant51` als MACHINE und enthält die HD51-
  Plattformfamilie. Es ist die wertvollste Quelle für Maschinen- und
  Branding-Verträge, aber kein Beweis für einen bootfähigen Neutrino-Port.
* `KittyBua/buildenv` zeigt einen reproduzierbaren Maschinen-Setup-Ansatz auf
  Poky 3.2. Die vorhandene HD51-Definition darf nicht ungeprüft in Wrynose
  übernommen werden.
* `buildsystem-ddt` dokumentiert die historische Trennung von Kernelmodulen,
  Firmware, `/etc/init.d` und Multiboot-Dateien. Das ist für unsere
  Paket-/Imageprüfung nützlich, ersetzt aber kein aktuelles BSP.
* `meta-hd51`/`meta-neutrino` sind Gatesgarth und damit ein Portierungs-
  Vergleich, keine Wrynose-Layer. Kernel-, Nexus-/V3D- und Firmware-ABI müssen
  gemeinsam passen.
* `libstb-hal` und der Generic-PC-Build sind direkte Kandidaten für API- und
  Testwiederverwendung. Die HAL-Implementierung wird nicht aus Enigma2- oder
  proprietären Treibern rekonstruiert.
* `neutrinohd2-1` ist historisch breit portiert. Seine Plattform-Sonderpfade
  werden nur als Vergleich verwendet; einzelne Übernahmen brauchen Lizenz-,
  API- und Ziel-ABI-Prüfung.

## Vergleichsmatrix

| Stack | OE/Yocto | HD51-Bezug | Wiederverwendung |
|---|---|---|---|
| Unser Wrynose/KAS | Wrynose 6.0 | Zielstack | primär; gepinnte Layer und gemeinsamer Core |
| OE-Alliance `6.0` | Wrynose-nahe Basis | `mutant51`, GFutures | MACHINE-/BSP-Vertrag und Rezepte prüfen |
| OpenPLi | Scarthgap | GFutures-Vergleich | nur gezielt und mit Layer-ABI-Prüfung |
| neutrino-hd | Gatesgarth | `meta-hd51` | historische BSP-/Rezeptreferenz |
| KittyBua | Poky 3.2 | `hd51` | Setup-Idee, keine direkte Layer-Mischung |
| DDT | historischer Make-/Rootfs-Stack | HD51-Release | Boot-/Rootfs-Struktur, nicht Wrynose-BSP |
| NeutrinoHD2 | diverse historische Targets | SH4/MIPS/ARM/PC | HAL-/Fallbackvergleich, keine automatische Übernahme |

## Praktische Konsequenz für dieses Repository

1. Der gemeinsame Core und `libstb-hal` bleiben die erste Integrationsstelle.
2. Für HD51 wird der vorhandene gepinnte OE-Alliance/GFutures-Pfad weiter
   geprüft; fehlende proprietäre Kernel-/Firmware-Artefakte werden als externe
   Blockade dokumentiert, nicht durch Dummy-Rezepte ersetzt.
3. Nicht benötigte Enigma2-, Kodi- und Qt5-Rezeptfamilien werden im
   HD51-Neutrino-Manifest nur dann maskiert, wenn sie nicht in
   `neutrino-image`-Abhängigkeiten vorkommen. Der DVB-GStreamer-Sink bleibt
   separat verfügbar.
4. Neue Übernahmen benötigen einen Eintrag mit URL, Branch, Commit, Lizenz,
   Zweck und Testnachweis. Floating-Branches gehören nicht in produktive
   Fetches.
5. Vor jeder Eigenentwicklung sind zuerst Core, HAL, Generic-PC-Build und die
   historischen HD51-/DDT-Lösungen zu vergleichen.

## Quellen und Grenzen

Die genannten Commitstände sind öffentlich auffindbare Referenzstände, keine
Garantie für Funktionsfähigkeit auf unserem Receiver. Hardwaredecoder,
Broadcom-Nexus/V3D, HDMI-CEC, CI und Multiboot bleiben nur durch passende
Kernel-/Treiber-/Firmwarekombinationen und echte Hardwaretests nachweisbar.
