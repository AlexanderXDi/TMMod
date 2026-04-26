package mopk.tmmod.block_func.Canner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CannerModePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<CannerModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("tmmod", "canner_mode"));

    public static final StreamCodec<FriendlyByteBuf, CannerModePacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeBlockPos(packet.pos()),
            buf -> new CannerModePacket(buf.readBlockPos())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
