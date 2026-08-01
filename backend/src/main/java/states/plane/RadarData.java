package states.plane;

import java.util.Map;

public record RadarData(
        int planeId,
        String type,
        String destination,
        String startingPoint,
        double fuelRemaining,
        double consumptionRate,
        String emergency,
        double cargoWeight,
        int passengerCount,
        Map<String, Object> stateDate
) { }
