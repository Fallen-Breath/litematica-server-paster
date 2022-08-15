package me.fallenbreath.lmspaster.network;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientNetworkHandler
{
	private static final int[] MINIMUM_SUPPORT_PACKETS = new int[]{Network.C2S.HI, Network.C2S.CHAT};
	private static int[] supportPackets = new int[0];

	public static void handleServerPacket(PacketByteBuf data, ClientPlayerEntity player)
	{
		int id = data.readVarInt();
		switch (id)
		{
			case Network.S2C.HI:
				String serverModVersion = data.readString();
				LitematicaServerPasterMod.LOGGER.info("Server is installed with mod {} @ {}", LitematicaServerPasterMod.MOD_NAME, serverModVersion);
				supportPackets = MINIMUM_SUPPORT_PACKETS.clone();
				break;

			case Network.S2C.ACCEPT_PACKETS:
				supportPackets = data.readIntArray();
				LitematicaServerPasterMod.LOGGER.debug("Packet IDs supported by the server: {}", supportPackets);
				break;
		}
	}

	public static void sendHiToTheServer(ClientPlayNetworkHandler clientPlayNetworkHandler)
	{
		supportPackets = new int[0];
		clientPlayNetworkHandler.sendPacket(Network.C2S.packet(buf -> buf.
				writeVarInt(Network.C2S.HI).
				writeString(LitematicaServerPasterMod.VERSION)
		));
	}

	public static boolean doesServerAcceptsLongChat()
	{
		return supportPackets.length > 0;
	}

	public static boolean doesServerAcceptsVeryLongChat()
	{
		return Arrays.stream(supportPackets).anyMatch(id -> id == Network.C2S.VERY_LONG_CHAT_START);
	}

	private static boolean isStringVeryLong(String string)
	{
		return string.getBytes(StandardCharsets.UTF_8).length > Short.MAX_VALUE;
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
			networkHandler.sendPacket(Network.C2S.packet(buf -> buf.
					writeVarInt(Network.C2S.CHAT).
					writeString(command)
			));
		}
	}

	private static void sendVeryLongCommand(String command)
	{
		ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if (networkHandler != null)
		{
			final int segmentLength = 8000;  // ~ Short.MAX_VALUE / 4, where utf8 allows 4 bytes per char at most

			networkHandler.sendPacket(Network.C2S.packet(buf -> buf.writeVarInt(Network.C2S.VERY_LONG_CHAT_START)));

			for (int i = 0; i < command.length(); i+= segmentLength)
			{
				int j = Math.min(command.length(), i + segmentLength);
				String segment = command.substring(i, j);
				networkHandler.sendPacket(Network.C2S.packet(buf -> buf.
						writeVarInt(Network.C2S.VERY_LONG_CHAT_CONTENT).
						writeString(segment)
				));
			}

			networkHandler.sendPacket(Network.C2S.packet(buf -> buf.writeVarInt(Network.C2S.VERY_LONG_CHAT_END)));
		}
	}
}
