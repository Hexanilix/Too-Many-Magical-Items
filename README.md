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
After downloading the latest release, simply put the .jar file in you **plugins** folder ***(this requiers a paper,
spigot or forge server)***, and the plugin will automatically sets everything up and is usable straight
out of the box.

### Optional Setup
Most options are tuned to fit most cases and generally, so it's not advised to change them if you don't have to,
but here are options that are woth changing to fit your needs:
- enabled
- autosaveFrequency

> For a more indepth analysis and options, check out [modifications](#modifications).

## Modifications
### Properties Config
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```config.yml```. Inside you'll find variables you can change. **However, you musn't change the properties listed above the comment regarding them, since they hold information about the file and plugin.**

### Table of changeable properties
| Property | Possible Values | Description |
|---------|:-----------------:|-------------|
|enabeled|true, false| *If the plugin should be enabled* |
|autosave|true, false| *Wether to automaticlly save data collected over time by the plugin.* **Note that this only saves data collected by the plugin, not server data** |
|autosaveMessage|true, false|  *Wether to log when a automatic save is performed to console* |
|autosaveFrequency| int 10-2147483647 | *Time between autosaves measured in seconds.* Defaults to 10 if value is under 10 |
|spellCollisions|true, false|*Whether spells should collide or not*|
> More and more parts of the plugin are becoming able to be toggled and modified, so this list isn't yet complete


### Default template for ```config.yml``` file:
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
> This file may be subsequent to change in the plugin itself before its updated here. This can be seen via the
> ```file_version``` property. You may hheck the release note attached to the release you downloaded, or run
> ```/tmmi plugin fileversion``` to find the plugins current fileversion. This should not happen but please be ware
