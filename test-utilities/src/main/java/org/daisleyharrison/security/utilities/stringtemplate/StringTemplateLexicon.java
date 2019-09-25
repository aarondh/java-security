package org.daisleyharrison.security.utilities.stringtemplate;

import org.daisleyharrison.security.utilities.tokenizer.AsciiLexicon;
import org.daisleyharrison.security.utilities.tokenizer.Token;

public class StringTemplateLexicon extends AsciiLexicon {
    private static StringTemplateLexicon _instance;

    public static StringTemplateLexicon getInstance() {
        if (_instance == null) {
            _instance = new StringTemplateLexicon();
        }
        return _instance;
    }

    private StringTemplateLexicon() {
        super();
    }

    @Override
    protected void populate() {
        super.populate();
        
        defineCharacter(' ', Token.Type.WHITESPACE);
        defineCharacter('\t', Token.Type.WHITESPACE);
        defineCharacter('\r', Token.Type.WHITESPACE);
        defineCharacter('\n', Token.Type.WHITESPACE);
        defineCharacter('\f', Token.Type.WHITESPACE);
        defineCharacter('\\', Token.Type.ESCAPE);

        setParseEscapes(true);
        setParseNumbers(true);
    }

}