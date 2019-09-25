package org.daisleyharrison.security.common.spi;

import java.util.Optional;

import org.daisleyharrison.security.common.models.PagedCollection;
import org.daisleyharrison.security.common.models.profile.Profile;
import org.daisleyharrison.security.common.models.profile.ProfileActivity;

public interface ProfileServiceProvider extends SecurityServiceProvider {
    public Profile create(Profile profile, String modifiedBy);

    public Optional<Profile> read(String profileId);

    public Optional<Profile> update(Profile profile, String modifiedBy);

    public Optional<Profile> delete(String profileId, String modifiedBy);

    public PagedCollection<Profile> list(int pageSize, int pageNumber);

    public PagedCollection<ProfileActivity> listActivity(String profileId, int pageNumber, int pageSize);

    public Optional<Profile> readByEmail(String profileId);
}