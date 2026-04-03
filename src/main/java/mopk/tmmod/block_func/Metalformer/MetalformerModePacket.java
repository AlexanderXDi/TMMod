package mopk.tmmod.block_func.Metalformer;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record MetalformerModePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<MetalformerModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("tmmod", "metalformer_mode"));
    public static final StreamCodec<ByteBuf, MetalformerModePacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, MetalformerModePacket::pos, MetalformerModePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

