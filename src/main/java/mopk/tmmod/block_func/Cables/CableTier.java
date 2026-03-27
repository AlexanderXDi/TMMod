package mopk.tmmod.block_func.Cables;

public enum CableTier {
    tin(256, 32, 1),
    copper(256, 128, 2),
    gold(256, 512, 3),
    iron(256, 2048, 4),
    glass(256, 8192, 5);

    private final int capacity;
    private final int transfer;
    private final int tier;

    CableTier(int capacity, int transfer, int tier) {
        this.capacity = capacity;
        this.transfer = transfer;
        this.tier = tier;
    }

    public int getCapacity() { return capacity; }
    public int getTransfer() { return transfer; }
    public int getTier() { return tier; }
}
