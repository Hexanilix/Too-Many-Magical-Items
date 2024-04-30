package org.tmmi;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FileVersion {
    public static double versionDiff(@NotNull FileVersion f1, @NotNull FileVersion f2) {
        double a = 0;
        double b = 0;
        int[] al = f1.getVer();
        int[] bl = f2.getVer();
        for (int i = 1; i <= al.length; i++) a += (double) (al[i-1]*10)/(i*10);
        for (int i = 1; i <= bl.length; i++) b += (double) (bl[i-1]*10)/(i*10);
        return a-b;
    }

    private final int[] ver;
    FileVersion(@NotNull String v) {
        String[] s = v.split("\\.");
        ver = new int[s.length];
        for (int i = 0; i < s.length; i++) ver[i] = Integer.parseInt(s[i]);
    }
    FileVersion(int @NotNull ... versions) {
        ver = new int[versions.length];
        System.arraycopy(versions, 0, ver, 0, versions.length);
    }

    public int[] getVer() {
        return ver;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(ver[0]);
        for (int i = 1; i < ver.length; i++) s.append('.').append(ver[i]);
        return s.toString();
    }
}
