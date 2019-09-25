package org.daisleyharrison.security.samples.jerseyService.utilities;

import java.io.IOException;

import com.github.jknack.handlebars.Options;

public class HandlebarsHelpers {
    public static Object matches(String regex, Options options) {
        String haystack = options.param(0);
        Options.Buffer buffer = options.buffer();
        try {
            if (haystack != null && haystack.matches(regex)) {
                buffer.append(options.fn());
            } else {
                buffer.append(options.inverse());
            }
            return buffer;
        } catch (IOException exception) {
            return null;
        }
    }
}