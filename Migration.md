# Migration of plugin data between server instances
Similarly to how you can move minecraft worlds between games, same goes for TMMI's files, *mostly*. While this plugin
offers the option to move player data files (or the whole folder both cases work) between server instances with the
plugin, that functionality **doesn't work for Blocks**. It's physical block I'm refering to and not the item, and
the reason is simply due to the fact that 
## Moving items
As stated before, it's possible to move TMMI items from one server instance to another. However, this doesn't come
as simple as just copying files.
**Here is a step-by-step process to migrating items:**
- ### Step one - Getting files ready:
  To begin this operation of item migration, you'll first have to get command on your server `/tmmi migrate`,
  or if you want just migrate one players items `/tmmi migrate [player]`. After executing this command via
  in game or console, you're going to find a new folder in the plugins folder called `migration`.