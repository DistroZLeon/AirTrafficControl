package config;

public record GeneratorSettings(
        int WeatherUpdateIntervalsSeconds,
        int EmergencyGenerationIntervalsSeconds
) { }
