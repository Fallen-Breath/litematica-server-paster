package me.fallenbreath.lmspaster.mixins;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkBase;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkCommand;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.util.PasteNbtBehavior;
import fi.dy.masa.malilib.util.LayerRange;
import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import me.fallenbreath.lmspaster.network.ClientNetworkHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(TaskPasteSchematicPerChunkCommand.class)
public abstract class TaskPasteSchematicSetblockMixin extends TaskPasteSchematicPerChunkBase
{
	@Shadow(remap = false) @Final protected String setBlockCommand;

	private Chunk currentSchematicChunk;

	public TaskPasteSchematicSetblockMixin(Collection<SchematicPlacement> placements, LayerRange range, boolean changedBlocksOnly)
	{
		super(placements, range, changedBlocksOnly);
	}

	private static final String CUSTOM_COMMAND_PREFIX = String.format("##%s##", LitematicaServerPasterMod.MOD_ID);

	@Override
	protected void sendCommandToServer(String command, ClientPlayerEntity player)
	{
		if (command.startsWith(CUSTOM_COMMAND_PREFIX))
		{
			command = command.substring(CUSTOM_COMMAND_PREFIX.length());
			if (command.charAt(0) != '/')
			{
				command = '/' + command;
			}
			ClientNetworkHandler.sendCommand(command);
		}
		else
		{
			// origin behavior
			super.sendCommandToServer(command, player);
		}
	}

	@Nullable
	private String customCommand = null;

	@Inject(method = "pasteBlock", at = @At("HEAD"), remap = false)
	private void recordCurrentSchematicChunk(BlockPos pos, WorldChunk schematicChunk, Chunk clientChunk, CallbackInfo ci)
	{
		this.currentSchematicChunk = schematicChunk;
	}

	@Inject(
			method = "queueSetBlockCommand",
			slice = @Slice(
					from = @At(
							value = "FIELD",
							target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskPasteSchematicPerChunkCommand;setBlockCommand:Ljava/lang/String;",
							remap = false
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Queue;offer(Ljava/lang/Object;)Z",
					remap = false,
					ordinal = 0
			),
			remap = false
	)
	private void useCustomLongChatPacketToPasteBlockNbtDirectly(int x, int y, int z, BlockState state, CallbackInfo ci)
	{
		// only works when PasteNbtBehavior equals NONE
		if (Configs.Generic.PASTE_NBT_BEHAVIOR.getOptionListValue() != PasteNbtBehavior.NONE)
		{
			return;
		}
		if (ClientNetworkHandler.doesServerAcceptsLongChat())
		{
			BlockEntity blockEntity = this.currentSchematicChunk.getBlockEntity(new BlockPos(x, y, z));
			if (blockEntity != null)
			{
				String cmdName = this.setBlockCommand;
				String stateString = BlockArgumentParser.stringifyBlockState(state);
				NbtCompound tag = blockEntity.createNbt();
				tag.remove("id");
				tag.remove("x");
				tag.remove("y");
				tag.remove("z");
				String tagString = tag.toString();
				String command = String.format("/%s %s %s %s %s%s", cmdName, x, y, z, stateString, tagString);
				if (ClientNetworkHandler.canSendCommand(command))
				{
					LitematicaServerPasterMod.LOGGER.info("Pasting block {} at [{}, {}, {}] with nbt tag", state.getBlock().getName().getString(), x, y, z);
					this.customCommand = command;
				}
			}
		}
	}

	@ModifyArg(
			method = "queueSetBlockCommand",
			slice = @Slice(
					from = @At(
							value = "FIELD",
							target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskPasteSchematicPerChunkCommand;setBlockCommand:Ljava/lang/String;",
							remap = false
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Queue;offer(Ljava/lang/Object;)Z",
					ordinal = 0,
					remap = false
			),
			remap = false
	)
	private Object useCustomLongChatPacketToPasteBlockNbtDirectly_setBlock(Object obj)
	{
		return this.useCustomLongChatIfPossible(obj);
	}

	@ModifyArg(
			method = "summonEntity",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Queue;offer(Ljava/lang/Object;)Z",
					ordinal = 0,
					remap = false
			),
			remap = false
	)
	private Object useCustomLongChatPacketToPasteEntityNbtDirectly(Object obj)
	{
		return this.useCustomLongChatIfPossible(obj);
	}

	private Object useCustomLongChatIfPossible(Object obj)
	{
		if (this.customCommand != null && obj instanceof String)
		{
			obj = CUSTOM_COMMAND_PREFIX + this.customCommand;
			this.customCommand = null;
		}
		return obj;
	}

	private Entity currentEntity;

	@Inject(method = "summonEntity", at = @At("HEAD"), remap = false)
	private void recordCurrentEntity(Entity entity, CallbackInfo ci)
	{
		this.currentEntity = entity;
	}

	@ModifyVariable(
			method = "summonEntity",
			at = @At(
					value = "STORE",
					target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
					ordinal = 0,
					remap = false
			),
			ordinal = 1,
			remap = false
	)
	private String useCustomLongChatPacketToPasteEntityNbtDirectly(String baseCommand)
	{
		if (ClientNetworkHandler.doesServerAcceptsLongChat())
		{
			if (this.currentEntity != null)
			{
				NbtCompound tag = this.currentEntity.writeNbt(new NbtCompound());

				// like net.minecraft.client.Keyboard.copyEntity
				tag.remove("UUID");
				tag.remove("Pos");
				tag.remove("Dimension");

				String tagString = tag.toString();
				String command = baseCommand + " " + tagString;
				if (ClientNetworkHandler.canSendCommand(command))
				{
					LitematicaServerPasterMod.LOGGER.info("Summoning entity {} with nbt tag", this.currentEntity.getType().getName().getString());
					this.customCommand = command;
				}
			}
		}
		return baseCommand;
	}
}
