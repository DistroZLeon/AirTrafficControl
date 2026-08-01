package states.weather;

public record WeatherState(
        WeatherType type,
        double windStrength,
        double visibility,
        int lanesClose,
        double consumptionRate
){}
