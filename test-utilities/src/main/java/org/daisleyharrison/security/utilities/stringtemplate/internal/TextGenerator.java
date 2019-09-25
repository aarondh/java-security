package org.daisleyharrison.security.utilities.stringtemplate.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.daisleyharrison.security.utilities.RandomHelper;

public class TextGenerator extends GeneratorBase {
    private String text;

    public TextGenerator(String text) {
        this.text = text;
    }

    @Override
    public String generateOne() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

}