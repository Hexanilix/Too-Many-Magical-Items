package org.tmmi;

public enum Properties {
    COMMENT("# "),
    ENABLED("enabled"),
    FILEVERSION("file_version"),
    AUTOSAVE("autosave"),
    AUTOSAVE_MSG("autosave_message"),
    AUTOSAVE_MSG_VALUE("message_value"),
    AUTOSAVE_FREQUENCY("autosave_frequency"),
    SPELL_COLLISION("spell_collision"),
    DISABLED_SPELLS("disabled_spells");

    private final String key;

    Properties(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
