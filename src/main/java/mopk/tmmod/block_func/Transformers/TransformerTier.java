package mopk.tmmod.block_func.Transformers;

public enum TransformerTier {
    LV("lv_transformer", 1, 2),
    MV("mv_transformer", 2, 3),
    HV("hv_transformer", 3, 4),
    EV("ev_transformer", 4, 5);

    private final String name;
    private final int lowTier;
    private final int highTier;

    TransformerTier(String name, int lowTier, int highTier) {
        this.name = name;
        this.lowTier = lowTier;
        this.highTier = highTier;
    }

    public String getName() { return name; }
    public int getLowTier() { return lowTier; }
    public int getHighTier() { return highTier; }
    
    public int getLowTransfer() {
        return switch(lowTier) {
            case 1 -> 32;
            case 2 -> 128;
            case 3 -> 512;
            case 4 -> 2048;
            default -> 32;
        };
    }

    public int getHighTransfer() {
        return switch(highTier) {
            case 2 -> 128;
            case 3 -> 512;
            case 4 -> 2048;
            case 5 -> 8192;
            default -> 128;
        };
    }
}
