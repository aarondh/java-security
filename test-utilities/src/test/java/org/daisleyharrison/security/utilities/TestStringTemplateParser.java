package org.daisleyharrison.security.utilities;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.daisleyharrison.security.utilities.stringtemplate.CompiledTemplate;
import org.daisleyharrison.security.utilities.stringtemplate.StringTemplateCompiler;

public class TestStringTemplateParser {

    @Test
    public void testParser() throws Exception {
        StringTemplateCompiler parser = new StringTemplateCompiler();
        parser.setSetProvider(name-> {
            return new String[]{"1", "2", "3"};
        });
        CompiledTemplate compiled = parser.compile(" ({ 1- }[0.5]\\({1 2 3 4 5 6 7 8 9}<digit>[2]\\)<digit>[3-3]-<digit>[4])[0.5]");
        compiled = parser.compile(" (<fubar>| *** |)[0.5] (<fablzort>| *** |format|)[0.3]");
    }

}