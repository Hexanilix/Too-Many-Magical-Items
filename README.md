# ❗THIS PLUGIN IS IN ALPHA❗
# TMMI: A fun Minecraft Magic Plugin
Too Many Magical Items, or TMMI for short, is a Magic Minecraft plugin made by [Hexanilix](https://github.com/Hexanilix) as a
result of some wants and lacks from similar plugins. 
This is a free, open source hobby project open to the community to use for themselves.
## Contents:
- [Getting started](#getting-started)
- [Modifications](#modifications)
## Getting started
### Initial Setup
After downloading the latest release, simply put the `.jar` file in your **plugins** folder
***(this requires a paper, spigot or forge server)***, and the plugin will automatically set everything up and is usable straight
out of the box.

### Optional Setup
Most options are tuned to suit most use cases, so it's not advised to change them if you don't have to,
but here are options that you should consider changing to fit your specific needs:
- enabled
- autosaveFrequency

> For a more indepth analysis and options, check out [modifications](#modifications).

## Modifications
### Properties Config
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```config.yml```. Inside you'll find variables you can change. **However, you musn't change the properties listed above the comment regarding them, since they hold information about the file and plugin.**

### Table of changeable properties
| Property                  | Possible Values | Description                                                                        |
|---------------------------|:---------------:|------------------------------------------------------------------------------------|
| enabled                   |   true, false   | *Enable plugin*                                                                    |
| migration                 |   true, false   | *Allow **[migration](#migration)***                                                |
| autosave*                 |   true, false   | *Automatically save data collected over time by the plugin.*                       |
| autosaveMessage           |   true, false   | *Log when a automatic save is performed to console*                                |
| autosaveFrequency         |       int       | *Time between auto saves measured in seconds.* Defaults to 10 if value is under 10 |
| spellCollisions           |   true, false   | *Spells can collide with one another*                                              |
| spell_seed_cap            |     double      | *The maximum speed at which a spell can travel measured in blocks*                 |
| spell_travel_distance_cap |     double      | *The maximum distance a spell can travel measured in block*                        |
| spell_damage_cap          |     double      | *The maximum amount of damage a spell can inflict on an entity*                    |
**autosave:** *Note that this only saves data collected by the plugin, not server data*
> More and more aspects of the plugin are becoming available to be changed,
> so this list is going to continue to expand, as well as the `config.yml` file.

### Default template for `config.yml` file:
```
# Last automatic modification: 29/04/2024 13:16:27
file_version: 1.0.1
# Do not change the above values, this can cause issues and improper loading in the plugin
# The following values are to be customised, change any value after the '=' char to your liking based
# of this list: 
enabled: true
autosave: true
autosave_frequency: 1800
autosave_message: true
message_value: Autosaving...
disabled_spells: 
   - 
spell_collision: true
```
# Saved Data
Simply, the plugin uses JSON as well as YAML files to store its data.
## Files

## Versions
The way files save data may become subsequent to change in new releases of the plugin. Some files like the config file
is updated on this page since it's not. as well as your plugin config file with new or renamed/changed options. However, if you copy the contents of another config file or have a backup,
the naming scheme may be incorrect or lack some options. The plugin has functionality to compensate for this however, it only works backwards
that it can read files backYou can check this by comparing the
`file_version` property in the file, with the one in the plugin. You can find it in the note attached to the release you downloaded, or run
`/tmmi plugin fileversion` in the plugin through console or in game. **This should not occur but please be aware**

