package states.weather;

public enum WeatherType {
    CLEAR(1.0, 1.0, 0),
    RAINY(1.2, 0.8, 0),
    STORMY(1.5, 0.4, 1),
    BLIZZARD(2.0, 0.1, 2);

    private final double baseFuelConsumption;
    private final double baseVisibility;
    private final int baseLanesClosed;

    WeatherType(double baseFuelConsumption, double baseVisibility, int baseLanesClosed){
        this.baseFuelConsumption = baseFuelConsumption;
        this.baseVisibility = baseVisibility;
        this.baseLanesClosed = baseLanesClosed;
    }

    public double getBaseFuelConsumption() {
        return baseFuelConsumption;
    }

    public double getBaseVisibility() {
        return baseVisibility;
    }

    public int getBaseLanesClosed() {
        return baseLanesClosed;
    }
}
