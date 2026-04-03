package mopk.tmmod.block_func.Cables;

public enum CableTier {
    tin(256, 32, 1, 1),
    copper(256, 128, 2, 1),
    gold(256, 512, 3, 2),
    iron(256, 2048, 4, 3),
    glass(256, 8192, 5, 0);

    private final int capacity;
    private final int transfer;
    private final int tier;
    private final int needsRubber;

    CableTier(int capacity, int transfer, int tier, int needsRubber) {
        this.capacity = capacity;
        this.transfer = transfer;
        this.tier = tier;
        this.needsRubber = needsRubber;
    }

    public int getCapacity() { return capacity; }
    public int getTransfer() { return transfer; }
    public int getTier() { return tier; }
    public int getNeedsRubber() { return needsRubber; }
}
