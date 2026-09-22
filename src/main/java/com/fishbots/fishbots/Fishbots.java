package com.fishbots.fishbots;

import com.fishbots.fishbots.mixin.PlayerInventoryAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Fishbots {
    private static KeyBinding toggleBotKey;
    private static KeyBinding modeSwitchKey;

    public static boolean isBotActive = false;
    public static boolean isMinigameActive = false;

    private static int waitTimer = 0;
    private static int botState = 0;
    private static int minigameTimeoutTimer = 0;

    public static void init() {
        KeyBinding.Category fishbotsCategory = KeyBinding.Category.create(Identifier.of("fishbots", "category"));

        toggleBotKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Botu Aç/Kapat",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                fishbotsCategory
        ));

        modeSwitchKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Modu Değiştir",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                fishbotsCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // 1. AÇMA KAPATMA KONTROLÜ
            while (toggleBotKey.wasPressed()) {
                if (!isBotActive) {
                    FishBotLogger.log("[FishBot] Bot açılıyor... Mod: " + BotManager.getCurrentMode().getName());
                    if (!hasValidRod(client)) {
                        FishBotLogger.log("[FishBot] HATA: Şartlara uygun olta bulunamadı!");
                        if (client.player != null) client.player.sendMessage(Text.literal("§c[FishBot] Uygun olta bulunamadı!"), true);
                        continue;
                    }

                    isBotActive = true;
                    botState = 0;
                    waitTimer = 0;
                    minigameTimeoutTimer = 0;
                    isMinigameActive = false;
                    FishBotLogger.log("[FishBot] Bot AKTİF EDİLDİ.");

                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§a[FishBot] Aktif Edildi! (" + BotManager.getCurrentMode().getName() + ")"), true);
                    }
                } else {
                    isBotActive = false;
                    FishBotLogger.log("[FishBot] Bot KAPATILDI.");
                    if (client.player != null) {
                        if (client.player.fishHook != null && client.interactionManager != null) {
                            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                            client.player.swingHand(Hand.MAIN_HAND);
                            FishBotLogger.log("[FishBot] Kapatılırken sudaki olta geri çekildi.");
                        }
                        client.player.sendMessage(Text.literal("§c[FishBot] Kapatıldı!"), true);
                    }
                }
            }

            // 2. MOD DEĞİŞTİRME VE AKTİFKEN ANINDA ŞART KONTROLÜ
            while (modeSwitchKey.wasPressed()) {
                BotManager.cycleMode(client);

                if (isBotActive) {
                    FishBotLogger.log("[FishBot] Bot aktifken mod değiştirildi. Şartlar kontrol ediliyor...");
                    if (!hasValidRod(client)) {
                        FishBotLogger.log("[FishBot] HATA: Yeni modun şartlarına uygun olta bulunamadı! Bot durduruluyor.");
                        if (client.player != null) {
                            client.player.sendMessage(Text.literal("§c[FishBot] Yeni mod için uygun olta bulunamadı! Bot kapatıldı."), true);

                            if (client.player.fishHook != null && client.interactionManager != null) {
                                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                                client.player.swingHand(Hand.MAIN_HAND);
                                FishBotLogger.log("[FishBot] Yeni modda şartlar sağlanmadığı için sudaki olta geri çekildi.");
                            }
                        }
                        isBotActive = false;
                        botState = 0;
                        isMinigameActive = false;
                    } else {
                        FishBotLogger.log("[FishBot] Yeni mod için şartlar uygun, işleme devam ediliyor.");
                    }
                }
            }

            if (!isBotActive || client.player == null || client.interactionManager == null) return;

            if (isMinigameActive) {
                minigameTimeoutTimer++;
                if (minigameTimeoutTimer > 200) {
                    FishBotLogger.log("[FishBot] DİKKAT: Mini-oyun zaman aşımına uğradı! Kilit zorla açılıyor.");
                    isMinigameActive = false;
                    botState = 0;
                    waitTimer = 10;
                }
                return;
            } else {
                minigameTimeoutTimer = 0;
            }

            BotManager.Mode currentMode = BotManager.getCurrentMode();
            if (currentMode == BotManager.Mode.XP_REPAIR) {
                ItemStack mainStack = client.player.getMainHandStack();
                if (getDurabilityPercentage(mainStack) < 0.05) {
                    FishBotLogger.log("[FishBot] Mod 1: Olta canı %5 altına düştü, Mod 2'ye geçiliyor.");
                    client.player.sendMessage(Text.literal("§e[FishBot] Olta canı %5 altına düştü! Mod 2'ye (Çoklu Olta) geçiliyor..."), true);
                    BotManager.switchToMultiRod();
                }
            }

            FishingBobberEntity bobber = client.player.fishHook;
            if (waitTimer > 0) { waitTimer--; return; }

            switch (botState) {
                case 0:
                    if (bobber == null) {
                        if (currentMode == BotManager.Mode.XP_REPAIR) {
                            if (handleXpRepair(client)) {
                                waitTimer = 30;
                                break;
                            }
                        }

                        FishBotLogger.log("[FishBot] [State 0] Olta fırlatılıyor...");
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        client.player.swingHand(Hand.MAIN_HAND);
                        botState = 1;
                        waitTimer = 10;
                    } else {
                        botState = 1;
                    }
                    break;
                case 1:
                    if (bobber != null) {
                        botState = 2;
                        waitTimer = 40;
                    } else {
                        botState = 0;
                    }
                    break;
                case 2:
                    if (bobber == null) {
                        botState = 0;
                    } else if (bobber.getVelocity().y < -0.04) {
                        FishBotLogger.log("[FishBot] [State 2] Şamandıra battı (Balık vurdu)! Oltaya tıklanıyor...");
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        client.player.swingHand(Hand.MAIN_HAND);

                        botState = 0;
                        isMinigameActive = true;
                        minigameTimeoutTimer = 0;
                        waitTimer = 20;
                    }
                    break;
            }
        });
    }

    public static boolean handleXpRepair(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return false;

        ItemStack mainStack = client.player.getMainHandStack();
        if (!mainStack.isOf(Items.FISHING_ROD)) return false;

        double durability = getDurabilityPercentage(mainStack);
        if (durability >= 0.90) return false;

        FishBotLogger.log("[Mod 1] Olta canı %" + (int)(durability * 100) + ". XP aranıyor...");

        int xpBottleSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (client.player.getInventory().getStack(i).isOf(Items.EXPERIENCE_BOTTLE)) {
                xpBottleSlot = i;
                break;
            }
        }

        if (xpBottleSlot == -1) {
            FishBotLogger.log("[Mod 1] Envanterde XP şişesi bulunamadı!");
            return false;
        }

        FishBotLogger.log("[Mod 1] XP şişesi fırlatılıyor (Slot: " + xpBottleSlot + ")...");

        PlayerInventoryAccessor inv = (PlayerInventoryAccessor) client.player.getInventory();
        int originalSlot = inv.getSelectedSlot();
        int targetHotbarSlot = -1;
        boolean moved = false;

        if (xpBottleSlot < 9) {
            targetHotbarSlot = xpBottleSlot;
        } else {
            for (int i = 0; i < 9; i++) {
                if (!client.player.getInventory().getStack(i).isOf(Items.FISHING_ROD)) {
                    targetHotbarSlot = i;
                    break;
                }
            }
            if (targetHotbarSlot == -1) targetHotbarSlot = 0;

            int syncId = client.player.playerScreenHandler.syncId;
            int sourceContainerSlot = xpBottleSlot;
            int targetContainerSlot = 36 + targetHotbarSlot;

            client.interactionManager.clickSlot(syncId, sourceContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, targetContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, sourceContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
            moved = true;
        }

        inv.setSelectedSlot(targetHotbarSlot);

        float originalPitch = client.player.getPitch();
        client.player.setPitch(90.0f);

        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        client.player.swingHand(Hand.MAIN_HAND);

        client.player.setPitch(originalPitch);
        inv.setSelectedSlot(originalSlot);

        if (moved) {
            int syncId = client.player.playerScreenHandler.syncId;
            int sourceContainerSlot = xpBottleSlot;
            int targetContainerSlot = 36 + targetHotbarSlot;

            client.interactionManager.clickSlot(syncId, sourceContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, targetContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, sourceContainerSlot, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, client.player);
        }

        return true;
    }

    public static double getDurabilityPercentage(ItemStack stack) {
        int maxDmg = stack.getMaxDamage();
        int currentDmg = stack.getDamage();
        if (maxDmg <= 0) return 1.0;
        return 1.0 - ((double) currentDmg / maxDmg);
    }

    private static boolean hasMendingEnchantment(MinecraftClient client, ItemStack stack) {
        if (client.world == null) return false;
        var enchantmentsComponent = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchantmentsComponent == null) return false;
        for (var entry : enchantmentsComponent.getEnchantments()) {
            if (entry.matchesKey(Enchantments.MENDING)) return true;
        }
        return false;
    }

    private static boolean hasValidRod(MinecraftClient client) {
        if (client.player == null) return false;
        BotManager.Mode currentMode = BotManager.getCurrentMode();
        if (currentMode == BotManager.Mode.MULTI_ROD) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getStack(i);
                if (stack.getItem() == Items.FISHING_ROD && getDurabilityPercentage(stack) >= 0.10) return true;
            }
            return false;
        } else if (currentMode == BotManager.Mode.XP_REPAIR) {
            ItemStack mainStack = client.player.getMainHandStack();
            if (mainStack.isOf(Items.FISHING_ROD)) {
                return hasMendingEnchantment(client, mainStack) && getDurabilityPercentage(mainStack) >= 0.05;
            }
            return false;
        } else {
            ItemStack mainStack = client.player.getMainHandStack();
            ItemStack offStack = client.player.getOffHandStack();
            ItemStack targetStack = mainStack.isOf(Items.FISHING_ROD) ? mainStack : (offStack.isOf(Items.FISHING_ROD) ? offStack : null);
            return targetStack != null && getDurabilityPercentage(targetStack) >= 0.10;
        }
    }
}