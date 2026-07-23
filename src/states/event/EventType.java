package states.event;

public enum EventType {
    BIRD_STRIKE(EventGravity.MINOR),
    MEDICAL_ISSUE(EventGravity.MINOR),
    HYDRAULIC_FAILURE(EventGravity.CRUCIAL),
    FUEL_LEAK(EventGravity.CRUCIAL),
    ENGINE_FAILURE(EventGravity.CRITICAL);

    private final EventGravity gravity;

    EventType(EventGravity gravity) {
        this.gravity= gravity;
    }

    public EventGravity getGravity() {
        return gravity;
    }
}
