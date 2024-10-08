# ❗THIS PLUGIN DOES NOT HAVE ANY RELEASES YET❗
# TMMI: A fun Minecraft Magic Plugin
**Too Many Magical Items**, or *TMMI* for short, is a Magic Minecraft plugin made by [Hexanilix](https://github.com/Hexanilix) as a
result of some fun ideas coming from friends and inspiration from popular games and TV shows 
This is a free, open source hobby project, available for the minecraft commiunity to enjoy and develop, for a memorable experience together.<br>
<img src="https://preview.redd.it/90-of-the-feedback-i-get-from-playtesting-is-like-v0-stono8yvbjbd1.png?auto=webp&s=b82f6fda0c0b2b1a3c09349acd5fd9f942fb5333">
## Contents:
- [Getting started](#getting-started)
- [Modifications](#modifications)

# Setup
## Latest releases:
<ul>
   <li>1.20.6 - <a href="https://github.com/Hexanilix/Too-Many-Magical-Items/releases/tag/Pre-alpha">blahblahbal</a></li>
</ul>

After downloading the latest release, simply put the `.jar` file in your **plugins** folder
***(this requires a server with built in [CraftBukkit](https://dev.bukkit.org/) support like [PaperMC](https://papermc.io/))***, and the plugin will automatically create and configure any nessesairy files. Thanks to this, there isn't any required setup besides downloading the plugin. The plugin automatically updates itself *(if set to do so)*, but to not miss out on any interesting new additions, we recomend visiting this page once in a while.
*You will be prompted to each update to visit this page. To disable this, click on "disable this message" or set the variable `SITE_PROMPT` in the [config.yml](#) file

### Optional Setup
Most options are tuned to suit most use cases, so it's not advised to change them if you don't have to,
but here are options that you should consider changing to fit your specific needs:
- enabled
- autosaveFrequency
- automaticUpdates

> For a more indepth analysis and options, check out [modifications](#modifications).

# Configuration
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```config.yml```. Inside you'll find variables you can change. **However, you musn't change the properties listed above the comment regarding them, since they hold information about the file and plugin.**

### Table of changeable properties
| Property                  | Possible Values | Description                                                                           |
|:--------------------------|:---------------:|:--------------------------------------------------------------------------------------|
| enabled                   |   true, false   | *Enable plugin*                                                                       |
| migration                 |   true, false   | *Allow **[migration](Migration.md)***                                                 |
| autosave   ¹              |   true, false   | *Automatically save data collected over time by the plugin.*                          |
| autosaveMessage           |   true, false   | *Log when a automatic save is performed to console*                                   |
| autosaveFrequency         |       int       | *Time between auto saves measured in seconds.* Defaults to 10 if value is under 10    |
| spellCollisions           |   true, false   | *Spells can collide with one another*                                                 |
| spell_seed_cap            |     double      | *The maximum speed at which a spell can travel measured in blocks*                    |
| spell_travel_distance_cap |     double      | *The maximum distance a spell can travel measured in block*                           |
| spell_damage_cap          |     double      | *The maximum amount of damage a spell can inflict on an entity*                       |
| enable_custom_spells      |   true, false   | *Allow **[custom spells](#custom-spells)***                                           |

> autosave¹ - Note that this function only saves data collected by the plugin, not server data

More and more aspects of the plugin are becoming available to be changed,
so this list is going to continue to expand, as well as the `config.yml` file.

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
### Here is the file tree on which the plugin operates:
- ### `TMMI`/
  - `config.yml` - The config file 
  - `playerdata`/
    - `[UUID].json` - Exclusive to each player
  - `spells`/
    -  fgh
## Versions
The way files save data and name it can be changed in new releases of the plugin. Some files like the config file
is updated on this page since it's not. as well as your plugin config file with new or renamed/changed options. However, if you copy the contents of another config file or have a backup,
the naming scheme may be incorrect or lack some options. The plugin has functionality to compensate for this however, it only works backwards
that it can read files backYou can check this by comparing the
`file_version` property in the file, with the one in the plugin. You can find it in the note attached to the release you downloaded, or run
`/tmmi plugin fileversion` in the plugin through console or in game. **This should not occur but please be aware**
# Migration of data
Similarly to how you can move minecraft worlds between games, TMMI allows similar functionality for *most* of its files. While this plugin
offers the option to move player data files (or the whole folder both cases work) between server instances with the
plugin, that functionality **doesn't work for Blocks**. It's physical block I'm refering to and not the item, and
the reason is rather obvious.
<br>
<br>
***However...*** 
## Moving items
As stated before, it's possible to move TMMI items from one server instance to another. However, this doesn't come
as simple as just copying files.
**Here is a step-by-step process to migrating items:**
- ### Step one - Getting files ready:
  To begin this operation of item migration, you'll first have to get command on your server `/tmmi migrate`,
  or if you want just migrate one players items `/tmmi migrate [player]`. After executing this command via
  in game or console, you're going to find a new folder in the plugins folder called `migration`.

