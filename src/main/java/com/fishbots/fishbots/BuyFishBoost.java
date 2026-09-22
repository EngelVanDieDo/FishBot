package com.fishbots.fishbots;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;


public class BuyFishBoost {

    private static long lastExecutionTime = System.currentTimeMillis();

    // TEST İÇİN 10 SANİYE (Hemen test edebilmen için. Çalıştığını görünce 60 * 60 * 1000 yaparsın)
    private static final long INTERVAL_MS =  60 * 60 * 1000;

    private static int taskState = 0;
    private static int waitTicks = 0;

    public static void init() {
        System.out.println("===========================================");
        System.out.println("[FishBoost] ÇİÇEK OTOMASYONU BAŞARIYLA YÜKLENDİ!");
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
                    if (System.currentTimeMillis() - lastExecutionTime >= INTERVAL_MS) {
                        FishBotLogger.log("[FishBoost] Süre doldu, arka planda /balıkçılık boost yazılıyor...");
                        if (client.getNetworkHandler() != null) {
                            client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("balıkçılık boost"));
                        }
                        taskState = 1;
                        waitTicks = 30; // 1.5 saniye bekle
                    }
                    break;

                case 1:
                    if (client.player != null && client.player.currentScreenHandler != null) {
                        FishBotLogger.log("[FishBoost] Menü algılandı. 24. Slota tıklanıyor...");
                        int syncId = client.player.currentScreenHandler.syncId;

                        client.interactionManager.clickSlot(syncId, 15, 0, SlotActionType.PICKUP, client.player);

                        taskState = 2;
                        waitTicks = 10; // 0.5 saniye bekle
                    } else {
                        FishBotLogger.log("[FishBoost] HATA: Menü açılamadı, iptal ediliyor.");
                        taskState = 0;
                        lastExecutionTime = System.currentTimeMillis();
                    }
                    break;

                case 2:
                    if (client.player != null) {
                        FishBotLogger.log("[FishBoost] İşlem tamam, menü kapatılıyor...");
                        client.player.closeHandledScreen();
                    }
                    taskState = 0;
                    lastExecutionTime = System.currentTimeMillis();
                    break;
            }
        });
    }
    
}
