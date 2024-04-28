# ❗THIS PLUGIN IS IN ALPHA❗
# TMMI: A fun Minecraft Magic Plugin
Too Many Magical Items, or TMMI for short, is a Magic Minecraft plugin made by [Hexanilix](https://github.com/Hexanilix) as a result of some wants and lacks from simmilar plugins.
This is a free, open source hobby project open to the commiunity to use for themselves.
## Contents:
- [Getting started](#getting-started)
- [Modifications](#modifications)
## Getting started
### Initial Setup
After downloading the latest release, simply put the .jar file in you **plugins** folder ***(this requiers a paper, spigot or forge server)***, and the plugin will automatically sets everything up and is usable straight out of the box.

### Optional Setup
Most options are tuned to fit most cases and generaly, so it's not advised to change them if you don't have to, but here are options that are woth changing to fit your needs:
- enabeled
- autosaveFrequency

> For a more indepth analysys and options, check out [modifications](#modifications).

## Modifications
### Properties Config
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```tmmi.properties```. Inside you'll find variables you can change. **However, you musn't change the properties listed above the comment regarding them, since they hold information about the file and plugin.**

### Table of changable properties
| Property | Possible Values | Description |
|---------|:-----------------:|-------------|
|enabeled|true, false| *If the plugin should be enabled* |
|autosave|true, false| *Wether to automaticlly save data collected over time by the plugin.* **Note that this only saves data collected by the plugin, not server data** |
|autosaveMessage|true, false|  *Wether to log when a automatic save is performed to console* |
|autosaveFrequency| int 10-2147483647 | *Time between autosaves measured in seconds.* Defaults to 10 if value is under 10 |
|spellCollisions|true, false|*Whether spells should collide or not*|
> More and more parts of the plugin are becoming able to be toggled and modified, so this list isn't yet complete


### Default template for ```tmmi.properties``` file:
```
# Last automatic modification: 26/04/2024 19:46:34
fileversion=1.0.0
# Do not change the above values and names, this can cause issues and improper loading in the plugin
# The following values are to be modified:
enabeled=true
autosave=true
autosaveMessage=true
autosaveFrequency=18000 # 30 minutes
```
> This file may be subsequent to changes in the plugin itself before its updated here. This can be seen via the ```fileversion``` property. You may hheck the release note attached to the release you downloaded, or run ```/tmmi plugin fileversion``` to find the plugins current fileversion. This should not happen but please be ware
