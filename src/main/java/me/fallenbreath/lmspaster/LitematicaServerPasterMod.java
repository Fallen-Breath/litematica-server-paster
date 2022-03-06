package me.fallenbreath.lmspaster;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LitematicaServerPasterMod implements ModInitializer
{
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String MOD_NAME = "Litematica Server Paster";
	public static final String MOD_ID = "litematica-server-paster";
	public static String VERSION = null;

	@Override
	public void onInitialize()
	{
		VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata().getVersion().getFriendlyString();

//		MixinEnvironment.getCurrentEnvironment().audit();
	}
}
