package org.daisleyharrison.security.utilities.tokenizer;

import java.util.HashMap;
import java.util.Map;

import org.daisleyharrison.security.utilities.tokenizer.Token;

public abstract class Lexicon {

    private Map<Integer, Token.Type> charMap;

    private boolean parseQuote;
    private boolean parseEscapes;
    private boolean parseNumbers;
    private boolean doubleQuoteEscape;
    private boolean parseNegativeNumbers;

    public Lexicon() {
        charMap = new HashMap<>();
        reset();
        populate();
    }

    public void defineCharacters(int low, int hi, Token.Type tokenType) {
        for (int i = low; i < hi; i++) {
            defineCharacter((char) i, tokenType);
        }
    }

    public void defineCharacter(int c, Token.Type tokenType) {
        charMap.put(c, tokenType);
    }

    public Token.Type map(int c) {
        Token.Type type = charMap.get(c);
        if (type == null) {
            type = Token.Type.UNKNOWN;
            charMap.put(c, type);
        }
        return type;
    }

    protected void reset() {
        charMap.clear();
    }

    protected abstract void populate();

    /**
     * @return Map<Integer, Token.Type> return the charMap
     */
    public Map<Integer, Token.Type> getCharMap() {
        return charMap;
    }

    /**
     * @param charMap the charMap to set
     */
    public void setCharMap(Map<Integer, Token.Type> charMap) {
        this.charMap = charMap;
    }

    /**
     * @return boolean return the parseQuote
     */
    public boolean isParseQuote() {
        return parseQuote;
    }

    /**
     * @param parseQuote the parseQuote to set
     */
    public void setParseQuote(boolean parseQuote) {
        this.parseQuote = parseQuote;
    }

    /**
     * @return boolean return the parseEscapes
     */
    public boolean isParseEscapes() {
        return parseEscapes;
    }

    /**
     * @param parseEscapes the parseEscape to set
     */
    public void setParseEscapes(boolean parseEscapes) {
        this.parseEscapes = parseEscapes;
    }

    /**
     * @return boolean return the parseNumbers
     */
    public boolean isParseNumbers() {
        return parseNumbers;
    }

    /**
     * @param parseNumbers the parseNumbers to set
     */
    public void setParseNumbers(boolean parseNumbers) {
        this.parseNumbers = parseNumbers;
    }

    /**
     * @return boolean return the doubleQuoteEscape
     */
    public boolean isDoubleQuoteEscape() {
        return doubleQuoteEscape;
    }

    /**
     * @param doubleQuoteEscape the doubleQuoteEscape to set
     */
    public void setDoubleQuoteEscape(boolean doubleQuoteEscape) {
        this.doubleQuoteEscape = doubleQuoteEscape;
    }

    /**
     * @return boolean return the parseNegativeNumbers
     */
    public boolean isParseNegativeNumbers() {
        return parseNegativeNumbers;
    }

    /**
     * @param parseNegativeNumbers the parseNegativeNumbers to set
     */
    public void setParseNegativeNumber(boolean parseNegativeNumbers) {
        this.parseNegativeNumbers = parseNegativeNumbers;
    }

}