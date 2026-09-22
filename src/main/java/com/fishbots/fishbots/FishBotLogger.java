package com.fishbots.fishbots;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FishBotLogger {
    // Logların kaydedileceği dosyanın adı. Oyunun ana klasörüne (veya IDE'de 'run' klasörüne) kaydedilir.
    private static final String FILE_PATH = "fishbot_afk_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void log(String message) {
        // Dosyaya ekleme (append) modunda yazar
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {

            String time = LocalDateTime.now().format(FORMATTER);
            String finalMessage = "[" + time + "] " + message;

            pw.println(finalMessage); // Txt dosyasına yaz
            System.out.println(finalMessage); // İstersen yine de konsolda görebilmen için buraya da yazdırıyoruz

        } catch (IOException e) {
            System.out.println("Log dosyaya yazılamadı: " + e.getMessage());
        }
    }
}