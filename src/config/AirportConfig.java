package config;

public record AirportConfig(
    String airportName,
    int numberOfGates,
    int numberOfRunways,
    int numberOfTakeOffTaxiways,
    int numberOfLandingTaxiways
) { }
