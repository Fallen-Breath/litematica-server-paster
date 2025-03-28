/*
 * This file is part of the Litematica Server Paster project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Fallen_Breath and contributors
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

package me.fallenbreath.lmspaster.utils;

import net.minecraft.nbt.CompoundTag;

public class NbtUtils
{
	// ============= Element Accessors =============

	public static String getStringOrEmpty(CompoundTag nbt, String key)
	{
		//#if MC >= 12105
		//$$ return nbt.getString(key, "");
		//#else
		return nbt.getString(key);
		//#endif
	}

	public static int[] getIntArrayOrEmpty(CompoundTag nbt, String key)
	{
		//#if MC >= 12105
		//$$ return nbt.getIntArray(key).orElseGet(() -> new int[]{});
		//#else
		return nbt.getIntArray(key);
		//#endif
	}
}
