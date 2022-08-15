package me.fallenbreath.lmspaster.mixins.network;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

//#if MC < 11900
import org.spongepowered.asm.mixin.gen.Invoker;
//#endif

@Mixin(ServerPlayNetworkHandler.class)
public interface ServerPlayNetworkHandlerAccessor
{
	// not used in 1.19+
	//#if MC < 11900
	@Invoker
	void invokeExecuteCommand(String command);
	//#endif
}
