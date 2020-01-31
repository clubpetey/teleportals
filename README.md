# teleportals
Minecraft mod to turn anything into a portal to a dimension.

I needed a mod like this for a mod-pack I'm working on, and I thought it would be useful to others as well.  This mod will let you
set a certain criteria that will cause the player to be teleported to a position in the current dimension or another dimension.  
Pattern matching against the NBTTag or the Block state is also available so you can create dimensional "keys" like only a certain
set of items in a chest will trigger a teleport.

## Configuration and Format
Upon server start, the dimension configuration file will be loaded and processed, the default for this file is teleportals.dims but 
it can be changed via the mod config file.  The .dims file uses the following format:


    DIM X_1
    criteria_a value_1 value_2 value_3...
    criteria_b value_1 value_2...
    criteria_c value_1 [
    value_2
    value_3
    ]
    
    DIM X_2
    ...

The value for "DIM" can be either a numeric dimension or the asterisk (*) to indicate the current dimension. Also notice that values for
a given fields can be specified on one line, or multiple lines.  to use multi-line values, the line with the criteria must end with a 
bracked ([) and the closing bracket (]) must be on a line by itself.

## Criteria 
The following criteria are currently supported:
* trigger - This can be one of: 
click (The player must right-click on a given block or with a given item)
block (The player must walk across the given block)
pos (the player must be a the given position in the world)
* item - the player must have this item (specified by registry name) in the main hand when right-clicking
* block - the player must be right-clicking on this block (trigger = click), or the player must walk over this block (trigger = block)
* moveto - teleport the player to this x, y, z location.  use "0" for y to have the mod find a safe height for the player nearest sea level
* move - teleport the player x, y, z blocks from their current position, if only value is specified, the player will be teleported that may blocks in the direction they are looking.
* pos - the position the player must be at (trigger = pos) use "*" to indicate the x/y/z value should be ignored
* base - build a small platform out of this block (specified by registry name) at the destination of the player
* itemmatch - This is a regex (if value start with a slash "/") or a partial string match  against the item's name and/or NBTtag
* blockmatch - This is a regex (if value start with a slash "/") or a partial string match  against the block name and/or NBTtag that the payer is looking at
* match - will translate to "itemmatch" if an "item" is specified, otherwise translates to "blockmatch"
* from - list of dimension IDs that the player must be in

NOTE: if neither "move" or "moveto" is used, the player's destination will be the dimension's spawn point.

## Examples

Here's a few examples to illustrate usage:

    DIM -1
    trigger block
    block minecraft:glass
Walking over a glass block will teleport you to the nether's spawn point, a safe height will be found for the player.

    DIM *
    trigger click
    item minecraft:ender_pearl
    move 400 0 0
Right-clicking on an ender peral will move your 400 blocks in the X direction

    DIM 1
    trigger click
    block minecraft:chest
    moveto 100 100 100
    blocktag minecraft:flint_and_steel
    base minecraft:glass
Right-clicking with an empty hand on a chest with a flint and steel in any slot will teleport the player to x,y,z = 100,100,100 in the End 
and create a platform of glass below and above the player
    
