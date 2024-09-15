package org.tmmi;

import org.hetils.FileVersion;
import org.hetils.HTTPDownloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

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
        File directory = new File("./plugins/");
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File f : fList) {
                if (f.exists() && f.isFile()) {
                    log(f);
                    String[] n = f.getName().split("\\.");
                    if (Objects.equals(n[n.length - 1], "jar")) {
                        for (String s : f.getName().split("-")) {
                            try {
                                if (new org.hetils.FileVersion(false,
                                        s.replace("v", "").replace("P", ""))
                                        .versionDiff(PLUGIN_VERSION) == 1)
                                    return true;
                            } catch (FileVersion.FileVersionFormatException ignore) {}
                        }
                    }
                }
            }
        return false;
    }
}
