/*
 * This file is part of the Litematica Server Paster project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * Litematica Server Paster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Litematica Server Paster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Litematica Server Paster.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.lmspaster.mixins;

import com.google.common.base.Strings;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.scheduler.tasks.TaskPasteSchematicPerChunkCommand;
import fi.dy.masa.litematica.util.PasteNbtBehavior;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 11700
//$$ import net.minecraft.nbt.NbtHelper;
//#endif

@Mixin(TaskPasteSchematicPerChunkCommand.class)
public abstract class TaskPasteSchematicSetblockMixin
{
	@Shadow(remap = false)
	protected abstract void sendCommandToServer(String command, ClientPlayerEntity player);

	@Unique
	private Chunk currentSchematicChunk;

	//#if MC >= 11700
	//$$ @Inject(method = "pasteBlock", at = @At("HEAD"), remap = false)
	//$$ private void recordCurrentSchematicChunk(BlockPos pos, @Coerce Chunk schematicChunk, Chunk clientChunk, CallbackInfo ci)
	//#else
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
	private Chunk recordCurrentSchematicChunk(Chunk schematicChunk)
	//#endif
	{
		this.currentSchematicChunk = schematicChunk;
		//#if MC < 11700
		return schematicChunk;
		//#endif
	}

	@Unique
	@Nullable
	private String customCommand = null;

	@Redirect(
			method = "sendCommand",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskPasteSchematicPerChunkCommand;sendCommandToServer(Ljava/lang/String;Lnet/minecraft/client/network/ClientPlayerEntity;)V",
					remap = true
			),
			remap = false
	)
	private void modifyCommand(TaskPasteSchematicPerChunkCommand instance, String command, ClientPlayerEntity player)
	{
		// maybe be modified to null cuz we do that in useCustomLongChatPacketToPasteEntityNbtDirectly
		if (command == null)
		{
			return;
		}

		if (!Strings.isNullOrEmpty(this.customCommand))
		{
			if (this.customCommand.charAt(0) != '/')
			{
				this.customCommand = '/' + this.customCommand;
			}
			ClientNetworkHandler.sendCommand(this.customCommand);
			this.customCommand = null;
		}
		else
		{
			// origin behavior
			this.sendCommandToServer(command, player);
		}
	}

	@Inject(
			method = "sendSetBlockCommand",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskPasteSchematicPerChunkCommand;sendCommand(Ljava/lang/String;Lnet/minecraft/client/network/ClientPlayerEntity;)V",
					remap = true
			),
			remap = false
	)
	private void useCustomLongChatPacketToPasteBlockNbtDirectly(int x, int y, int z, BlockState state, ClientPlayerEntity player, CallbackInfo ci)
	{
		// only works when PasteNbtBehavior equals NONE
		if (Configs.Generic.PASTE_NBT_BEHAVIOR.getOptionListValue() != PasteNbtBehavior.NONE)
		{
			return;
		}
		if (ClientNetworkHandler.isServerPasterAvailable())
		{
			BlockEntity blockEntity = this.currentSchematicChunk.getBlockEntity(new BlockPos(x, y, z));
			if (blockEntity != null)
			{
				String cmdName = Configs.Generic.PASTE_COMMAND_SETBLOCK.getStringValue();
				String stateString = BlockArgumentParser.stringifyBlockState(state);
				NbtCompound tag = blockEntity.writeNbt(new NbtCompound());
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

	@Unique
	private Entity currentEntity;

	//#if MC >= 11700
	//$$ @Inject(method = "summonEntity", at = @At("HEAD"), remap = false)
	//$$ private void recordCurrentEntity(Entity entity, CallbackInfo ci)
	//#else
	@ModifyVariable(
			method = "summonEntities",
			at = @At(
					value = "STORE",
					target = "Ljava/util/Iterator;next()Ljava/lang/Object;",
					remap = false
			),
			remap = false,
			ordinal = 0
	)
	private Entity recordCurrentEntity(Entity entity)
	//#endif
	{
		this.currentEntity = entity;
		//#if MC < 11700
		return entity;
		//#endif
	}

	//#if MC >= 11700
	//$$ @ModifyVariable(
	//$$ 		method = "summonEntity",
	//$$ 		at = @At(
	//$$ 				value = "STORE",
	//$$ 				target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
	//$$ 				ordinal = 0,
	//$$ 				remap = false
	//$$ 		),
	//$$ 		ordinal = 1,
	//$$ 		remap = false
	//$$ )
	//#else
	@ModifyArg(
			method = "summonEntities",
			at = @At(
					value = "INVOKE",
					target = "Lfi/dy/masa/litematica/scheduler/tasks/TaskPasteSchematicPerChunkCommand;sendCommand(Ljava/lang/String;Lnet/minecraft/client/network/ClientPlayerEntity;)V",
					remap = true
			),
			remap = false
	)
	//#endif
	private String useCustomLongChatPacketToPasteEntityNbtDirectly(String baseCommand)
	{
		if (ClientNetworkHandler.isServerPasterAvailable())
		{
			if (this.currentEntity != null)
			{
				if (this.currentEntity.getVehicle() != null)
				{
					// acaciachan: don't paste passenger entities, they are already handled at the bottom-most entity
					return null;
				}

				NbtCompound tag = this.currentEntity.writeNbt(new NbtCompound());

				// like net.minecraft.client.Keyboard.copyEntity
				tag.remove("UUID");
				tag.remove("Pos");
				tag.remove("Dimension");

				//#if MC >= 11700
				//$$ String tagString = NbtHelper.toPrettyPrintedText(tag).getString();
				//#else
				String tagString = tag.toText().getString();
				//#endif
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