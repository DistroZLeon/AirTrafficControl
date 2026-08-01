package states.event;

public enum EventGravity {
    MINOR(0),
    CRUCIAL(1),
    CRITICAL(2);

    private final int value;
    EventGravity(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
