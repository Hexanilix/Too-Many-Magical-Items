package org.tmmi;

import org.hetils.FileVersion;
import org.hetils.HTTPDownloader;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.tmmi.Main.PLUGIN_VERSION;
import static org.tmmi.Main.log;

public class Updator {
    public static FileVersion newestV = PLUGIN_VERSION;
    public static boolean update() {
        if (checkForUpdates() != -1) return false;
        HTTPDownloader.downloadFile(
                "https://github.com/Hexanilix/Too-Many-Magical-Items/releases/download/Pre-alpha/TMMI-1.20.6-v"+newestV.toString()+"P-alpha-jar-with-dependencies.jar",
                "./TMMI-1.20.6-v1.0.0P-alpha-jar-with-dependencies.jar"
        );
        return true;
    }

    public static int checkForUpdates() {
        try {
            URL url = new URL("https://github.com/Hexanilix/Too-Many-Magical-Items/raw/master/src/main/resources/plugin_versions.txt");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            newestV = new FileVersion(in.readLine());
            in.close();
            connection.disconnect();
            return PLUGIN_VERSION.versionDiff(newestV);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -2;
    }

    public static boolean checkForBetterInstalledVersions() {
        return lookForDuplicates() == 1;
    }
    public static int lookForDuplicates() {
        int best = -2;
        File directory = new File("./plugins/");
        File[] fList = directory.listFiles();
        if (fList != null)
            for (File f : fList) {
                if (f.exists() && f.isFile() && Objects.equals(getLastArrayItem(f.getName().split("\\.")), "jar")) {
                    try (ZipFile jarFile = new ZipFile(f)) {
                        ZipEntry entry = jarFile.getEntry("plugin.yml");
                        if (entry == null) continue;
                        BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (!line.strip().toLowerCase().contains("tmmi")) break;
                            if (line.contains("version:")) {
                                int r = new org.hetils.FileVersion(line.strip().replace("version:", ""))
                                        .versionDiff(PLUGIN_VERSION);
                                if (r > best) best = r;
                            }
                        }
                    } catch (IOException ignore) {}
                }
            }
        return best;
    }

    @Contract(pure = true)
    public static <T> T getLastArrayItem(T @NotNull [] array) {
        return array[array.length-1];
    }
}
