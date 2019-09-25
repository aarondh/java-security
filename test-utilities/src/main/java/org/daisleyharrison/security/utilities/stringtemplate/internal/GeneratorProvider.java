package org.daisleyharrison.security.utilities.stringtemplate.internal;

import org.daisleyharrison.security.utilities.stringtemplate.Generator;

public interface GeneratorProvider {
    public Generator resolveGenerator(String generatorName);
}