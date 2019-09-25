package org.daisleyharrison.security.utilities.tokenizer;

public class Location {
    private int line;
    private int column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Location(Location value) {
        this.line = value.line;
        this.column = value.column;
    }

    public Location() {
        this.line = 0;
        this.column = 0;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    void incrementLine() {
        line++;
        column = 0;
    }

    void incrementColumn() {
        column++;
    }

    @Override
    public String toString() {
        return "@" + Integer.toString(line) + "," + Integer.toString(column);
    }
}