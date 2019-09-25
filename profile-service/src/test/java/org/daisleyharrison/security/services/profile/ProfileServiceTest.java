package org.daisleyharrison.security.services.profile;
import java.util.ServiceLoader;
import java.util.Optional;
import org.junit.Test;
import org.daisleyharrison.security.common.spi.ProfileServiceProvider;
import static org.junit.Assert.assertFalse;

public class ProfileServiceTest {

    /**
     * Test to see that the PorfileServiceProvider can be instantiated.
     */
    @Test
    public void testServiceInstantiation() {
        ServiceLoader<ProfileServiceProvider> loader = ServiceLoader.load(ProfileServiceProvider.class);
        Optional<ProfileServiceProvider> profileServiceProvider = loader.findFirst();
        assertFalse(profileServiceProvider == null);
        ProfileServiceProvider profileService = profileServiceProvider.get();
        assertFalse(profileService == null);
    }
}
