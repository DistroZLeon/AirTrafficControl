package config;
import simulator.plane.Plane;
import java.util.List;

public record Config(
        AirportConfig airportConfig,
        GeneratorSettings generatorSettings,
        List<Plane> planes
) { }
