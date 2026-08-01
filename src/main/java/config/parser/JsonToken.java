package config.parser;

public enum JsonToken {
    START_OBJECT,
    END_OBJECT,
    START_ARRAY,
    END_ARRAY,
    COLON,
    COMMA,
    STRING,
    NUMBER,
    EOF
}
