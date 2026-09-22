# 🎣 FishBot - TurkeyMC Skyblock Otomasyonu

Minecraft Fabric (1.21.11) tabanlı, **TurkeyMC Skyblock** sunucusu için özel olarak geliştirilmiş modüler otomasyon ve asistan modudur. Bot, arka planda güvenli menü kontrolleri ve zamanlayıcılarla çalışarak sunucu ile tam senkronize işlem yapar.

## ✨ Özellikler

Bot, bağımsız çalışan 5 farklı modül ile tam otomasyon sağlar:

* **🐟 Gelişmiş Balık Botu:** Suya atılan oltayı takip eder, balık vurduğunda otomatik çeker. Çoklu olta kullanımı ve Mending (Tamir) büyüsü için otomatik XP şişesi fırlatma modlarına sahiptir.
* **🌻 Çiçek (Mevsim) Otomasyonu:** Her **53 dakikada bir** arka planda `/mevsim depo` komutunu çalıştırır ve 51. slota tıklayarak çiçekleri otomatik satar.
* **🌾 Çiftçi Otomasyonu:** Çakışmaları önlemek için her **47 dakikada bir** `/çiftçi` komutunu çalıştırır. Sayfalar arası geçiş yaparak (Shift+Sağ Tık / QUICK_MOVE) eşyaları oyuncunun üzerine çeker. Menü gecikmelerine karşı sunucu senkronizasyon koruması içerir.
* **🚀 Balıkçılık Boost:** Her **70 dakikada bir** `/balıkçılık boost` menüsünü açar ve ilgili slotu alarak balıkçılık hızını artırır.
* **🧱 DirtBot (Toprak Botu):** F tuşundaki (Off-hand) Taşlı Toprağı (`Coarse Dirt`) milisaniyelik hızla boşluğa yerleştirir, kürekle dönüştürüp anında kırarak (Normal Toprak veya Patika Yol) süreci hızlandırır. Envanterde taşlı toprak bittiğinde matrise (`Toprak-Çakıl / Çakıl-Toprak`) göre **otomatik craft yapar** ve kürek canı %5'in altına düştüğünde ya da malzemeler bittiğinde kendini güvenle durdurur.

## ⚙️ Gereksinimler

* Minecraft **1.21.11**
* Fabric Loader (0.19.2+)
* Java 21

## 🎮 Kullanım (Kontroller)

Bot oyun içinde kısayol tuşları ile kontrol edilir:

* <kbd>R</kbd> : **FishBot'u Başlat / Durdur** (Tüm arka plan sayaçlarını sıfırlar ve balık/çiçek/çiftçi döngüsünü başlatır).
* <kbd>T</kbd> : **DirtBot'u (Toprak Botu) Başlat / Durdur** (FishBot'tan tamamen bağımsız olarak çalışır, sol elindeki taşlı toprağı ve küreği kullanarak otomatik toprak döngüsünü yürütür).
* <kbd>G</kbd> : **Balık Modunu Değiştir** * *Mod 1 (XP Repair):* Olta canı %5'in altına düştüğünde envanterden XP şişesi fırlatarak oltayı tamir eder.
  * *Mod 2 (Multi-Rod):* Kırılmak üzere olan oltayı bırakıp envanterdeki diğer sağlam oltaya geçer.
  * *Mod 3 (Dümdüz):* Standart balık tutma modu.

## 🛡️ Güvenlik ve Mimari

Bu mod, sunucu kilitlenmelerini (crash) ve hile koruması (anti-cheat) engellemelerini aşmak için:
* Sayfa geçişlerinde sunucu (ping) gecikmelerini hesaba katarak tick bazlı beklemeler yapar.
* 1 saniyeden uzun süre hedefin dışındaki bloklara bakılması durumunda koruma sayaçları devreye girer.
* `FishbotsClient` üzerinden modüler bir mimariyle başlatılır.