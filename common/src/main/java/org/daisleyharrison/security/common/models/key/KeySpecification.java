package org.daisleyharrison.security.common.models.key;

import java.time.Duration;

public interface KeySpecification {
    public String getAlgorithm();
    public String getKeyPath();
    public char[] getPassword();
    public int getKeySize();
    public Duration getTTLDuration();
    public String getIssuer();
    public String getSubject();
}