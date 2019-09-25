package org.daisleyharrison.security.utilities.typegenerators;

import java.util.Date;
import java.util.Calendar;

import org.daisleyharrison.security.utilities.TypeGenerator;
import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomDateGenerator implements TypeGenerator<Date> {
    private static RandomHelper RANDOM;
    public static final Date MIN_DATE = new Date(0);
    private static final Calendar MAX_CAL = Calendar.getInstance();
    static {
        MAX_CAL.set(9999, 12, 31, 23, 59, 59);
    }
    public static final Date MAX_DATE = MAX_CAL.getTime();
    private String pattern;
    private Date min;
    private Date max;

    public RandomDateGenerator(String pattern, Date min, Date max) {
        this.pattern = pattern;
        if (min == null) {
            throw new IllegalArgumentException("min date cannot be null");
        }
        if (max == null) {
            throw new IllegalArgumentException("max date cannot be null");
        }
        this.min = min;
        this.max = max;
    }

    public RandomDateGenerator(String pattern) {
        this.pattern = pattern;
        this.min = MIN_DATE;
        this.max = MAX_DATE;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Date apply(Class<Date> type, String name) throws Exception {
        return new Date(RANDOM.nextLong(min.getTime(), max.getTime()));
    }
}