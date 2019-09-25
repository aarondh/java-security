package org.daisleyharrison.security.utilities.tokenizer;

import org.daisleyharrison.security.utilities.tokenizer.Lexicon;
import org.daisleyharrison.security.utilities.tokenizer.Token;

public class AsciiLexicon extends Lexicon {
    private static AsciiLexicon _instance;

    public static AsciiLexicon getInstance() {
        if (_instance == null) {
            _instance = new AsciiLexicon();
        }
        return _instance;
    }

    protected AsciiLexicon() {
    }
    @Override
    protected void reset() {
        super.reset();
        defineCharacter(0, Token.Type.NULL);
        defineCharacters(1, 31, Token.Type.CONTROL);
        defineCharacter('\n', Token.Type.LINE_FEED);
        defineCharacter('\r', Token.Type.CARRIAGE_RETURN);
        defineCharacter('\t', Token.Type.TAB);
        defineCharacter(32, Token.Type.SPACE);
        defineCharacters(33, 126, Token.Type.CHAR);
        defineCharacter(127, Token.Type.DEL);
    }

    @Override
    protected void populate() {
        defineCharacter('!', Token.Type.WHITESPACE);
        defineCharacter('"', Token.Type.QUOTE);
        defineCharacter('#', Token.Type.POUND);
        defineCharacter('$', Token.Type.DOLOR);
        defineCharacter('%', Token.Type.PERCENT);
        defineCharacter('&', Token.Type.AMPERSAND);
        defineCharacter('\'', Token.Type.APOSTROPHE);
        defineCharacter('(', Token.Type.OPEN_PAREN);
        defineCharacter(')', Token.Type.CLOSE_PAREN);
        defineCharacter('*', Token.Type.ASTERISK);
        defineCharacter('+', Token.Type.PLUS);
        defineCharacter(',', Token.Type.COMMA);
        defineCharacter('-', Token.Type.MINUS);
        defineCharacter('.', Token.Type.PERIOD);
        defineCharacter('/', Token.Type.SLASH);
        defineCharacters('0', '9', Token.Type.DIGIT);
        defineCharacter(':', Token.Type.COLON);
        defineCharacter(';', Token.Type.SEMICOLON);
        defineCharacter('<', Token.Type.LESS_THAN);
        defineCharacter('=', Token.Type.EQUALS);
        defineCharacter('>', Token.Type.GREATER_THAN);
        defineCharacter('?', Token.Type.QUESTION);
        defineCharacter('@', Token.Type.AT_SIGN);
        defineCharacters('A', 'Z', Token.Type.LETTER);
        defineCharacter('[', Token.Type.OPEN_BRACKET);
        defineCharacter('\\', Token.Type.ESCAPE);
        defineCharacter(']', Token.Type.CLOSE_BRACKET);
        defineCharacter('^', Token.Type.CIRCUMFLEX);
        defineCharacter('_', Token.Type.UNDERBAR);
        defineCharacter('`', Token.Type.GRAVE);
        defineCharacters('a', 'z', Token.Type.LETTER);
        defineCharacter('{', Token.Type.OPEN_CURLY);
        defineCharacter('|', Token.Type.VERTICALBAR);
        defineCharacter('}', Token.Type.CLOSE_CURLY);
        defineCharacter('~', Token.Type.TILDA);
    }

}