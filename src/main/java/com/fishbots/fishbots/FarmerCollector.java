package com.fishbots.fishbots;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class FarmerCollector {

    private static long lastExecutionTime = System.currentTimeMillis();

    private static final long INTERVAL_MS = 47L * 60 * 1000; // 47 Dakika

    private static int taskState = 0;
    private static int waitTicks = 0;
    private static int menuWaitTimeout = 0; // Menünün gelmesini bekleme sayacı

    public static void init() {
        System.out.println("===========================================");
        System.out.println("[FarmerCollector] ÇİFTÇİ TOPLAMA OTOMASYONU BAŞARIYLA YÜKLENDİ!");
        System.out.println("===========================================");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!Fishbots.isBotActive || Fishbots.isMinigameActive) {
                if (!Fishbots.isBotActive) lastExecutionTime = System.currentTimeMillis();
                return;
            }

            if (waitTicks > 0) {
                waitTicks--;
                return;
            }

            switch (taskState) {
                case 0:
                    // 1. KOMUTU GÖNDER
                    if (System.currentTimeMillis() - lastExecutionTime >= INTERVAL_MS) {
                        FishBotLogger.log("[FarmerCollector] Süre doldu, arka planda /çiftçi yazılıyor...");
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("çiftçi"));
                        }
                        taskState = 1;
                        waitTicks = 10; // Yarım saniye sonra menü kontrolüne başla
                        menuWaitTimeout = 0;
                    }
                    break;

                case 1:
                    if (isChestMenuOpen(client)) {
                        FishBotLogger.log("[FarmerCollector] Çiftçi menüsü doğrulandı. 1. Sayfa toplanıyor...");
                        int syncId = client.player.currentScreenHandler.syncId;

                        client.interactionManager.clickSlot(syncId, 20, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 24, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 34, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 37, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 38, 1, SlotActionType.QUICK_MOVE, client.player);

                        FishBotLogger.log("[FarmerCollector] 2. Sayfaya geçiliyor...");
                        client.interactionManager.clickSlot(syncId, 53, 0, SlotActionType.PICKUP, client.player);

                        taskState = 2;
                        waitTicks = 20; // Sunucunun 2. sayfayı yüklemesi için 1 saniye bekle
                    } else {
                        menuWaitTimeout++;
                        if (menuWaitTimeout > 60) {
                            FishBotLogger.log("[FarmerCollector] HATA: Menü 3 saniye içinde açılmadı, işlem iptal edildi.");
                            taskState = 0;
                            lastExecutionTime = System.currentTimeMillis();
                        } else {
                            waitTicks = 2; // Menü gelene kadar 2 tick aralıklarla kontrol et
                        }
                    }
                    break;

                case 2:
                    // 3. SAYFA 2'Yİ TOPLA
                    if (isChestMenuOpen(client)) {
                        FishBotLogger.log("[FarmerCollector] 2. Sayfa toplanıyor...");
                        int syncId = client.player.currentScreenHandler.syncId;

                        client.interactionManager.clickSlot(syncId, 14, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 15, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 16, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 20, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 28, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 31, 1, SlotActionType.QUICK_MOVE, client.player);
                        client.interactionManager.clickSlot(syncId, 39, 1, SlotActionType.QUICK_MOVE, client.player);

                        FishBotLogger.log("[FarmerCollector] 3. Sayfaya geçiliyor...");
                        client.interactionManager.clickSlot(syncId, 53, 0, SlotActionType.PICKUP, client.player);

                        taskState = 3;
                        waitTicks = 20; // 3. sayfanın yüklenmesi için 1 saniye bekle
                    } else {
                        taskState = 0;
                        lastExecutionTime = System.currentTimeMillis();
                    }
                    break;

                case 3:
                    if (isChestMenuOpen(client)) {
                        FishBotLogger.log("[FarmerCollector] 3. Sayfa toplanıyor...");
                        int syncId = client.player.currentScreenHandler.syncId;

                        // 3. Sayfa Eşyası (Shift + Sağ Tık = 1)
                        client.interactionManager.clickSlot(syncId, 28, 1, SlotActionType.QUICK_MOVE, client.player);

                        taskState = 4;
                        waitTicks = 10;
                    } else {
                        taskState = 0;
                        lastExecutionTime = System.currentTimeMillis();
                    }
                    break;

                case 4:
                    // 5. MENÜYÜ KAPAT
                    if (client.player != null && isChestMenuOpen(client)) {
                        FishBotLogger.log("[FarmerCollector] İşlem tamam, menü kapatılıyor...");
                        client.player.closeHandledScreen();
                    }
                    taskState = 0;
                    lastExecutionTime = System.currentTimeMillis();
                    break;
            }
        });
    }

    // KESİN GÜVENLİK KONTROLÜ: Ekranda gerçekten harici bir sandık menüsü var mı?
    private static boolean isChestMenuOpen(MinecraftClient client) {
        if (client.player == null || client.player.currentScreenHandler == null) return false;

        // Açık olan menü oyuncunun kendi envanteri DEĞİLSE ve slot sayısı en az 54 ise
        return !(client.player.currentScreenHandler instanceof PlayerScreenHandler)
                && client.player.currentScreenHandler.slots.size() >= 54;
    }
}