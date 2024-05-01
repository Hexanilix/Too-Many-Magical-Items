package org.tmmi;

import org.jetbrains.annotations.NotNull;

public class FileVersion {
    public static class FileVersionFormatException extends Exception {
        public FileVersionFormatException(String e) {
            super(e);
        }
    }
    public enum VDating {
        NEWER,
        SAME,
        OBSOLETE;
    }
    public static VDating versionDiff(@NotNull FileVersion comp, @NotNull FileVersion base) {
        for (int i = 0; i < Math.max(comp.ver.length, base.ver.length); i++) {
            int v1 = (i < comp.ver.length ? comp.ver[i] : 0);
            int v2 = (i < base.ver.length ? base.ver[i] : 0);
            if (v1 > v2) return VDating.NEWER;
            else if (v1 < v2) return VDating.OBSOLETE;
        }
        return VDating.SAME;
    }

    private final int[] ver;
    FileVersion(@NotNull String v) {
        String[] s = v.split("\\.");
        ver = new int[s.length];
        try {
            for (int i = 0; i < s.length; i++) {
                try {
                    ver[i] = Integer.parseInt(s[i]);
                } catch (NumberFormatException e) {
                    throw new FileVersionFormatException("Cannot convert String \"" + v + "\" to FileVersion. Supported char combination: x.x.x (n numbers separated by a '.'");
                }
            }
        } catch (FileVersionFormatException e) {
            e.printStackTrace();
        }
    }
    FileVersion(int @NotNull ... versions) {
        ver = new int[versions.length];
        System.arraycopy(versions, 0, ver, 0, versions.length);
    }

    public int[] getVer() {
        return ver;
    }

    public String toString() {
        StringBuilder s = new StringBuilder().append(ver[0]);
        for (int i = 1; i < ver.length; i++) s.append('.').append(ver[i]);
        return s.toString();
    }
}
