package org.daisleyharrison.security.services.cypher.internal;

import org.daisleyharrison.security.common.models.cypher.ConcealedString;
import javax.security.auth.DestroyFailedException;
import java.util.Arrays;

public final class ConcealedStringImpl implements ConcealedString {
    private byte[] data;
    private boolean internal;
    public ConcealedStringImpl(byte[] data, boolean internal) {
        this.data = data;
        this.internal = internal;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean isInternal() {
        return this.internal;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        Arrays.fill(this.data, (byte) 0);
        this.data = null;
    }

}
