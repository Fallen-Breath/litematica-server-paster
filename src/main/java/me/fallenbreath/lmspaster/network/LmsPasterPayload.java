/*
 * This file is part of the Litematica Server Paster project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
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

package me.fallenbreath.lmspaster.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

//#if MC >= 12005
//$$ import net.minecraft.network.codec.PacketCodec;
//#endif

//#if MC >= 12002
//$$ import net.minecraft.network.packet.CustomPayload;
//#endif

public class LmsPasterPayload
		//#if MC >= 12002
		//$$ implements CustomPayload
		//#endif
{
	public static final Identifier ID = Network.CHANNEL;

	//#if MC >= 12005
	//$$ public static final CustomPayload.Id<LmsPasterPayload> KEY = new CustomPayload.Id<>(ID);
	//$$ public static final PacketCodec<PacketByteBuf, LmsPasterPayload> CODEC = CustomPayload.codecOf(LmsPasterPayload::write, LmsPasterPayload::new);
	//#endif

	private final int id;
	private final CompoundTag nbt;

	public LmsPasterPayload(int id, CompoundTag nbt)
	{
		this.id = id;
		this.nbt = nbt;
	}

	public LmsPasterPayload(PacketByteBuf buf)
	{
		this(buf.readVarInt(), buf.readCompoundTag());
	}

	//#if MC >= 12002
	//$$ //#if MC < 12005
	//$$ @Override
	//$$ //#endif
	//$$ public void write(PacketByteBuf buf)
	//$$ {
	//$$ 	buf.writeVarInt(this.id);
	//$$ 	buf.writeNbt(this.nbt);
	//$$ }
	//$$
	//$$ //#if MC < 12005
	//$$ @Override
	//$$ //#endif
	//$$ public Identifier id()
	//$$ {
	//$$ 	return ID;
	//$$ }
	//#endif

	//#if MC >= 12005
	//$$ @Override
	//$$ public Id<? extends LmsPasterPayload> getId()
	//$$ {
	//$$ 	return KEY;
	//$$ }
	//#endif

	public int getPacketId()
	{
		return this.id;
	}

	public CompoundTag getNbt()
	{
		return this.nbt;
	}
}
