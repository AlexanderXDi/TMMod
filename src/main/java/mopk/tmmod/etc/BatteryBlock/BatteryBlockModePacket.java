package mopk.tmmod.etc.BatteryBlock;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record BatteryBlockModePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<BatteryBlockModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("tmmod", "battery_block_mode"));
    public static final StreamCodec<ByteBuf, BatteryBlockModePacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, BatteryBlockModePacket::pos, BatteryBlockModePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

