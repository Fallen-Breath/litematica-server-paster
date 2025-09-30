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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerUtils
{
	@NotNull
	public static MinecraftServer getServerFromPlayer(ServerPlayerEntity player)
	{
		//#if MC >= 1.21.9
		//$$ return player.getEntityWorld().getServer();
		//#else
		return player.getServerWorld().getServer();
		//#endif
	}
}
