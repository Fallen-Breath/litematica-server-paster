# Litematica Server Paster

[![Modrinth](https://img.shields.io/modrinth/dt/HCbarMw6?label=Modrinth%20Downloads)](https://modrinth.com/mod/litematica-server-paster)

Let [Litematica](https://github.com/maruohon/litematica) be able to paste tile entity data of block / entity data in a server

By using a custom chat packet to bypass the chat length limit so the client and simply append the tile entity or the entity nbt tag to the `/setblock` or the `/summon` command

You need to install it on both client & server to work.

For the client-sode, it requires litematica mod only. For the server-side, it requires nothing

You need to set `commandUseWorldEdit` to false and `pasteNbtRestoreBehavior` to `None` in litematica for this mod to work, if these 2 options exist in your litematica mod

Tile entity data won't be pasted again if the block state matches the schematic though

