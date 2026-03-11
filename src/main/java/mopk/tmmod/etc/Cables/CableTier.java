package mopk.tmmod.etc.Cables;

public enum CableTier {
    COPPER(1000, 128),
    IRON(4000, 512),
    GOLD(16000, 2048),
    DIAMOND(64000, 8192);

    private final int capacity;
    private final int transfer;

    CableTier(int capacity, int transfer) {
        this.capacity = capacity;
        this.transfer = transfer;
    }

    public int getCapacity() { return capacity; }
    public int getTransfer() { return transfer; }
}
