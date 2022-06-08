package me.fallenbreath.lmspaster.mixins.network;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public interface ServerPlayNetworkHandlerAccessor
{
	// not used in 1.19+
//	@Invoker
//	void invokeExecuteCommand(String command);
}
