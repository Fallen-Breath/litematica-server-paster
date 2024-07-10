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

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.CompoundTag;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientNetworkHandler
{
	private static final int[] MINIMUM_SUPPORT_PACKETS = new int[]{LmsNetwork.C2S.HI, LmsNetwork.C2S.CHAT};
	private static int[] supportPackets = new int[0];

	public static void handleServerPacket(LmsPasterPacket packet, ClientPlayerEntity player)
	{
		int id = packet.getPacketId();
		CompoundTag nbt = packet.getNbt();
		switch (id)
		{
			case LmsNetwork.S2C.HI:
				String serverModVersion = nbt.getString("mod_version");
				LitematicaServerPasterMod.LOGGER.info("Server is installed with mod {} @ {}", LitematicaServerPasterMod.MOD_NAME, serverModVersion);
				supportPackets = MINIMUM_SUPPORT_PACKETS.clone();
				break;

			case LmsNetwork.S2C.ACCEPT_PACKETS:
				supportPackets = nbt.getIntArray("ids");
				LitematicaServerPasterMod.LOGGER.debug("Packet IDs supported by the server: {}", supportPackets);
				break;
		}
	}

	public static void sendHiToTheServer(ClientPlayNetworkHandler clientPlayNetworkHandler)
	{
		supportPackets = new int[0];
		clientPlayNetworkHandler.sendPacket(LmsNetwork.C2S.packet(LmsNetwork.C2S.HI, nbt2 -> {
			nbt2.putString("mod_version", LitematicaServerPasterMod.VERSION);
		}));
	}

	public static boolean isServerPasterAvailable()
	{
		return supportPackets.length > 0;
	}

	public static boolean doesServerAcceptsVeryLongChat()
	{
		return Arrays.stream(supportPackets).anyMatch(id -> id == LmsNetwork.C2S.VERY_LONG_CHAT_START);
	}

	private static boolean isStringVeryLong(String string)
	{
		return string.getBytes(StandardCharsets.UTF_8).length > Short.MAX_VALUE - 100;  // -100 for safety
	}

	public static boolean canSendCommand(String command)
	{
		if (command.isEmpty())
		{
			return false;
		}
		// long chat support
		if (doesServerAcceptsVeryLongChat())
		{
			return true;
		}
		return !isStringVeryLong(command);
	}

	public static void sendCommand(String command)
	{
		if (isStringVeryLong(command))
		{
			if (doesServerAcceptsVeryLongChat())
			{
				sendVeryLongCommand(command);
			}
			else
			{
				LitematicaServerPasterMod.LOGGER.warn("Tried to send over-length command but server does not support very long chat. Command length {}", command.length());
			}
			return;
		}
		ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if (networkHandler != null)
		{
			networkHandler.sendPacket(LmsNetwork.C2S.packet(LmsNetwork.C2S.CHAT, nbt2 -> {
				nbt2.putString("chat", command);
			}));
		}
	}

	private static void sendVeryLongCommand(String command)
	{
		ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if (networkHandler != null)
		{
			final int segmentLength = 8000;  // ~ Short.MAX_VALUE / 4, where utf8 allows 4 bytes per char at most

			networkHandler.sendPacket(LmsNetwork.C2S.packet(LmsNetwork.C2S.VERY_LONG_CHAT_START, nbt2 -> {}));

			for (int i = 0; i < command.length(); i+= segmentLength)
			{
				int j = Math.min(command.length(), i + segmentLength);
				String segment = command.substring(i, j);
				networkHandler.sendPacket(LmsNetwork.C2S.packet(LmsNetwork.C2S.VERY_LONG_CHAT_CONTENT, nbt2 -> {
					nbt2.putString("segment", segment);
				}));
			}

			networkHandler.sendPacket(LmsNetwork.C2S.packet(LmsNetwork.C2S.VERY_LONG_CHAT_END, nbt2 -> {}));
		}
	}
}
