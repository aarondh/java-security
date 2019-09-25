package org.daisleyharrison.security.utilities.stringtemplate.internal;

import java.util.List;
import java.util.stream.Collectors;
import java.util.LinkedList;

import org.daisleyharrison.security.utilities.RandomHelper;
import org.daisleyharrison.security.utilities.stringtemplate.Generator;

/**
 * Generates a string from a set of string
 */
public class GeneratorGenerator extends GeneratorBase {
    private String name;
    private Generator generator;

    public GeneratorGenerator(String name, Generator generator) {
        this.generator = generator;
    }

    @Override
    public Object generateOne() {
        return generator.generate();
    }

    @Override
    public String toString() {
        return "<" + this.name + "> " + super.toString();
    }
}
