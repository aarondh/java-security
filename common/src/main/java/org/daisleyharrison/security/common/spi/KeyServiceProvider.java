package org.daisleyharrison.security.common.spi;

import org.daisleyharrison.security.common.models.key.CachedKeyProvider;

public interface KeyServiceProvider extends SecurityServiceProvider, CachedKeyProvider {
}