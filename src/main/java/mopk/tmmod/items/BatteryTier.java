package mopk.tmmod.items;

public enum BatteryTier {
    BATTERY("battery", 10_000, 1, 32),
    ADVANCED_BATTERY("advanced_battery", 100_000, 2, 128),
    ENERGY_CRYSTAL("energy_crystal", 1_000_000, 3, 512),
    LAPOTRON_CRYSTAL("lapotron_crystal", 10_000_000, 4, 2048);

    private final String name;
    private final int maxEnergy;
    private final int tier;
    private final int transfer;

    BatteryTier(String name, int maxEnergy, int tier, int transfer) {
        this.name = name;
        this.maxEnergy = maxEnergy;
        this.tier = tier;
        this.transfer = transfer;
    }

    public String getName() {
        return name;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public int getTier() {
        return tier;
    }

    public int getTransfer() {
        return transfer;
    }
}
