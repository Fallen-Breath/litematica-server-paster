/*
 * This file is part of the Carpet TIS Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * Carpet TIS Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Carpet TIS Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Carpet TIS Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.lmspaster.mixins.network;

import me.fallenbreath.lmspaster.network.LmsPasterPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class CustomPayloadC2SPacketMixin
{
	@ModifyArg(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"
			)
	)
	private static List<?> registerTISCMPayload_c2s(List<CustomPayload.Type<?, ?>> types)
	{
		types = new ArrayList<>(types);
		types.add(new CustomPayload.Type<>(LmsPasterPayload.KEY, LmsPasterPayload.CODEC));
		return Collections.unmodifiableList(types);
	}
}
