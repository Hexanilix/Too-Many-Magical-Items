package org.tmmi;

public enum Property {
    FILEVERSION("file_version", "\"" + Main.FILE_VERSION.toString() + "\" # DO NOT CHANGE"),
    ENABLED("enabled", true),
    AUTOSAVE("autosave", true),
    AUTOSAVE_MSG("autosave_message", true),
    AUTOSAVE_MSG_VALUE("message_value", "Autosaving..."),
    AUTOSAVE_FREQUENCY("autosave_frequency", 1800),
    SPELL_COLLISION("spell_collision", true),
    CUSTOM_SPELLS("enable_custom_spells", false),
    DISABLED_SPELLS("disabled_spells", "\n"),
    SPELL_SPEED_CAP("spell_seed_cap", 20),
    SPELL_TRAVEL_CAP("spell_travel_distance_cap", 20),
    SPELL_DAMAGE_CAP("spell_damage_cap", 20);

    private final String key;
    private final Object value;

    Property(String key, Object value) {
        this.key = key;
        this.value = String.valueOf(value);
    }

    public String key() {
        return key;
    }
    public Object val() {
        return value;
    }
    public String sval() {
        return String.valueOf(value);
    }
}
