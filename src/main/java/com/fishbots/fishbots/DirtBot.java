package com.fishbots.fishbots;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class DirtBot {
    public static boolean isEnabled = false;
    private static int nonDirtTimer = 0; // Hedef dışı blok sayacı (1 saniye koruması için)

    public static void toggle(MinecraftClient client) {
        if (!isEnabled) {
            int shovelSlot = findValidShovel(client);
            if (shovelSlot == -1) {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c[FishBot] Hata: Envanterde kullanılabilir kürek bulunamadı!"), false);
                }
                return;
            }
            nonDirtTimer = 0;
        }

        isEnabled = !isEnabled;
        if (client.player != null) {
            client.player.sendMessage(Text.literal(isEnabled ? "§a[FishBot] Toprak Botu Aktif!" : "§c[FishBot] Toprak Botu Kapatıldı!"), false);
        }
    }

    public static void tick(MinecraftClient client) {
        if (!isEnabled || client.player == null || client.world == null || client.interactionManager == null) return;

        int shovelSlot = findValidShovel(client);
        if (shovelSlot == -1) {
            client.player.sendMessage(Text.literal("§c[FishBot] Kürek bitti! Bot durduruldu."), false);
            isEnabled = false;
            return;
        }

        ensureCoarseDirtInOffhand(client);

        HitResult hit = client.player.raycast(4.5D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            nonDirtTimer++;
            if (nonDirtTimer > 20) { // 20 tick = 1 saniye
                // 1 saniyedir hedefte blok yoksa işlem yapma
                return;
            }
            return;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos targetPos = blockHit.getBlockPos();
        Block targetBlock = client.world.getBlockState(targetPos).getBlock();

        if (targetBlock != Blocks.COARSE_DIRT && targetBlock != Blocks.DIRT && targetBlock != Blocks.DIRT_PATH) {
            nonDirtTimer++;
            if (nonDirtTimer > 20) { // 1 saniye dolduysa tıklamayı bırak ve çık
                return;
            }
        } else {
            nonDirtTimer = 0; // Doğru bloksa sayacı sıfırla
        }

        if (targetBlock == Blocks.COARSE_DIRT) {
            int hotbarShovel = getHotbarSlot(shovelSlot);
            if (hotbarShovel != -1) {
                client.player.getInventory().setSelectedSlot(hotbarShovel);
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                client.player.swingHand(Hand.MAIN_HAND);
            } else {
                moveItemToHotbar(client, shovelSlot);
            }
            return;
        }

        if (targetBlock == Blocks.DIRT || targetBlock == Blocks.DIRT_PATH) {
            int hotbarShovel = getHotbarSlot(shovelSlot);
            if (hotbarShovel != -1) {
                client.player.getInventory().setSelectedSlot(hotbarShovel);
                client.interactionManager.updateBlockBreakingProgress(targetPos, blockHit.getSide());
                client.player.swingHand(Hand.MAIN_HAND);
            }
            return;
        }

        BlockPos placePos = targetPos.offset(blockHit.getSide());
        if (client.world.getBlockState(placePos).isAir()) {
            client.interactionManager.interactBlock(client.player, Hand.OFF_HAND, blockHit);
            client.player.swingHand(Hand.OFF_HAND);
        }
    }

    private static int findValidShovel(MinecraftClient client) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() instanceof ShovelItem) {
                return i;
            }
        }
        return -1;
    }

    private static void ensureCoarseDirtInOffhand(MinecraftClient client) {
        ItemStack offhandStack = client.player.getOffHandStack();
        if (offhandStack.getItem() == Items.COARSE_DIRT) {
            return; // Zaten F slotunda var
        }

        int coarseSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (client.player.getInventory().getStack(i).getItem() == Items.COARSE_DIRT) {
                coarseSlot = i;
                break;
            }
        }

        int syncId = client.player.playerScreenHandler.syncId;

        if (coarseSlot != -1) {
            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(coarseSlot), 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(coarseSlot), 0, SlotActionType.PICKUP, client.player);
        } else {
            if (!tryAutoCraftCoarseDirt(client)) {
                client.player.sendMessage(Text.literal("§c[FishBot] Taşlı toprak ve malzemeler bitti! Bot durduruldu."), false);
                isEnabled = false;
            }
        }
    }

    private static boolean tryAutoCraftCoarseDirt(MinecraftClient client) {
        int dirt1 = -1, dirt2 = -1, gravel1 = -1, gravel2 = -1;

        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() == Items.DIRT) {
                if (dirt1 == -1) dirt1 = i;
                else if (dirt2 == -1) dirt2 = i;
            } else if (stack.getItem() == Items.GRAVEL) {
                if (gravel1 == -1) gravel1 = i;
                else if (gravel2 == -1) gravel2 = i;
            }
        }

        if (dirt1 == -1 || dirt2 == -1 || gravel1 == -1 || gravel2 == -1) {
            return false;
        }

        int syncId = client.player.playerScreenHandler.syncId;

        try {
            // Slot 1: Toprak, Slot 2: Çakıl, Slot 3: Çakıl, Slot 4: Toprak
            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(dirt1), 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 1, 0, SlotActionType.PICKUP, client.player);

            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(gravel1), 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 2, 0, SlotActionType.PICKUP, client.player);

            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(gravel2), 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 3, 0, SlotActionType.PICKUP, client.player);

            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(dirt2), 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 4, 0, SlotActionType.PICKUP, client.player);

            // Sonuç slotundan (Slot 0) üretilen Taşlı Toprağı al ve doğrudan F slotuna (45) taşı
            client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
            client.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, client.player);

            // Elde kalan varsa envantere geri bırak
            client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.QUICK_MOVE, client.player);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static int convertInventorySlotToContainer(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) {
            return 36 + inventorySlot;
        } else if (inventorySlot >= 9 && inventorySlot < 36) {
            return inventorySlot;
        }
        return inventorySlot;
    }

    private static int getHotbarSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) return inventorySlot;
        return -1;
    }

    private static void moveItemToHotbar(MinecraftClient client, int inventorySlot) {
        if (inventorySlot >= 9) {
            int syncId = client.player.playerScreenHandler.syncId;
            client.interactionManager.clickSlot(syncId, convertInventorySlotToContainer(inventorySlot), 0, SlotActionType.SWAP, client.player);
        }
    }
}