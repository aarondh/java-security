package org.daisleyharrison.security.utilities.tokenizer;

import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class Tokenizer implements Iterable<Token> {

    private Reader reader;
    private Stack<Token> tokenStack;
    private Token token;
    private Token.Type previousTokenType;
    private Stack<Location> locationStack;
    private Location location;
    private Lexicon lexicon;

    public Tokenizer(Lexicon lexicon, Reader reader) {
        this.lexicon = lexicon;
        this.reader = reader;
        this.tokenStack = new Stack<>();
        this.token = new Token(Token.Type.EOF);
        this.locationStack = new Stack<>();
        this.location = new Location();
        this.previousTokenType = Token.Type.UNDEFINED;
    }

    public Location getLocation() {
        return location;
    }

    public Token getToken() {
        return this.token;
    }

    private void throwExpected(String expected) throws IOException {
        throw new IOException(expected + " " + getLocation().toString());
    }

    private boolean isHexDigit(int token) {
        if (token >= '0' && token <= '9') {
            return true;
        }
        if (token >= 'a' && token <= 'f') {
            return true;
        }
        if (token >= 'A' && token <= 'F') {
            return true;
        }
        return false;
    }

    private boolean isOctalDigit(int token) {
        if (token >= '0' && token <= '7') {
            return true;
        }
        return false;
    }

    private Token.Type parseHex() throws IOException {
        char[] hex = new char[2];
        int charRead = reader.read();
        if (!isHexDigit(charRead)) {
            throwExpected("Hex digit 0-9 | A-F");
        }
        hex[0] = (char) charRead;
        charRead = reader.read();
        if (!isHexDigit(charRead)) {
            throwExpected("Hex digit 0-9 | A-F");
        }
        hex[1] = (char) charRead;
        this.token.setValue((char) Integer.parseInt(new String(hex), 16));
        this.token.setType(Token.Type.CHAR);
        return Token.Type.CHAR;
    }

    private Token.Type parseOctal() throws IOException {
        char[] octal = new char[3];
        int charRead = reader.read();
        if (!isHexDigit(charRead)) {
            throwExpected("Octal digit 0-7");
        }
        octal[0] = (char) charRead;
        charRead = reader.read();
        if (!isOctalDigit(charRead)) {
            throwExpected("Octal digit 0-7");
        }
        octal[1] = (char) charRead;
        charRead = reader.read();
        if (!isOctalDigit(charRead)) {
            throwExpected("Octal digit 0-7");
        }
        octal[2] = (char) charRead;
        this.token.setValue((char) Integer.parseInt(new String(octal), 8));
        this.token.setType(Token.Type.CHAR);
        return Token.Type.CHAR;
    }

    private Token.Type parseEscape() throws IOException {
        int charRead = reader.read();
        if (charRead != -1) {
            if (charRead == 'x' || charRead == 'X') {
                return parseHex();
            }
            if (charRead == 'o' || charRead == 'O') {
                return parseOctal();
            }
            this.token.setValue((char) charRead);
            this.token.setType(Token.Type.CHAR);
            return Token.Type.CHAR;
        }
        this.token.setValue(null);
        this.token.setType(Token.Type.EOF);
        return Token.Type.EOF;

    }

    private Token.Type parseQuote(int endQuote) throws IOException {
        Token.Type type;
        boolean eos = false;

        StringBuilder text = new StringBuilder();
        do {
            int charRead = reader.read();
            type = lexicon.map(charRead);
            if (type == Token.Type.ESCAPE && lexicon.isParseEscapes()) {
                parseEscape();
                text.append(token.getCharValue());
            } else if (type == Token.Type.EOF) {
                eos = true;
            } else if (charRead == endQuote) {
                if (lexicon.isDoubleQuoteEscape()) {
                    int peek = peekReader();
                    eos = (peek != endQuote);
                } else {
                    eos = true;
                }
                if (!eos) {
                    text.append((char) charRead);
                }
            }

        } while (!eos);

        reader.reset();

        token.setType(Token.Type.STRING);

        token.setValue(text.toString());

        return Token.Type.STRING;
    }

    private Token.Type parseNumber(Token.Type opening, char openingChar) throws IOException {
        Token.Type type;
        boolean period = false;
        boolean exponent = false;
        boolean eon = false;

        StringBuilder number = new StringBuilder();

        Token.Type previous = peekPreviousTokenType();
        switch (previous) {
        case NUMBER:
        case DIGIT:
        case LETTER:
            this.token.setType(opening);
            this.token.setValue(openingChar);
            return opening;
        default:
            break;
        }

        if (opening == Token.Type.MINUS) {
            if (!lexicon.isParseNegativeNumbers()) {
                this.token.setType(opening);
                this.token.setValue(openingChar);
                return opening;
            }
            Token.Type peek = lexicon.map(peekReader());
            if (peek != Token.Type.DIGIT) {
                this.token.setType(opening);
                this.token.setValue(openingChar);
                return opening;
            }
            number.append(openingChar);
        } else if (opening == Token.Type.DIGIT) {
            number.append(openingChar);
        }

        do {
            reader.mark(1);
            int charRead = reader.read();
            type = lexicon.map(charRead);
            switch (type) {
            case PERIOD:
                if (period) {
                    eon = true;
                    break;
                }
                period = true;
            case DIGIT:
                number.append((char) charRead);
                break;
            default:
                if(charRead == 'e' || charRead == 'E'){
                    if(exponent){
                        eon = true;
                    } else {
                        exponent = true;
                    }
                }
                eon = true;
                break;
            }
        } while (!eon);

        reader.reset();

        token.setType(Token.Type.NUMBER);

        token.setValue(number.toString());

        return Token.Type.NUMBER;
    }

    public Token.Type peekToken() throws IOException {
        Token.Type token = nextToken();
        pushToken();
        return token;
    }

    public void pushToken() {
        if (token.getType() == Token.Type.UNDEFINED) {
            throw new IllegalStateException("There are no tokens to push");
        }
        locationStack.push(location);
        location = new Location(location);
        tokenStack.push(token);
        token = new Token();
        previousTokenType = Token.Type.UNDEFINED;
    }

    /**
     * Look ahead in the token stream for a matching token or EOF
     * 
     * The matching token type or EOF will be returned. The token stream will be
     * preserved.
     * 
     * @param tokens The set of token types to scan for
     * @return Token.Type
     * @throws IOException
     */
    public Token.Type lookAhead(Token.Type... tokens) throws IOException {
        LinkedList<Token> tokensRead = new LinkedList<>();
        Token.Type type;
        boolean end = false;
        do {
            type = nextToken();
            if (type == Token.Type.EOF) {
                break;
            }
            tokensRead.addLast(this.token);
            this.token = new Token();
            for (Token.Type test : tokens) {
                if (test == type) {
                    end = true;
                    break;
                }
            }
        } while (!end);

        while (!tokensRead.isEmpty()) {
            this.token = tokensRead.removeLast();
            this.pushToken();
        }

        return type;

    }

    /**
     * Get the next token note: to get the full token call getToken();
     * 
     * @return Token.Type the type of token
     * @throws IOException
     */
    public Token.Type nextToken() throws IOException {
        previousTokenType = this.token.getType();

        if (this.tokenStack.isEmpty()) {
            return readNextToken();
        }

        this.token = this.tokenStack.pop();
        this.location = this.locationStack.pop();
        return this.token.getType();
    }

    /**
     * peek at the previous tokens type
     * 
     * if no previous token the token type will be Token.Type.UNDEFINED
     * 
     * @return Token.Type
     */
    public Token.Type peekPreviousTokenType() {
        if (previousTokenType == Token.Type.UNDEFINED) {
            if (!tokenStack.empty()) {
                return tokenStack.peek().getType();
            }
        }
        return previousTokenType;
    }

    /**
     * Peek ahead one character in the char stream from the reader
     */
    private int peekReader() throws IOException {
        reader.mark(1);
        int charRead = reader.read();
        reader.reset();
        return charRead;
    }

    /**
     * Read the next character from the reader and turn it into a token
     * 
     * @return Token.Type The type of token read
     * @throws IOException
     */
    private Token.Type readNextToken() throws IOException {

        int charRead = reader.read();

        if (charRead == -1) {
            this.token.setType(Token.Type.EOF);
            this.token.setValue((char) 0);
            return Token.Type.EOF;
        }

        switch (charRead) {
        case '\r':
            break;
        case '\n':
            location.incrementLine();
            break;
        default:
            location.incrementColumn();
            break;
        }

        Token.Type tokenType = lexicon.map(charRead);

        switch (tokenType) {
        case ESCAPE:
            if (lexicon.isParseEscapes()) {
                return parseEscape();
            }
            break;
        case QUOTE:
            if (lexicon.isParseQuote()) {
                return parseQuote(charRead);
            }
            break;
        case MINUS:
        case DIGIT:
            if (lexicon.isParseNumbers()) {
                return parseNumber(tokenType, (char) charRead);
            }
            break;
        default:
            break;
        }

        this.token.setValue((char) charRead);
        this.token.setType(tokenType);

        return tokenType;
    }

    /**
     * @return Reader return the reader
     */
    public Reader getReader() {
        return reader;
    }

    /**
     * @param reader the reader to set
     */
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * @return Stack<Token> return the tokenStack
     */
    public Stack<Token> getTokenStack() {
        return tokenStack;
    }

    /**
     * @param tokenStack the tokenStack to set
     */
    public void setTokenStack(Stack<Token> tokenStack) {
        this.tokenStack = tokenStack;
    }

    /**
     * @param token the token to set
     */
    public void setToken(Token token) {
        this.token = token;
    }

    /**
     * @return Token.Type return the previousTokenType
     */
    public Token.Type getPreviousTokenType() {
        return previousTokenType;
    }

    /**
     * @param previousTokenType the previousTokenType to set
     */
    public void setPreviousTokenType(Token.Type previousTokenType) {
        this.previousTokenType = previousTokenType;
    }

    /**
     * @return Stack<Location> return the locationStack
     */
    public Stack<Location> getLocationStack() {
        return locationStack;
    }

    /**
     * @param locationStack the locationStack to set
     */
    public void setLocationStack(Stack<Location> locationStack) {
        this.locationStack = locationStack;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return Lexicon return the lexicon
     */
    public Lexicon getLexicon() {
        return lexicon;
    }

    /**
     * @param lexicon the lexicon to set
     */
    public void setLexicon(Lexicon lexicon) {
        this.lexicon = lexicon;
    }

    private class TokenIterator implements Iterator<Token> {
        private Tokenizer tokenizer;

        public TokenIterator(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
        }

    /**
     * Returns {@code true} if the tokenizer has more tokens.
     *
     * @return {@code true} if the tokenizer has more tokens
     */
    @Override
        public boolean hasNext() {
            try {
                return tokenizer.peekToken() != Token.Type.EOF;
            } catch (IOException ex) {
                return false;
            }
        }

    /**
     * Returns the next token from the tokenizer.
     *
     * @return the next token in the tokenizer
     * @throws NoSuchElementException if the tokenizer has no more tokens
     */
    @Override
        public Token next() {
            try {
                if (tokenizer.nextToken() == Token.Type.EOF) {
                    throw new NoSuchElementException();
                } else {
                    return getToken().clone();
                }
            } catch (IOException ex) {
                throw new NoSuchElementException();
            }
        }
    }

    /**
     * returns a token iterator for this tokenizer
     */
    @Override
    public Iterator<Token> iterator() {
        return new TokenIterator(this);
    }

    /**
     * returns the tokens as a stream()
     * @return
     */
    public Stream<Token> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

}
