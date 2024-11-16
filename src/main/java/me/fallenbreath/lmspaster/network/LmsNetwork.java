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

package me.fallenbreath.lmspaster.network;

import com.google.common.collect.Sets;
import me.fallenbreath.fanetlib.api.event.FanetlibClientEvents;
import me.fallenbreath.fanetlib.api.packet.FanetlibPackets;
import me.fallenbreath.fanetlib.api.packet.PacketCodec;
import me.fallenbreath.fanetlib.api.packet.PacketId;
import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Consumer;

public class LmsNetwork
{
	private static final PacketId<LmsPasterPacket> PACKET_ID = PacketId.of(LitematicaServerPasterMod.MOD_ID, "network_v2");

	public static class C2S
	{
		public static final int HI = 0;
		public static final int CHAT = 1;
		public static final int VERY_LONG_CHAT_START = 2;
		public static final int VERY_LONG_CHAT_CONTENT = 3;
		public static final int VERY_LONG_CHAT_END = 4;

		public static final int[] ALL_PACKET_IDS;

		static
		{
			Set<Integer> allPacketIds = Sets.newLinkedHashSet();
			for (Field field : C2S.class.getFields())
			{
				if (field.getType() == int.class && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()))
				{
					try
					{
						int id = (int) field.get(null);
						boolean notExists = allPacketIds.add(id);
						if (!notExists)
						{
							LitematicaServerPasterMod.LOGGER.error("Duplicated packet id {} ({})", id, field.getName());
						}
					}
					catch (Exception e)
					{
						LitematicaServerPasterMod.LOGGER.error("Failed to access field {}: {}", field, e);
					}
				}
			}
			ALL_PACKET_IDS = new int[allPacketIds.size()];
			int i = 0;
			for (Integer id : allPacketIds)
			{
				ALL_PACKET_IDS[i++] = id;
			}
		}

		public static CustomPayloadC2SPacket packet(int packetId, Consumer<CompoundTag> payloadBuilder)
		{
			CompoundTag nbt = new CompoundTag();
			payloadBuilder.accept(nbt);
			return FanetlibPackets.createC2S(PACKET_ID, new LmsPasterPacket(packetId, nbt));
		}
	}

	public static class S2C
	{
		public static final int HI = 0;
		public static final int ACCEPT_PACKETS = 1;

		public static CustomPayloadS2CPacket packet(int packetId, Consumer<CompoundTag> payloadBuilder)
		{
			CompoundTag nbt = new CompoundTag();
			payloadBuilder.accept(nbt);
			return FanetlibPackets.createS2C(PACKET_ID, new LmsPasterPacket(packetId, nbt));
		}
	}

	public static void initPackets()
	{
		FanetlibPackets.registerDual(
				PACKET_ID,
				PacketCodec.of(LmsPasterPacket::write, LmsPasterPacket::new),
				(p, c) -> ServerNetworkHandler.handleClientPacket(p, c.getPlayer()),
				(p, c) -> ClientNetworkHandler.handleServerPacket(p, c.getPlayer())
		);
	}

	public static void initEvents()
	{
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
		{
			initClientEvents();
		}
	}

	private static void initClientEvents()
	{
		FanetlibClientEvents.registerGameJoinListener((client, networkHandler) -> ClientNetworkHandler.sendHiToTheServer(networkHandler));
		FanetlibClientEvents.registerPlayerRespawnListener((client, networkHandler) -> ClientNetworkHandler.sendHiToTheServer(networkHandler));
	}
}
