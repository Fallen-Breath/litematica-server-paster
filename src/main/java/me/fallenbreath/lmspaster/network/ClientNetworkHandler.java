package me.fallenbreath.lmspaster.network;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.PacketByteBuf;

import java.nio.charset.StandardCharsets;

public class ClientNetworkHandler
{
	private static boolean serverModded = false;

	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	public static void handleServerPacket(PacketByteBuf data, ClientPlayerEntity player)
	{
		int id = data.readVarInt();
		switch (id)
		{
			case Network.S2C.HI:
				String serverModVersion = data.readString();
				LitematicaServerPasterMod.LOGGER.info("Server is installed with mod {} @ {}", LitematicaServerPasterMod.MOD_NAME, serverModVersion);
				serverModded = true;
				break;
		}
	}

	public static void onGameJoin(ClientPlayNetworkHandler clientPlayNetworkHandler)
	{
		serverModded = false;
		clientPlayNetworkHandler.sendPacket(Network.C2S.packet(buf -> buf.
				writeVarInt(Network.C2S.HI).
				writeString(LitematicaServerPasterMod.VERSION)
		));
	}

	public static boolean doesServerAcceptsLongChat()
	{
		return serverModded;
	}

	public static boolean canSendCommand(String command)
	{
		return !command.isEmpty() && command.getBytes(StandardCharsets.UTF_8).length <= Short.MAX_VALUE;
	}

	public static void sendCommand(String command)
	{
		ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if (networkHandler != null)
		{
			networkHandler.sendPacket(Network.C2S.packet(buf -> buf.
					writeVarInt(Network.C2S.CHAT).
					writeString(command)
			));
		}
	}
}
