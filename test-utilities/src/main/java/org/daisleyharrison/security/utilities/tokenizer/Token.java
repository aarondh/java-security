package org.daisleyharrison.security.utilities.tokenizer;

public class Token implements Cloneable {
    public static enum Type {
        UNDEFINED, EOF, UNKNOWN, NULL, CONTROL, CARRIAGE_RETURN, LINE_FEED, TAB,  SPACE, CHAR, APOSTROPHE, OPEN_PAREN, OPEN_CURLY, OPEN_BRACKET, CLOSE_PAREN, CLOSE_CURLY, CLOSE_BRACKET,
        LESS_THAN, GREATER_THAN, SEPARATOR, VERTICALBAR, MINUS, PLUS, EQUALS, COLON, SEMICOLON, AMPERSAND, EXCLAMATION, TILDA, DOLOR, PERCENT, POUND, AT_SIGN, UNDERBAR, SLASH, PERIOD, COMMA,
        CIRCUMFLEX, GRAVE, ASTERISK, QUESTION, DEL,

        QUOTE, DIGIT, LETTER, WHITESPACE, ESCAPE, NUMBER, STRING
    }

    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token(Type token) {
        this.type = token;
        this.value = null;
    }

    public Token() {
        this.type = Type.UNDEFINED;
        this.value = null;
    }

    public Token clone(){
        return new Token(type,value);
    }

    public String getStringValue() {
        return value;
    }

    public Double getNumberValue() {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    public String getCharValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = String.valueOf((char) value);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return (type == null ? "null" : type.toString()) + ": " + (value == null ? "null" : "\"" + value + "\"");
    }
}
