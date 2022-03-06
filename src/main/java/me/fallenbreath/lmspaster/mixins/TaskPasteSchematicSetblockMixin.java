package me.fallenbreath.lmspaster.mixins;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicSetblock;
import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import me.fallenbreath.lmspaster.network.ClientNetworkHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.arguments.BlockArgumentParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TaskPasteSchematicSetblock.class)
public abstract class TaskPasteSchematicSetblockMixin
{
	private Chunk currentSchematicChunk;

	@ModifyVariable(
			method = "processBox",
			at = @At(
					value = "STORE",
					target = "Lfi/dy/masa/litematica/world/ChunkProviderSchematic;getChunk(II)Lfi/dy/masa/litematica/world/ChunkSchematic;",
					remap = false
			),
			remap = false,
			ordinal = 0
	)
	private Chunk recordCurrentSchematicChunk(Chunk chunkSchematic)
	{
		this.currentSchematicChunk = chunkSchematic;
		return chunkSchematic;
	}

	@Redirect(
			method = "sendSetBlockCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendChatMessage(Ljava/lang/String;)V",
					remap = true
			),
			remap = false
	)
	private void useCustomLongChatPacketToPasteNbtDirectly(ClientPlayerEntity player, String string, /* function args -> */ int x, int y, int z, BlockState state, ClientPlayerEntity player_)
	{
		if (ClientNetworkHandler.doesServerAcceptsLongChat())
		{
			BlockEntity blockEntity = this.currentSchematicChunk.getBlockEntity(new BlockPos(x, y, z));
			if (blockEntity != null)
			{
				String cmdName = Configs.Generic.PASTE_COMMAND_SETBLOCK.getStringValue();
				String stateString = BlockArgumentParser.stringifyBlockState(state);
				CompoundTag tag = blockEntity.toTag(new CompoundTag());
				tag.remove("id");
				tag.remove("x");
				tag.remove("y");
				tag.remove("z");
				String tagString = tag.toString();
				String command = String.format("/%s %s %s %s %s%s", cmdName, x, y, z, stateString, tagString);
				if (ClientNetworkHandler.canSendCommand(command))
				{
					LitematicaServerPasterMod.LOGGER.info("Pasting block {} at [{}, {}, {}] with nbt tag", state.getBlock().getName().getString(), x, y, z);
					ClientNetworkHandler.sendCommand(command);
					return;
				}
			}
		}
		// original behavior
		player.sendChatMessage(string);
	}
}
