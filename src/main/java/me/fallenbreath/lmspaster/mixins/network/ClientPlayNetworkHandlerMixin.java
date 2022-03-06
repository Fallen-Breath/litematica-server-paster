package me.fallenbreath.lmspaster.mixins.network;

import me.fallenbreath.lmspaster.network.ClientNetworkHandler;
import me.fallenbreath.lmspaster.network.Network;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin
{
	@Inject(
			method = "onCustomPayload",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/s2c/play/CustomPayloadS2CPacket;getChannel()Lnet/minecraft/util/Identifier;",
					ordinal = 0
			),
			cancellable = true
	)
	private void onCustomPayload$lmspaster(CustomPayloadS2CPacket packet, CallbackInfo ci)
	{
		Identifier channel = ((CustomPayloadS2CPacketAccessor)packet).getChannel();
		if (Network.CHANNEL.equals(channel))
		{
			ClientNetworkHandler.handleServerPacket(((CustomPayloadS2CPacketAccessor)packet).getData(), MinecraftClient.getInstance().player);
			ci.cancel();
		}
	}

	@Inject(method = "onGameJoin", at = @At("RETURN"))
	private void playerJoinClientHook$lmspaster(GameJoinS2CPacket packet, CallbackInfo ci)
	{
		ClientNetworkHandler.onGameJoin((ClientPlayNetworkHandler)(Object)this);
	}
}
