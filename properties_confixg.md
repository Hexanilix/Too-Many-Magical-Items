# Properties Config
By going into your plugin folder and locating a folder called **TMMI**, you should find a file named ```tmmi.properties```. Inside you'll find variables you can change. **However, some of them you musn't, the file states which ones**

## Table of changable properties
| Property | Possible Values | Description |
|:---------|:-----------------:|-------------|
|enabeled|true, false|If the plugin should be enabled|
|autosave|true, false| |
|autosaveMessage|true, false| |
  - 
  - Possible values: ``````
- ### 
  - Wether to automaticlly save data collected over time by the plugin. **Note that this only saves data collected by the plugin, not server data** 
  - Possible values: ```true, false```
- ### 
  - Wether to log to console when the plugin autosave occures. Runs only when **autosave** is enabled.
  - Possible values: ```true, false```
- ### autosaveFrequency
  - Time between autosaves measured in seconds
  - Possible values: ```10-2147483647, Defaults to 10 if value is under 10```
- ### spellCollisions
  - Whether spells can collide or not
  - Possible values: ```true, false```
