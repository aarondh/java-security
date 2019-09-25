package org.daisleyharrison.security.utilities.stringtemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.daisleyharrison.security.utilities.stringtemplate.internal.CompiledTemplateImpl;
import org.daisleyharrison.security.utilities.stringtemplate.internal.GeneratorBase;
import org.daisleyharrison.security.utilities.stringtemplate.internal.GeneratorGenerator;
import org.daisleyharrison.security.utilities.stringtemplate.internal.GeneratorProvider;
import org.daisleyharrison.security.utilities.stringtemplate.Modifier;
import org.daisleyharrison.security.utilities.stringtemplate.internal.SetGenerator;
import org.daisleyharrison.security.utilities.stringtemplate.internal.TextGenerator;
import org.daisleyharrison.security.utilities.tokenizer.Tokenizer;
import org.daisleyharrison.security.utilities.tokenizer.Token;
import org.daisleyharrison.security.utilities.tokenizer.Location;

public class StringTemplateCompiler {

    private MacroProvider macroProvider;
    private SetProvider setProvider;
    private GeneratorProvider generatorProvider;
    private ModifierProvider modifierProvider;

    public class Context {
        private Tokenizer tokenizer;
        private Stack<CompiledTemplateImpl> stack;
        private CompiledTemplateImpl current;

        public Context(Reader reader) {
            this.tokenizer = new Tokenizer(StringTemplateLexicon.getInstance(), reader);
            this.stack = new Stack<>();
            this.current = new CompiledTemplateImpl();
        }

        public Token.Type nextToken() throws IOException {
            return tokenizer.nextToken();
        }

        public Token.Type lookAhead(Token.Type... tokens) throws IOException {
            return this.tokenizer.lookAhead(tokens);
        }

        public Token.Type peekToken() throws IOException {
            return tokenizer.peekToken();
        }

        public String getTokenValue() {
            return tokenizer.getToken().getStringValue();
        }

        public Double getTokenNumberValue() {
            return tokenizer.getToken().getNumberValue();
        }

        public Location getLocation() {
            return tokenizer.getLocation();
        }

        public void pushToken() {
            tokenizer.pushToken();
        }

        public void push() {
            this.stack.add(this.current);
            this.current = new CompiledTemplateImpl();
        }

        public CompiledTemplateImpl pop() {
            CompiledTemplateImpl popped = this.current;
            if (this.stack.isEmpty()) {
                this.current = null;
            } else {
                this.current = this.stack.pop();
            }
            return popped;
        }

        public CompiledTemplateImpl getCurrent() {
            return current;
        }

        public void add(GeneratorBase generator) {
            this.current.add(generator);
        }
    }

    public StringTemplateCompiler() {
        this.macroProvider = n -> null;
        this.setProvider = n -> null;
        this.generatorProvider = n -> null;
        this.modifierProvider = n -> null;

    }

    private String parseWhitespace(Context context) throws IOException {
        StringBuilder whitespace = new StringBuilder();
        Token.Type next;
        while ((next = context.nextToken()) != Token.Type.EOF) {
            if (next != Token.Type.WHITESPACE) {
                context.pushToken();
                break;
            }
            whitespace.append(context.getTokenValue());
        }
        return whitespace.toString();
    }

    private boolean isTokenOneOf(Token.Type token, Token.Type... tokens) {
        if (tokens == null) {
            return false;
        }
        for (Token.Type test : tokens) {
            if (token == test) {
                return true;
            }
        }
        return false;
    }

    private String parseText(Context context, Token.Type... endTokens) throws IOException {
        StringBuilder text = new StringBuilder();
        Token.Type token;
        boolean eof = false;
        do {
            token = context.nextToken();
            if (isTokenOneOf(token, endTokens)) {
                context.pushToken();
                break;
            }
            switch (token) {
            case EOF:
                eof = true;
                break;
            default:
                text.append(context.getTokenValue());
                break;
            }
        } while (!eof);

        return text.toString();
    }

    private void throwExpected(Context context, String expected) throws IOException {
        throw new IOException(expected + " " + context.getLocation().toString());
    }

    private void parseBrace(Context context) throws IOException {
        Token.Type next;
        boolean eof = false;
        context.push();
        do {
            parseExpression(context);
            next = context.nextToken();
            if (next == Token.Type.CLOSE_PAREN) {

                CompiledTemplateImpl current = context.pop();
                context.add(current);

                String trailing = parseWhitespace(context);
                next = context.nextToken();
                if (next == Token.Type.OPEN_BRACKET) {
                    parseParameters(context);
                } else {
                    context.pushToken();
                    if (!trailing.isEmpty()) {
                        context.add(new TextGenerator(trailing));
                    }

                }
                return;
            } else if (next == Token.Type.EOF) {
                eof = true;
                throwExpected(context, "Unbalanced braces here");
            } else {
                context.pushToken();
            }
        } while (!eof);
    }

    private void parseSet(Context context) throws IOException {
        String modifierName = null;
        String setName = parseText(context, Token.Type.COLON, Token.Type.GREATER_THAN, Token.Type.CLOSE_PAREN);
        Token.Type next = skipWhitespace(context);
        if (next == Token.Type.COLON) {
            modifierName = parseText(context, Token.Type.GREATER_THAN, Token.Type.CLOSE_PAREN);
            next = skipWhitespace(context);
        }
        if (next != Token.Type.GREATER_THAN) {
            throwExpected(context, "Expected a > here");
        }
        String macro = getMacroProvider().resolveMacro(setName);
        if (macro != null) {
            CompiledTemplate compiled = compile(macro);
            if(compiled instanceof GeneratorBase){
                context.add((GeneratorBase)compiled);
            }
        } else {
            Generator generator = getGeneratorProvider().resolveGenerator(setName);
            if (generator != null) {
                context.add(new GeneratorGenerator(setName, generator));
            } else {
                String[] set = getSetProvider().resolveSet(setName);
                if (set == null) {
                    throwExpected(context, "Set <" + setName + "> was not defined");
                } else {
                    context.add(new SetGenerator(setName, set));
                }
                if (modifierName != null) {
                    Modifier modifier = getModifierProvider().resolveModifier(modifierName);
                    if (modifier == null) {
                        throwExpected(context, "Modifier <...:" + modifierName + "> was not defined");
                    }
                    GeneratorBase generatorBase = context.getCurrent().getLast();
                    generatorBase.setModifier(modifier);
                }
            }
        }
    }

    private void parseWords(Context context) throws IOException {
        Token.Type token;
        List<String> words = new ArrayList<>();
        parseWhitespace(context);
        do {
            String word = parseText(context, Token.Type.WHITESPACE, Token.Type.CLOSE_CURLY, Token.Type.CLOSE_PAREN);
            words.add(word);
            token = context.peekToken();
            if (token == Token.Type.WHITESPACE) {
                parseWhitespace(context);
                token = context.peekToken();
            }
        } while (token != Token.Type.CLOSE_CURLY && token != Token.Type.CLOSE_PAREN && token != Token.Type.EOF);
        if (token != Token.Type.CLOSE_CURLY) {
            throwExpected(context, "Expected a } here");
        }
        context.nextToken(); // eat the CLOSE_PAREN

        String[] set = words.toArray(String[]::new);
        context.add(new SetGenerator(null, set));
    }

    private class FormatModifier implements Modifier {
        private Modifier parent;
        private String format;

        public FormatModifier(Modifier parent, String format) {
            this.parent = parent;
            this.format = format;
        }

        @Override
        public Object apply(Object t) {
            return String.format(format, parent.apply(t));
        }
    }

    private void parseSeparatorAndFormat(Context context) throws IOException {
        final String separator = parseText(context, Token.Type.VERTICALBAR, Token.Type.CLOSE_PAREN);
        String format = "";
        Token.Type token = context.nextToken();
        if (token != Token.Type.VERTICALBAR) {
            throwExpected(context, "Expected a | character here");
        }
        token = context.lookAhead(Token.Type.VERTICALBAR, Token.Type.CLOSE_PAREN);
        if (token == Token.Type.VERTICALBAR) {
            format = parseText(context, Token.Type.VERTICALBAR);
            token = context.nextToken();
            if(token != Token.Type.VERTICALBAR){
                throwExpected(context, "Expected a | character here");
            }
        }
        GeneratorBase generator = context.getCurrent().getLast();
        if (!separator.isEmpty()) {
            generator.setSeparator(separator);
        }
        if (!format.isEmpty()) {
            final Modifier modifier = generator.getModifier();
            generator.setModifier(new FormatModifier(modifier, format));
        }
    }

    private Token.Type skipWhitespace(Context context) throws IOException {
        parseWhitespace(context);
        return context.nextToken();
    }

    private void parseParameters(Context context) throws IOException {
        Token.Type next;
        double probability = Double.NaN;
        double min = Double.NaN;
        double max = Double.NaN;
        next = skipWhitespace(context);
        if (next == Token.Type.NUMBER) {
            probability = context.getTokenNumberValue();
            next = skipWhitespace(context);
            if (next == Token.Type.MINUS) {
                min = probability;
                probability = Double.NaN;
                next = skipWhitespace(context);
                if (next == Token.Type.NUMBER) {
                    max = context.getTokenNumberValue();
                    next = skipWhitespace(context);
                } else {
                    throwExpected(context, "Expected a number (max) here");
                }
            } else if (next == Token.Type.NUMBER) {
                min = context.getTokenNumberValue();
                next = skipWhitespace(context);
                if (next == Token.Type.MINUS) {
                    next = skipWhitespace(context);
                    if (next == Token.Type.NUMBER) {
                        max = context.getTokenNumberValue();
                        next = skipWhitespace(context);
                    } else {
                        throwExpected(context, "Expected a number (max) here");
                    }
                } else {
                    max = min;
                }
            } else if ( probability > 1.0 ) {
                min = max = probability;
                probability = Double.NaN;
            }
        } else {
            throwExpected(context, "Expected numbers for { probability | min | min \"-\" max } here");
        }

        if (next != Token.Type.CLOSE_BRACKET)
        {
            throwExpected(context, "Expected a ] here");
        }

        GeneratorBase generator = context.getCurrent().getLast();

        if (!Double.isNaN(probability)) {
            if (probability > 1.0) {
                throwExpected(context, "Expected an probability <= 1.0");
            }
            generator.setProbability(probability);
        }
        if (!Double.isNaN(min)) {
            generator.setMin(min);
        }
        if (!Double.isNaN(max)) {
            generator.setMax(max);
        }

    }

    private void parseExpression(Context context) throws IOException {
        String leading = parseText(context, Token.Type.CLOSE_PAREN, Token.Type.OPEN_CURLY, Token.Type.OPEN_BRACKET,
                Token.Type.OPEN_PAREN, Token.Type.LESS_THAN);
        if (!leading.isEmpty()) {
            context.add(new TextGenerator(leading));
        }
        Token.Type token = context.nextToken();
        switch (token) {
        case EOF:
            return;
        case CLOSE_PAREN:
            context.pushToken();
            return;
        case OPEN_PAREN:
            parseBrace(context);
            break;
        case OPEN_CURLY:
            parseWords(context);
            break;
        case LESS_THAN:
            parseSet(context);
            break;
        default:
            throwExpected(context, "Expected ( { or < here");
            return;
        }
        String trailing = parseWhitespace(context);
        token = context.nextToken();
        switch (token) {
        case EOF:
            break;
        case VERTICALBAR:
            parseSeparatorAndFormat(context);
            trailing = parseWhitespace(context);
            token = context.nextToken();
            switch (token) {
            case EOF:
                return;
            case OPEN_BRACKET:
                parseParameters(context);
                break;
            default:
                context.pushToken();
                break;
            }
            break;
        case OPEN_BRACKET:
            parseParameters(context);
            break;
        default:
            context.pushToken();
            break;
        }
        if (!trailing.isEmpty()) {
            context.add(new TextGenerator(trailing));
        }
    }

    public CompiledTemplate compile(String template) throws IOException {
        Reader reader = new StringReader(template);
        Context context = new Context(reader);

        while (context.peekToken() != Token.Type.EOF) {
            parseExpression(context);
        }
        return context.pop();
    }

    /**
     * @return MacroProvider return the macroProvider
     */
    public MacroProvider getMacroProvider() {
        return macroProvider;
    }

    /**
     * @param macroProvider the macroProvider to set
     */
    public void setMacroProvider(MacroProvider macroProvider) {
        this.macroProvider = macroProvider;
    }

    /**
     * @param setProvider the setProvider to set
     */
    public void setSetProvider(SetProvider setProvider) {
        this.setProvider = setProvider;
    }

    public SetProvider getSetProvider() {
        return setProvider;
    }

    /**
     * @param setProvider the setProvider to set
     */
    public void setModifierProvider(ModifierProvider modifierProvider) {
        this.modifierProvider = modifierProvider;
    }

    public ModifierProvider getModifierProvider() {
        return modifierProvider;
    }

    /**
     * @return GeneratorProvider return the generatorProvider
     */
    public GeneratorProvider getGeneratorProvider() {
        return generatorProvider;
    }

    /**
     * @param generatorProvider the generatorProvider to set
     */
    public void setGeneratorProvider(GeneratorProvider generatorProvider) {
        this.generatorProvider = generatorProvider;
    }

}