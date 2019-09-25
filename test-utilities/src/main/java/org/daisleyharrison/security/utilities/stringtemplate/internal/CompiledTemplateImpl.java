package org.daisleyharrison.security.utilities.stringtemplate.internal;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.daisleyharrison.security.utilities.stringtemplate.CompiledTemplate;

public class CompiledTemplateImpl extends GeneratorBase implements CompiledTemplate {
    private LinkedList<GeneratorBase> generators;

    public CompiledTemplateImpl() {
        this.generators = new LinkedList<>();
    }

    public void add(GeneratorBase generator) {
        generators.addLast(generator);
    }

    public GeneratorBase getLast() {
        return generators.getLast();
    }

    @Override
    public String generateOne() {
        return generators.stream().map(gen->gen.generate()).map(Object::toString).collect(Collectors.joining(getSeparator()));
    }
}