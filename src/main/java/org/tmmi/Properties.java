package org.tmmi;

public enum Properties {
    COMMENT("# "),
    ENABLED("enabled"),
    FILEVERSION("fileversion"),
    AUTOSAVE("autosave"),
    AUTOSAVE_MSG("autosaveMessage"),
    AUTOSAVE_FREQUENCY("autosaveFrequency"),
    SPELL_COLLISION("spellCollision"),
    ;

    private final String key;

    Properties(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
