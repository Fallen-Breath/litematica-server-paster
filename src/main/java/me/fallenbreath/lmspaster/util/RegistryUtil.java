package me.fallenbreath.lmspaster.util;

import me.fallenbreath.lmspaster.LitematicaServerPasterMod;
import net.minecraft.util.Identifier;

public class RegistryUtil
{
	public static Identifier id(String name)
	{
		return new Identifier(LitematicaServerPasterMod.MOD_ID, name);
	}
}
