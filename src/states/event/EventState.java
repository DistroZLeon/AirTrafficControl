package states.event;

public record EventState(
        int targetId,
        EventType type,
        double fuelDrainRate,
        String description
) { }
