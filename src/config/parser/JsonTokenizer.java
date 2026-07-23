package config.parser;

import java.io.IOException;
import java.io.Reader;

public class JsonTokenizer {
    private final Reader reader;
    private int currentChar;
    
    public JsonTokenizer(Reader reader) throws IOException {
        this.reader = reader;
        advance();
    }

    private void advance() throws IOException {
        this.currentChar = this.reader.read();
    }

    private void skipWhitespace() throws IOException {
        while (this.currentChar!= -1&& Character.isWhitespace(this.currentChar)) {
            this.currentChar = this.reader.read();
        }
    }

    private Token getNextToken() throws IOException {
        skipWhitespace();
        if (this.currentChar== -1) {
            return new Token(JsonToken.EOF, "");
        }
        char c= (char)this.currentChar;
        switch (c) {
            case '{': advance(); return new Token(JsonToken.START_OBJECT, "{");
            case '[': advance(); return new Token(JsonToken.START_ARRAY, "[");
            case '}': advance(); return new Token(JsonToken.END_OBJECT, "}");
            case ']': advance(); return new Token(JsonToken.END_ARRAY, "]");
            case ':': advance(); return new Token(JsonToken.COLON, ":");
            case ',': advance(); return new Token(JsonToken.COMMA, ",");
            case '"': return readString();
        }

        if(Character.isDigit(c)|| c== '-') return readNumber();

        throw new RuntimeException("Unexpected character: " + c);
    }

    private Token readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean hasDecimal = false;

        if(currentChar == '-') {
            sb.append((char) currentChar);
            advance();
        }

        while (currentChar != -1) {
            if (Character.isDigit(currentChar)) {
                sb.append((char) this.currentChar);
                advance();
            } else if (currentChar == '.') {
                if (hasDecimal) {
                    throw new RuntimeException("Invalid JSON number!");
                }
                hasDecimal = true;
                sb.append((char) currentChar);
                advance();
            } else {
                break;
            }
        }

        String result = sb.toString();
        if (result.equals("-") || result.equals("-.")) {
            throw new RuntimeException("Invalid JSON number format!");
        }
        return new Token(JsonToken.NUMBER, result);
    }

    private Token readString() throws IOException{
        advance();
        StringBuilder sb = new StringBuilder();

        while(currentChar != -1 && currentChar != '"') {
            sb.append((char) this.currentChar);
            advance();
        }

        if (currentChar == '"') {
            advance();
        } else {
            throw new RuntimeException("Unterminated string");
        }

        return new Token(JsonToken.STRING, sb.toString());
    }
}
