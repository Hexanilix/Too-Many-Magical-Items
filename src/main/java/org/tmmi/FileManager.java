package org.tmmi;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hetils.FileVersion;
import org.hetils.Property;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tmmi.block.*;
import org.tmmi.spell.*;
import org.tmmi.spell.atributes.Type;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static org.tmmi.Element.getElement;
import static org.tmmi.Main.*;
import static org.tmmi.WeavePlayer.getWeaver;

public class FileManager {
    public final Plugin p;
    public String DTFL;
    public String CONF_FILE;
    public String BLOCK_DATAFILE;
    public String PLAYER_DATA_FOLDER;
    public FileManager(@NotNull Plugin p) {
        this.p = p;
        this.DTFL = p.getDataFolder().getAbsolutePath() + "\\";
        this.CONF_FILE = this.DTFL + "config.yml";
    }
    public void createFiles() {
        if (!Files.exists(Path.of(this.DTFL))) {
            if (new File(this.DTFL).mkdir()) {
                log("Created data folder");
            } else {
                log(Level.SEVERE, "Created data folder");
            }
        }
        if (!Files.exists(Path.of(CONF_FILE))) {
            try {
                Files.createFile(Path.of(CONF_FILE));
                this.updateConfig();
                log(Level.WARNING, "Created new config file since it was absent");
            } catch (IOException e) {
                p.onDisable();
                log(Level.SEVERE, "Could not create config file at '" + CONF_FILE + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                return;
            }
            try {
                FileWriter writer = new FileWriter(CONF_FILE);
                for (Property prop : Property.properties) writer.append(prop.p()).append(": ").append(prop.toString()).append("\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                p.onDisable();
            }
        }
    }
    int CloadAttms = 0;
    public boolean loadConfig() {
        if (!Files.exists(Path.of(CONF_FILE))) {
            try {
                Files.createFile(Path.of(CONF_FILE));
                this.updateConfig();
                log(Level.WARNING, "Created new config file since it was absent");
            } catch (IOException e) {
                p.onDisable();
                log(Level.SEVERE, "Could not create config file at '" + CONF_FILE + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
            }
            try {
                FileWriter writer = new FileWriter(CONF_FILE);
                for (Property prop : Property.properties) writer.append(prop.p()).append(": ").append(prop.toString()).append("\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                p.onDisable();
            }
        }
        try (InputStream input = new FileInputStream(CONF_FILE)) {
            HashMap<String, Object> l = new Yaml().load(input);
            if (l == null) return false;
            if (l.isEmpty()) {
                if (CloadAttms == 3) return false;
                CloadAttms++;
                this.updateConfig();
                return loadConfig();
            }
            for (Map.Entry<String, Object> s : l.entrySet())
                for (Property pr : Property.properties)
                    if (Objects.equals(pr.p(), s.getKey()))
                        switch (s.getKey()) {
                            case "FILE_VERSION" -> pr.setV(new FileVersion(String.valueOf(s.getValue())));
                            default -> pr.setV(s.getValue());
                        }
            return true;
        }  catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateConfig() {
        FileVersion old = FILE_VERSION.v();
        try (InputStream input = new FileInputStream(CONF_FILE)) {
            FILE_VERSION.setV(FILES_VERSION);
            FileWriter writer = new FileWriter(CONF_FILE);
            writer.append("# Last automatic modification: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("\n");
            HashMap<String, Object> l = new Yaml().load(input);
            if (l == null) l = new HashMap<>();
            for (Property pr : Property.properties)
                if (!l.containsKey(pr.p())) writer.append(pr.p()).append(": ").append(pr.toString()).append("\n");
            writer.close();
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            FILE_VERSION.setV(old);
            p.onDisable();
        }
        if (!ENABLED.v()) {
            log(Level.WARNING, "Plugin is soft disabled in config, make sure this is a change you wanted");
            p.onDisable();
        }
    }

    public boolean savePlayerData(@NotNull Player p) {
        return savePlayerData(p.getUniqueId());
    }

    public boolean savePlayerData(@NotNull UUID id) {
        File file = new File(DTFL + "playerdata/" + id + ".json");
        try {
            if (!file.exists()) file.createNewFile();
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                WeavePlayer w = getWeaver(id);
                String json = "{\n";
                if (w != null)
                    json += w.toJSON();
                json += "\n}";
                writer.write(json); writer.close();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public boolean saveData() {
        boolean complete = true;
        for (Player p : Bukkit.getOnlinePlayers())
            if (!savePlayerData(p)) complete = false;
        complete = saveBlockData() && complete;
        return complete;
    }
    public void loadBlockData() {
        File file = new File(BLOCK_DATAFILE);
        if (file.exists()) {
            try {
                JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                try {
                    if (!json.has("blocks")) return;
                    JSONArray ar = json.getJSONArray("blocks");
                    for (int i = 0; i < ar.length(); i++) {
                        JSONObject j = ar.getJSONObject(i);
                        try {
                            Block b;
                            org.tmmi.block.Type st = org.tmmi.block.Type.getType(j.getString("type"));
                            World w = Bukkit.getWorld(j.getString("world"));
                            if (w == null) {
                                log("Unknown world \"" + j.getString("world") + "\". Omitting");
                                continue;
                            }
                            Location loc = new Location(w,
                                    j.getDouble("x"),
                                    j.getDouble("y"),
                                    j.getDouble("z"));
                            if (st == null) {
                                log("Unknown block type \"" + Type.getType(j.getString("type")) + "\". Omitting");
                                continue;
                            }
                            switch (st) {
                                case CRAFTINGCAULDRON -> b = new CraftingCauldron(loc);
                                case FORCEFIELD -> b = new ForceField(loc);
                                case SPELLSUCKER -> new SpellAbsorbingBlock(loc);
                                case SPELLWEAVER -> new SpellWeaver(loc);
                                case WEAVINGTABLE -> new WeavingTable(loc);
                                case MANACAULDRON -> new ManaCauldron(loc, j.getInt("mana"));
//                                case PRESENCE_DETECTOR -> new Presence(loc);
                            }
                        } catch (JSONException ignore) {}
                    }
                } catch (JSONException ignore) {}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log(Level.SEVERE, "Block data file doesn't exist and couldn't be created at " + file.getAbsolutePath() + '\n' + e);
            }
        }
    }
    public boolean saveBlockData() {
        File file = new File(BLOCK_DATAFILE);
        try {
            if (!file.exists()) file.createNewFile();
            if (file.exists()) {
                FileWriter writer = new FileWriter(file);
                String json = "{\n";
                List<String> blocks = new ArrayList<>();
                for (Block b : Block.instances)
                    blocks.add(String.join("\n\t\t", b.toJSON().split("\n")));
                json += "\t\"blocks\": [\n" + String.join(",\n", blocks) + "\n\t]";
                json += "\n}";
                writer.write(json);
                writer.close();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public void loadPlayerSaveData(@NotNull Player p) {
        loadPlayerSaveData(p.getUniqueId());
    }
    public void loadPlayerSaveData(UUID id) {
        if (getWeaver(id) != null) return;
        File folder = new File(PLAYER_DATA_FOLDER);
        File file = new File(PLAYER_DATA_FOLDER + id + ".json");
        try {
            if (!folder.exists() || !file.exists()) {
                if (!folder.exists())
                    if (!folder.mkdir()) {
                        log(Level.SEVERE, "Player data folder doesn't exist and couldn't be created at " + file.getAbsolutePath() + "\n" +
                                "Player data won't be saved, it is advised to restart the plugin and check logs for any reading errors or" +
                                "insufficient permission levels for this plugin");
                    } else {
                        file.createNewFile();
                    }
            } else {
                JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
                try {
                    if (json.has("spells")) {
                        JSONArray ar = json.getJSONArray("spells");
                        Element el = getElement(json.getString("element"));
                        WeavePlayer w = new WeavePlayer(
                                Bukkit.getPlayer(id),
                                el,
                                json.getInt("can_size"),
                                json.getInt("sor_size"),
                                json.getFloat("max_mana"));
                        for (int i = 0; i < ar.length(); i++) {
                            JSONObject j = ar.getJSONObject(i);
                            try {
                                Spell s;
                                Type st = Type.getType(j.getString("type"));
                                if (st == null) {
                                    log("Unknown spell type \"" + Type.getType(j.getString("type") + "\". Omitting"));
                                    continue;
                                }
//                                switch (st) {
//                                    case ATK -> s = new ATK(
//                                            UUID.fromString(j.getString("id")),
//                                            j.getString("name"),
//                                            Weight.getSpellType(j.getString("weight")),
//                                            j.getInt("level"),
//                                            j.getInt("experience"),
//                                            j.getInt("cast_cost"),
//                                            getElement(j.getString("main_element")),
//                                            getElement(j.getString("secondary_element")),
//                                            AreaEffect.getAreaEffect(j.getString("area_effect")),
//                                            j.getDouble("speed"),
//                                            j.getDouble("travel"),
//                                            j.getDouble("base_damage"),
//                                            j.getBoolean("phase"));
//                                    case DEF -> s = new DEF(
//                                            UUID.fromString(j.getString("id")),
//                                            j.getString("name"),
//                                            Weight.getSpellType(j.getString("weight")),
//                                            j.getInt("level"),
//                                            j.getInt("experience"),
//                                            j.getInt("cast_cost"),
//                                            getElement(j.getString("element")),
//                                            AreaEffect.getAreaEffect(j.getString("area_effect")),
//                                            j.getInt("hold_time"),
//                                            j.getInt("durability"));
//                                    case STI -> s = new STI(
//                                            STI.Stat.get(j.getString("stat")),
//                                            UUID.fromString(j.getString("id")),
//                                            j.getInt("level"),
//                                            j.getInt("experience"),
//                                            j.getInt("cast_cost"),
//                                            j.getInt("effect_time"),
//                                            j.getInt("multiplier"));
//                                    default -> s = new UTL(
//                                            UTL.Util.get(j.getString("util")),
//                                            j.getInt("level"),
//                                            j.getInt("experience"),
//                                            j.getInt("cast_cost"));
//                                }
//                                w.addSpell(s);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Spell main = json.getString("main").equals("null") ? null : Spell.getSpell(UUID.fromString(json.getString("main")));
                        log(main);
                        Spell sec = json.getString("second").equals("null") ? null : Spell.getSpell(UUID.fromString(json.getString("second")));
                        if (main != null) w.setMain(main);
                        if (sec != null) w.setSecondary(sec);
                        Particle particle;
                        switch (el) {
                            case FIRE -> particle = Particle.FLAME;
                            case AIR -> particle = Particle.CLOUD;
                            case WATER -> particle = Particle.DRIPPING_WATER;
                            case EARTH -> particle = Particle.ANGRY_VILLAGER;
                            case null, default -> particle = null;
                        }
                        if (particle != null) {
                            Particle finalParticle = particle;
                            new BukkitRunnable() {
                                public final Particle part = finalParticle;

                                @Override
                                public void run() {
                                    Player p = Bukkit.getPlayer(id);
                                    if (p != null)
                                        p.getWorld().spawnParticle(part, p.getLocation().clone().add(0, 1, 0), 1, 0.2, 0.4, 0.2, 0.01);
                                }
                            }.runTaskTimer(plugin, 80, 80);
                        }
                    }
                } catch (JSONException ignore) {
                    log(Level.WARNING, "An error occurred while loading " + Bukkit.getPlayer(id).getName() + "'s player file located at " + file.toPath() + ". Omitting.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean checkFilesAndCreate() {
        PLAYER_DATA_FOLDER = DTFL + "playerdata/";
        File pdf = new File(PLAYER_DATA_FOLDER);
        if (!pdf.exists())
            if (!pdf.mkdir()) {
                log(Level.SEVERE, "Couldn't create player data folder at: "  + PLAYER_DATA_FOLDER);
                return false;
            }
        BLOCK_DATAFILE = DTFL + "blocks.json";
        List<String> files = new ArrayList<>(Arrays.asList(BLOCK_DATAFILE));
        for (String file : files) {
            Path path = Path.of(file);
            if (!Files.exists(path)) {
                try {
                    Files.createFile(path);
                    log(Level.WARNING, "Created new file at '" + path +"' since it was absent");
                } catch (IOException e) {
                    log(Level.SEVERE, "Could not create file at '" + path + "'\nLog:\n" + String.join(Arrays.asList(e.getStackTrace()).toString()) + "\n");
                    return false;
                }
            }
        }
        return true;
    }
}
