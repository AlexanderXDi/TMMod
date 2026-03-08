package mopk.tmmod.events_and_else.BatteryBlock;

public enum BatteryBlockMode {
    BOTH, INPUT, OUTPUT;

    public BatteryBlockMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}

