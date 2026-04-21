package mopk.tmmod.block_func.Cables;

public enum CableTier {
    tin(32, 1, 1),
    copper(128, 2, 1),
    gold(512, 3, 2),
    iron(2048, 4, 3),
    glass(8192, 5, 0);

    private final int transfer;
    private final int tier;
    private final int needsRubber;

    CableTier(int transfer, int tier, int needsRubber) {
        this.transfer = transfer;
        this.tier = tier;
        this.needsRubber = needsRubber;
    }

    public int getTransfer() { return transfer; }
    public int getTier() { return tier; }
    public int getNeedsRubber() { return needsRubber; }
}
