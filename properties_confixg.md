# Properties Config
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```tmmi.properties```. Inside you'll find variables you can change. **However, you musn't change the properties listed above the comment regarding them, since they hold information on the plugin itself.**

## Table of changable properties
| Property | Possible Values | Description |
|:---------|:-----------------:|-------------|
|enabeled|true, false|If the plugin should be enabled|
|autosave|true, false| Wether to automaticlly save data collected over time by the plugin. **Note that this only saves data collected by the plugin, not server data** |
|autosaveMessage|true, false|  Wether to automaticlly save data collected over time by the plugin. **Note that this only saves data collected by the plugin, not server data** |
|autosaveFrequency| int 10-2147483647 | Time between autosaves measured in seconds. Defaults to 10 if value is under 10 |
|spellCollisions|true, false|Whether spells can collide or not|

More and more parts of the plugin are becoming able to be toggled and modified so this list isn't yet complete




### Default ```tmmi.properties``` file in case plugin don't create it or it wans't filled out properly:
```
# Last automatic modification: date
fileversion=1.0.0
# Do not change the above values and names, this can cause issues and improper loading in the plugin
# The following values are to be customised
enabeled=true
autosave=true
autosaveMessage=true
autosaveFrequency=18000 # 30 minutes

```
