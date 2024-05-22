package org.tmmi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Property<T> {
    public static final List<Property> properties = new ArrayList<>();
    public static final Property<FileVersion> FILE_VERSION = new Property<>("FILE_VERSION", org.tmmi.Main.PLUGIN_VERSION);
    public static final Property<Boolean> ENABLED = new Property<>("ENABLED", true);
    public static final Property<Boolean> GARBAGE_COLLECTION = new Property<>("GARBAGE_COLLECTION", true);
    public static final Property<Boolean> AUTOSAVE = new Property<>("AUTOSAVE", true);
    public static final Property<Boolean> AUTOSAVE_MSG = new Property<>("AUTOSAVE_MSG", true);
    public static final Property<String> AUTOSAVE_MSG_VALUE = new Property<>("AUTOSAVE_MSG_VALUE", "Autosaving...");
    public static final Property<Integer> AUTOSAVE_FREQUENCY = new Property<>("AUTOSAVE_FREQUENCY", 1800);
    public static final Property<Boolean> CUSTOM_SPELLS = new Property<>("CUSTOM_SPELLS", false);
    public static final Property<List<UUID>> DISABLED_SPELLS = new Property<>("DISABLED_SPELLS", new ArrayList<>());
    public static final Property<Double> SPELL_SPEED_CAP = new Property<>("SPELL_SPEED_CAP", 20d);
    public static final Property<Double> SPELL_TRAVEL_CAP = new Property<>("SPELL_TRAVEL_CAP", 20d);
    public static final Property<Double> SPELL_DAMAGE_CAP = new Property<>("SPELL_DAMAGE_CAP", 20d);
    
    
    private final String prop;
    private final T defVal;
    private T value;
    
    Property(String property, T value) {
        this.prop = property;
        this.defVal = value;
        this.value = value;
        properties.add(this);
    }

    public String p() {
        return prop;
    }

    public T d() {
        return defVal;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public T v() {
        return value;
    }

    public void setV(T value) {
        this.value = value;
    }
}
