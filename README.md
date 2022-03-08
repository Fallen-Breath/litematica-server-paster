# Litematica Server Paster

Let Litematica be able to paste tile entity data of block / entity data in a server

By using a custom chat packet to bypass the chat length limit so the client and simply append the tile entity or the entity nbt tag to the `/setblock` or the `/summon` command

You need to install it on both client & server to work.

For the client-sode, it requires litematica mod only. For the server-side, it requires nothing

For clientside, using latest litematica mod is always suggested

Tile entity data won't be pasted again if the block state matches the schematic though

