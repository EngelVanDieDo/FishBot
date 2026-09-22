package com.fishbots.fishbots.mixin;

import com.fishbots.fishbots.BotManager;
import com.fishbots.fishbots.FishBotLogger;
import com.fishbots.fishbots.Fishbots;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Timer;
import java.util.TimerTask;

@Mixin(ClientPlayNetworkHandler.class)
public class TitleS2CPacketMixin {

    private static long lastPullTime = 0;
    private static long lastCatchTime = 0;

    private static boolean isInventoryFull(MinecraftClient client) {
        if (client.player == null) return false;
        int occupiedSlots = 0;
        for (int i = 0; i < 36; i++) {
            if (!client.player.getInventory().getStack(i).isEmpty()) occupiedSlots++;
        }
        double occupancyRate = (double) occupiedSlots / 36.0;
        FishBotLogger.log("[Mixin] Envanter doluluk oranı: %" + (int)(occupancyRate * 100));
        return occupancyRate >= 0.80;
    }

    private static boolean handleXpRepair(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return false;
        int xpBottleSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (client.player.getInventory().getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) {
                xpBottleSlot = i;
                break;
            }
        }
        if (xpBottleSlot != -1) {
            FishBotLogger.log("[Mixin] XP şişesi bulundu, fırlatılıyor...");
            final int targetSlot = xpBottleSlot;
            client.execute(() -> {
                PlayerInventoryAccessor inv = (PlayerInventoryAccessor) client.player.getInventory();
                int originalSlot = inv.getSelectedSlot();
                if (targetSlot < 9) inv.setSelectedSlot(targetSlot);

                float originalPitch = client.player.getPitch();
                client.player.setPitch(90.0f);

                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                client.player.swingHand(Hand.MAIN_HAND);

                client.player.setPitch(originalPitch);
                inv.setSelectedSlot(originalSlot);
            });
            return true; // Şişe fırlatıldı
        }
        return false;
    }

    private static boolean handleMultiRod(MinecraftClient client) {
        if (client.player == null) return false;
        PlayerInventoryAccessor inv = (PlayerInventoryAccessor) client.player.getInventory();
        int currentSlot = inv.getSelectedSlot();
        ItemStack currentItem = client.player.getInventory().getStack(currentSlot);
        boolean needsSwitch = false;

        if (currentItem.getItem() == Items.FISHING_ROD) {
            if (currentItem.getMaxDamage() > 0 && (1.0 - ((double) currentItem.getDamage() / currentItem.getMaxDamage())) < 0.10) {
                FishBotLogger.log("[Mixin] Mevcut olta aşındı, değişim gerekiyor.");
                needsSwitch = true;
            }
        } else {
            needsSwitch = true;
        }

        if (needsSwitch) {
            int bestRodSlot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (stack.getItem() == Items.FISHING_ROD && stack.getMaxDamage() > 0 && (1.0 - ((double) stack.getDamage() / stack.getMaxDamage())) >= 0.10) {
                    bestRodSlot = i;
                    break;
                }
            }
            if (bestRodSlot != -1) {
                FishBotLogger.log("[Mixin] Sağlam olta seçiliyor -> Slot: " + bestRodSlot);
                inv.setSelectedSlot(bestRodSlot);
                return true;
            } else {
                FishBotLogger.log("[Mixin] HATA: Sağlam olta kalmadı!");
                client.player.sendMessage(net.minecraft.text.Text.literal("§c[FishBots] Sağlam olta kalmadı! Bot kapatılıyor."), true);
                return false;
            }
        }
        return true;
    }

    @Inject(method = "onTitle", at = @At("HEAD"))
    private void onTitle(TitleS2CPacket packet, CallbackInfo ci) {
        if (!Fishbots.isBotActive) return;

        if (packet.text() != null) {
            String titleText = packet.text().getString();
            long currentTime = System.currentTimeMillis();
            MinecraftClient client = MinecraftClient.getInstance();

            // MİNİ OYUN BAŞLADIYSA FISHBOTS KİLİTLENİR
            if (titleText.contains("Bekle") || titleText.contains("Çek!") || titleText.contains("Isabet") || titleText.contains("Yakaladın!") || titleText.contains("Balık Kaçtı!")) {
                Fishbots.isMinigameActive = true;
            }

            // ÇİFT TIKLAMA HIZLI TEPKİ
            if (titleText.contains("Çek!")) {
                if (currentTime - lastPullTime > 50) {
                    FishBotLogger.log("[Mixin] 'Çek!' yakalandı, ÇİFT TIKLAMA yapılıyor.");
                    lastPullTime = currentTime;

                    if (client != null && client.interactionManager != null && client.player != null) {
                        client.execute(() -> {
                            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                            client.player.swingHand(Hand.MAIN_HAND);
                        });

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                client.execute(() -> {
                                    if (client.interactionManager != null && client.player != null) {
                                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                                        client.player.swingHand(Hand.MAIN_HAND);
                                    }
                                });
                            }
                        }, 50);
                    }
                }
            }
            // OYUN BİTTİ, KİLİDİ AÇ
            else if (titleText.contains("Yakaladın!") || titleText.contains("Balık Kaçtı!")) {
                if (currentTime - lastCatchTime > 3000) {
                    FishBotLogger.log("[Mixin] Mini-oyun sonlandı (" + titleText + ").");
                    lastCatchTime = currentTime;

                    if (client != null && client.getNetworkHandler() != null && client.player != null) {
                        if (isInventoryFull(client)) {
                            FishBotLogger.log("[Mixin] Envanter dolu, balık satılıyor...");
                            client.execute(() -> client.getNetworkHandler().sendPacket(new CommandExecutionC2SPacket("balık sat")));
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() { endMinigameAndUnlock(client); }
                            }, 2500);
                        } else {
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() { endMinigameAndUnlock(client); }
                            }, 1000);
                        }
                    }
                }
            }
        }
    }

    private static void endMinigameAndUnlock(MinecraftClient client) {
        BotManager.Mode currentMode = BotManager.getCurrentMode();
        client.execute(() -> {
            if (client.interactionManager == null || client.player == null) return;

            if (currentMode == BotManager.Mode.MULTI_ROD) {
                if (!handleMultiRod(client)) {
                    Fishbots.isBotActive = false;
                    return;
                }
                FishBotLogger.log("[Mixin] Çoklu Olta kontrolü bitti, kilit açılıyor.");
                Fishbots.isMinigameActive = false; // KİLİDİ AÇ (Ana Beyin olta atacak)
            }
            else if (currentMode == BotManager.Mode.XP_REPAIR) {
                boolean threw = handleXpRepair(client);
                if (threw) {
                    // Şişenin kırılması için 1.5 saniye bekle, sonra Ana Beyni uyandır
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            client.execute(() -> {
                                FishBotLogger.log("[Mixin] Şişe kırıldı, kilit açılıyor.");
                                Fishbots.isMinigameActive = false;
                            });
                        }
                    }, 1500);
                } else {
                    FishBotLogger.log("[Mixin] Şişe yok, kilit açılıyor.");
                    Fishbots.isMinigameActive = false;
                }
            }
            else {
                FishBotLogger.log("[Mixin] Dümdüz Balık (Mod 3), kilit açılıyor.");
                Fishbots.isMinigameActive = false; // KİLİDİ AÇ
            }
            // NOT: Artık Mixin olta fırlatmıyor. Kilidi açıyor, Fishbots Ana Beyin oltayı kendisi atıyor.
        });
    }
}