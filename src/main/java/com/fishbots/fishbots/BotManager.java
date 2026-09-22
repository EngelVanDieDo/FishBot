package com.fishbots.fishbots;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class BotManager {
    public enum Mode {
        PURE_FISHING("Dümdüz Balık (Mod 3)"),
        XP_REPAIR("XP Şişesi ile Tamir (Mod 1)"),
        MULTI_ROD("Çoklu Olta Döngüsü (Mod 2)");

        private final String name;
        Mode(String name) { this.name = name; }
        public String getName() { return name; }
    }

    private static Mode currentMode = Mode.PURE_FISHING;

    public static void cycleMode(MinecraftClient client) {
        Mode oldMode = currentMode;

        if (currentMode == Mode.PURE_FISHING) {
            currentMode = Mode.XP_REPAIR;
        } else if (currentMode == Mode.XP_REPAIR) {
            currentMode = Mode.MULTI_ROD;
        } else {
            currentMode = Mode.PURE_FISHING;
        }

        FishBotLogger.log("[BotManager] Mod değiştirildi: " + oldMode.getName() + " -> " + currentMode.getName());

        if (client.player != null) {
            client.player.sendMessage(Text.literal("§b[FishBots] Mod Değişti: §e" + currentMode.getName()), true);
        }
    }

    public static void switchToMultiRod() {
        if (currentMode != Mode.MULTI_ROD) {
            FishBotLogger.log("[BotManager] Otomatik olarak Çoklu Olta (Mod 2) moduna geçildi!");
            currentMode = Mode.MULTI_ROD;
        }
    }

    public static Mode getCurrentMode() {
        return currentMode;
    }
}