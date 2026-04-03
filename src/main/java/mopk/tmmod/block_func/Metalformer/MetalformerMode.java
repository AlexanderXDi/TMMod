package mopk.tmmod.block_func.Metalformer;

public enum MetalformerMode {
    FORGING, CUTTING, SQUEEZING;

    public MetalformerMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}

