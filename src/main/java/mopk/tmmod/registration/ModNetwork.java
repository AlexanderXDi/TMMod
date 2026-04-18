package mopk.tmmod.registration;

import mopk.tmmod.block_func.Metalformer.MetalformerBE;
import mopk.tmmod.block_func.Metalformer.MetalformerModePacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// синхронизирует кнопку с сервером
public class ModNetwork {
    public static void handleMetalformerMode(MetalformerModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(data.pos()) instanceof MetalformerBE be) {
                be.toggleMode();
            }
        });
    }
    public static void handleTransformerMode(mopk.tmmod.block_func.Transformers.TransformerModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getBlockEntity(data.pos()) instanceof mopk.tmmod.block_func.Transformers.TransformerBE be) {
                be.toggleMode();
            }
        });
    }
}
