package mopk.tmmod.block_func.Transformers;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TransformerModePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TransformerModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("tmmod", "transformer_mode"));
    public static final StreamCodec<ByteBuf, TransformerModePacket> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, TransformerModePacket::pos, TransformerModePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
