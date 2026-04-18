package mopk.tmmod.block_func.Accumulators;

public enum AccumulatorTier {
    BATBOX("batbox", 40000, 32, 1),
    CESU("cesu", 300000, 128, 2),
    MFE("mfe", 4000000, 512, 3),
    MFSU("mfsu", 40000000, 2048, 4),
    AESS("aess", 400000000, 8192, 5);

    private final String name;
    private final int capacity;
    private final int transfer;
    private final int tier;

    AccumulatorTier(String name, int capacity, int transfer, int tier) {
        this.name = name;
        this.capacity = capacity;
        this.transfer = transfer;
        this.tier = tier;
    }

    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public int getTransfer() { return transfer; }
    public int getTier() { return tier; }
}
