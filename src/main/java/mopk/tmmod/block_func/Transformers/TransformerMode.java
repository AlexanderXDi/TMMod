package mopk.tmmod.block_func.Transformers;

public enum TransformerMode {
    DOWN("down"),
    UP("up");

    private final String name;

    TransformerMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public TransformerMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
