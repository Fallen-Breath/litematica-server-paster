## Litematica Server Paster

Let Litematica be able to paste tile entity data of block / entity data in a server

By using a custom chat packet to bypass the chat length limit so the client and simply append the tile entity nbt tag to the `/setblock` command

You need to install it on both client & server to work.

For the client-sode, it requires litematica mod only. For the server-side, it requires nothing

Tile entity data won't be pasted again if the block state matches the schematic though

