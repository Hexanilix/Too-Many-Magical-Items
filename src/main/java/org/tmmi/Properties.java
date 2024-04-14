package org.tmmi;

public enum Properties {
    COMMENT("# "),
    ENABLED("enabeled"),
    FILEVERSION("fileversion"),
    AUTOSAVE("autosave"),
    AUTOSAVE_MSG("autosaveMessage"),
    AUTOSAVE_FREQUENCY("autosaveFrequency");

    private final String key;

    Properties(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
