package me.fallenbreath.lmspaster.network;

import io.netty.buffer.Unpooled;
import me.fallenbreath.lmspaster.util.RegistryUtil;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.function.Consumer;

public class Network
{
	public static final Identifier CHANNEL = RegistryUtil.id("network");

	public static class C2S
	{
		public static final int HI = 0;
		public static final int CHAT = 1;

		public static CustomPayloadC2SPacket packet(Consumer<PacketByteBuf> byteBufBuilder)
		{
			PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
			byteBufBuilder.accept(packetByteBuf);
			return new CustomPayloadC2SPacket(CHANNEL, packetByteBuf);
		}
	}

	public static class S2C
	{
		public static final int HI = 0;

		public static CustomPayloadS2CPacket packet(Consumer<PacketByteBuf> byteBufBuilder)
		{
			PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
			byteBufBuilder.accept(packetByteBuf);
			return new CustomPayloadS2CPacket(CHANNEL, packetByteBuf);
		}
	}
}
