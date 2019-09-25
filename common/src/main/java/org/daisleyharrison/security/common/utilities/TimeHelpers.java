package org.daisleyharrison.security.common.utilities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TimeHelpers {
    public static Date getTimeFromNow(int offset) {
        LocalDateTime dateTime = LocalDateTime.now().plus(Duration.of(offset, ChronoUnit.MINUTES));
        Date timeFromNow = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return timeFromNow;
    }
}