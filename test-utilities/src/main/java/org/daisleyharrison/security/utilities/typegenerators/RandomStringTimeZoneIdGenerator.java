package org.daisleyharrison.security.utilities.typegenerators;

import java.util.TimeZone;

public class RandomStringTimeZoneIdGenerator extends RandomSetGenerator<String> {

    public RandomStringTimeZoneIdGenerator(String pattern) {
        super(pattern, TimeZone.getAvailableIDs());
    }

}
