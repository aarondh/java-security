package org.daisleyharrison.security.services.authorization;

import org.junit.Test;
import org.daisleyharrison.security.common.spi.AuthorizationServiceProvider;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;

import static org.junit.Assert.assertFalse;

public class AuthorizationServiceTest {
    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    /**
     * Test to see that the AuthorizationService can be instantiated.
     */
    @Test
    public void testServiceInstantiation() throws Exception {
        AuthorizationServiceProvider authorizationService = _serviceProvider.provideService(AuthorizationServiceProvider.class);
        assertFalse(authorizationService == null);
    }
}
