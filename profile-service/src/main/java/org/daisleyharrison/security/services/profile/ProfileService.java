package org.daisleyharrison.security.services.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import javax.management.ServiceNotFoundException;

import org.daisleyharrison.security.common.models.PagedCollection;
import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.profile.Profile;
import org.daisleyharrison.security.common.models.profile.ProfileActivity;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.spi.ProfileServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.common.utilities.PagedCollectionImpl;
import org.daisleyharrison.security.common.utilities.StageImpl;
import org.daisleyharrison.security.data.models.Account;
import org.daisleyharrison.security.data.models.Activity;

public final class ProfileService implements ProfileServiceProvider {
    private static Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    public enum State {
        CREATED, INITIALIZING, INITIALIZED, COMPROMISED, CLOSED, ERROR
    }

    private State state;
    private DatastoreCollection<Account> accountCollection;
    private DatastoreCollection<Activity> activityCollection;

    @Override
    public boolean isInitialized() {
        return state != State.CREATED && state != State.CLOSED && state != State.INITIALIZING;
    }

    @Override
    public boolean isReady() {
        return state == State.INITIALIZED;
    }

    private void assertReady() {
        if (!isReady()) {
            throw new IllegalStateException("profile-service is in state " + state.toString() + " and is not ready");
        }
    }

    private void assertInitializing() {
        if (state != State.INITIALIZING) {
            throw new IllegalStateException(
                    "profile-service is in state " + state.toString() + " and is not initializing");
        }
    }

    public ProfileService() {
        this.state = State.CREATED;
    }

    public Stage beginInitialize() {
        if (this.state != State.CREATED && this.state != State.CLOSED) {
            throw new IllegalStateException(
                    "profile-service is in state " + state.toString() + " and cannot initalize");
        }
        this.state = State.INITIALIZING;
        return new StageImpl(() -> {
            if (this.state != State.INITIALIZING) {
                throw new IllegalStateException(
                        "profile-service is in state " + state.toString() + " and was not initializing");
            }
            if (this.accountCollection == null) {
                this.state = State.ERROR;
                throw new IllegalStateException("profile-service did not configure correctly.");
            }
            this.state = State.INITIALIZED;
        });
    }

    private DatastoreServiceProvider getDatastore() throws ServiceNotFoundException {
        return _serviceProvider.provideService(DatastoreServiceProvider.class);
    }

    private ConfigurationServiceProvider getConfig() throws ServiceNotFoundException {
        return _serviceProvider.provideService(ConfigurationServiceProvider.class);
    }

    @Override
    public void configure() throws Exception {
        assertInitializing();
        this.accountCollection = getDatastore().openCollection(Account.class, "id");
        this.activityCollection = getDatastore().openCollection(Activity.class, "id");
    }

    private Profile toProfile(Account account) {
        Profile profile = new Profile();
        profile.setId(account.getId());
        profile.setGivenName(account.getGivenName());
        profile.setMiddleName(account.getMiddleName());
        profile.setFamilyName(account.getFamilyName());
        profile.setPreferredUsername(account.getPreferredUsername());
        profile.setEmail(account.getEmail());
        profile.setPhoneNumber(account.getPhoneNumber());
        profile.setPictureUrl(account.getPictureUrl());
        profile.setLocked(account.isLocked());
        profile.setLockReason(account.getLockReason());
        profile.setCreated(account.getCreated());
        profile.setModified(account.getModified());
        profile.setModifiedBy(account.getModifiedBy());
        return profile;
    }

    private ProfileActivity toProfileActivity(Activity activity) {
        ProfileActivity profileActivity = new ProfileActivity();
        profileActivity.setId(activity.getId());
        profileActivity.setProfileId(activity.getAccountId());
        profileActivity.setActivityType(activity.getActivityType());
        profileActivity.setClient(activity.getClient());
        profileActivity.setCreated(activity.getCreated());
        return profileActivity;
    }

    private Account setAccount(Account account, Profile profile) {
        account.setGivenName(profile.getGivenName());
        account.setMiddleName(profile.getMiddleName());
        account.setFamilyName(profile.getFamilyName());
        account.setPreferredUsername(profile.getPreferredUsername());
        account.setEmail(profile.getEmail());
        account.setModified(profile.getModified());
        account.setModifiedBy(profile.getModifiedBy());
        return account;
    }

    @Override
    public Profile create(Profile profile, String modifiedBy) {
        assertReady();
        Account account = Account.newAccount(modifiedBy);
        setAccount(account, profile);
        accountCollection.insert(account);
        return toProfile(account);
    }

    @Override
    public Optional<Profile> read(String profileId) {
        assertReady();
        if (profileId == null || profileId.isEmpty()) {
            throw new IllegalArgumentException("profileId cannot be null or empty");
        }
        Account account = accountCollection.findById(profileId);
        if (account == null) {
            return Optional.empty();
        } else {
            return Optional.of(toProfile(account));
        }
    }

    @Override
    public Optional<Profile> readByEmail(String email) {
        assertReady();
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("email cannot be null or empty");
        }
        Query byEmail = accountCollection.buildQuery().root().property("email").is(email).build();
        Optional<Account> account = accountCollection.find(byEmail).findFirst();
        if (account.isEmpty()) {
            Query byIssuerEmail = accountCollection.buildQuery().root().node("issuer").property("email").is(email)
                    .build();
            account = accountCollection.find(byIssuerEmail).findFirst();
        }
        if (account.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toProfile(account.get()));
        }
    }

    @Override
    public Optional<Profile> update(Profile profile, String profileId) {
        assertReady();
        if (profileId == null || profileId.isEmpty()) {
            throw new IllegalArgumentException("profileId cannot be null or empty");
        }
        Account account = accountCollection.findById(profileId);
        if (account == null) {
            return Optional.empty();
        } else {
            account = setAccount(account, profile);
            accountCollection.save(account);
            return Optional.of(toProfile(account));
        }
    }

    @Override
    public Optional<Profile> delete(String profileId, String modifiedBy) {
        assertReady();
        if (profileId == null || profileId.isEmpty()) {
            throw new IllegalArgumentException("profileId cannot be null or empty");
        }
        Account account = accountCollection.findById(profileId);
        if (account != null) {
            if (accountCollection.remove(account)) {
                return Optional.of(toProfile(account));
            }
        }
        return Optional.empty();
    }

    @Override
    public PagedCollection<Profile> list(int pageSize, int pageNumber) {
        assertReady();

        Query all = accountCollection.buildQuery().root().property("id").isNot("").build();

        Stream<Account> accounts = accountCollection.find(all);

        long totalSize = accounts.count();

        PagedCollectionImpl<Profile> profiles = new PagedCollectionImpl<>(totalSize);

        accounts = accounts.skip(pageSize * pageNumber).limit(pageSize);

        accounts.forEach(account -> {
            profiles.add(toProfile(account));
        });
        return profiles;
    }

    @Override
    public PagedCollection<ProfileActivity> listActivity(String profileId, int pageNumber, int pageSize) {
        assertReady();

        Query all = activityCollection.buildQuery().root().property("accountId").is(profileId).build();

        Stream<Activity> activities = activityCollection.find(all);

        long totalSize = activities.count();

        activities = activityCollection.find(all);

        PagedCollectionImpl<ProfileActivity> pagedActivities = new PagedCollectionImpl<>(totalSize);

        activities = activities.sorted().skip(pageSize * pageNumber).limit(pageSize);

        activities.forEach(activity -> {
            pagedActivities.add(toProfileActivity(activity));
        });
        return pagedActivities;
    }

    @Override
    public void close() {
        this.state = State.CLOSED;
    }
}