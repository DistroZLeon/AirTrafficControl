package config.parser;

public record Token(
        JsonToken type,
        String value
) { }
