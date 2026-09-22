\# 🎣 FishBot - TurkeyMC Skyblock Otomasyonu



Minecraft Fabric (1.21.11) tabanlı, \*\*TurkeyMC Skyblock\*\* sunucusu için özel olarak geliştirilmiş modüler otomasyon ve asistan modudur. Bot, arka planda güvenli menü kontrolleri ve zamanlayıcılarla çalışarak sunucu ile tam senkronize işlem yapar.



\## ✨ Özellikler



Bot, tek bir ana başlatıcı üzerinden 4 farklı bağımsız modül ile çalışır:



\* \*\*🐟 Gelişmiş Balık Botu:\*\* Suya atılan oltayı takip eder, balık vurduğunda otomatik çeker. Çoklu olta kullanımı ve Mending (Tamir) büyüsü için otomatik XP şişesi fırlatma modlarına sahiptir.

\* \*\*🌻 Çiçek (Mevsim) Otomasyonu:\*\* Her 60 dakikada bir arka planda `/mevsim depo` komutunu çalıştırır ve 51. slota tıklayarak çiçekleri otomatik satar.

\* \*\*🌾 Çiftçi Otomasyonu:\*\* Çakışmaları önlemek için her 47 dakikada bir `/çiftçi` komutunu çalıştırır. Sayfalar arası geçiş yaparak (Shift+Sağ Tık / QUICK\_MOVE) eşyaları oyuncunun üzerine çeker. Menü gecikmelerine karşı sunucu senkronizasyon koruması içerir.

\* \*\*🚀 Balıkçılık Boost:\*\* Her 60 dakikada bir `/balıkçılık boost` menüsünü açar ve ilgili slotu alarak balıkçılık hızını artırır.



\## ⚙️ Gereksinimler



\* Minecraft \*\*1.21.11\*\*

\* Fabric Loader (0.19.5+)

\* Java 21



\## 🎮 Kullanım (Kontroller)



Bot oyun içinde kısayol tuşları ile kontrol edilir. Çiçek, Çiftçi ve Boost modülleri, sadece ana bot aktif olduğunda arka planda süre saymaya başlar.



\* <kbd>R</kbd> : \*\*Botu Başlat / Durdur\*\* (Tüm arka plan sayaçlarını sıfırlar ve başlatır).

\* <kbd>G</kbd> : \*\*Balık Modunu Değiştir\*\* 

&#x20; \* \*Mod 1 (XP Repair):\* Olta canı %5'in altına düştüğünde envanterden XP şişesi fırlatarak oltayı tamir eder.

&#x20; \* \*Mod 2 (Multi-Rod):\* Kırılmak üzere olan oltayı bırakıp envanterdeki diğer sağlam oltaya geçer.

&#x20; \* \*Mod 3 (Dümdüz):\* Standart balık tutma modu.



\## 🛡️ Güvenlik ve Mimari



Bu mod, sunucu kilitlenmelerini (crash) ve hile koruması (anti-cheat) engellemelerini aşmak için:

\* Sayfa geçişlerinde sunucu (ping) gecikmelerini hesaba katarak tick bazlı beklemeler yapar.

\* `currentScreenHandler` kontrolleri ile GUI (Menü) açılmadan oyuncu envanterine tıklama hatalarını engeller.

\* `FishbotsClient` üzerinden modüler bir mimariyle başlatılır.

