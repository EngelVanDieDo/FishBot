package com.fishbots.fishbots.client;

import net.fabricmc.api.ClientModInitializer;
import com.fishbots.fishbots.Fishbots;
import com.fishbots.fishbots.FlowerAutomator;
import com.fishbots.fishbots.FarmerCollector;
import com.fishbots.fishbots.BuyFishBoost;

public class FishbotsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("===========================================");
        System.out.println("[FishBots] ANA SİSTEM BAŞLATILIYOR...");
        System.out.println("===========================================");

        // 1. Balık Botunu Başlat
        Fishbots.init();

        // 2. Çiçek Otomasyonunu Başlat
        FlowerAutomator.init();

        // 3. Çiftçi Otomasyonunu Başlat
        FarmerCollector.init();

        // 4. Balık Boost Otomasyonunu Başlat
        BuyFishBoost.init();

        System.out.println("TÜM MODÜLLER BAŞARIYLA YÜKLENDİ!");
    }
}