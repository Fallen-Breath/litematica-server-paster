package me.fallenbreath.lmspaster.mixins.network;

import me.fallenbreath.lmspaster.network.Network;
import me.fallenbreath.lmspaster.network.ServerNetworkHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin
{
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void onCustomPayload$lmspaster(CustomPayloadC2SPacket packet, CallbackInfo ci)
	{
		Identifier channel = ((CustomPayloadC2SPacketAccessor)packet).getChannel();
		if (Network.CHANNEL.equals(channel))
		{
			ServerNetworkHandler.handleClientPacket(((CustomPayloadC2SPacketAccessor)packet).getData(), this.player);
			ci.cancel();
		}
	}
}
