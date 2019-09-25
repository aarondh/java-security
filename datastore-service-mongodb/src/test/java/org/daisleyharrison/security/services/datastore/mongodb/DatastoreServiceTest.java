package org.daisleyharrison.security.services.datastore.mongodb;

import org.daisleyharrison.security.common.models.Stage;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.DatastoreCursor;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.serviceProvider.LibraryServiceProvider;
import org.daisleyharrison.security.common.spi.ConfigurationServiceProvider;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.utilities.RandomTypeGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class DatastoreServiceTest {

    private static final RandomTypeGenerator typeGenerator = new RandomTypeGenerator();

    private static LibraryServiceProvider _serviceProvider = LibraryServiceProvider.getInstance();

    private ConfigurationServiceProvider configurationService;

    private DatastoreServiceProvider datastoreService;

    @Before
    public void setUp() throws Exception {
        // start the service
        configurationService = _serviceProvider.provideService(ConfigurationServiceProvider.class);
    }

    private String locatePath(String path, String component) {
        if (currentDirectory().contains(component)) {
            return "." + path;
        } else {
            return path;
        }
    }

    private String currentDirectory() {
        return new File(".").getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void initializeConfigurationService() throws Exception {
        if (!configurationService.isInitialized()) {
            File configFile = new File(
                    locatePath("./datastore-service-mongodb/src/test/resources/testconfig.yaml", "datastore-service-mongodb"));
            System.out.println("using config file: " + configFile.getAbsolutePath());
            try (InputStream inputStream = new FileInputStream(configFile)) {
                try (Stage stage = configurationService.beginInitialize()) {
                    configurationService.setSource(inputStream);
                    configurationService.configure();
                }
            }
        }
    }

    @Test
    public void initializeDatastoreService() throws Exception {
        initializeConfigurationService();
        datastoreService = _serviceProvider.provideService(DatastoreServiceProvider.class);
        if (!datastoreService.isInitialized()) {
            try (Stage stage = datastoreService.beginInitialize()) {
                datastoreService.configure();
            }
        }
    }

    /**
     * test retrieval of salt generation
     */
    @Test
    public void testCreate() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User testUser1 = typeGenerator.generate(User.class);
        users.insert(testUser1);
        User testUser2 = typeGenerator.generate(User.class);
        users.insert(testUser2);
    }
    @Test
    public void testFindById() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        User actual = users.findById(expected.get_id());
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
    }
    @Test
    public void testQueryOne() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        Query query = users.buildQuery().root().property("family_name").is(expected.getFamily_name()).build();
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryInvalidField() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        users.buildQuery().root().property("not_a_valid_field").is("fubar").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryInvalidNode() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        users.buildQuery().node("not_a_valid_node").property("id").is("fubar").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryInvalidValue() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        users.buildQuery().root().property("id").is(42).build();
    }

    @Test
    public void testQueryOneDate() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("created_at").is(expected.getCreated_at())
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.getId(), actual.get().getId());
    }
    
    @Test
    public void testQueryTwo() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("family_name").is(expected.getFamily_name())
                           .and("given_name").is(expected.getGiven_name())
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.getFamily_name(), actual.get().getFamily_name());
        assertEquals(expected.getGiven_name(), actual.get().getGiven_name());
    }
    @Test
    public void testQueryNotTwo() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("family_name").is(expected.getFamily_name())
                           .and("given_name").isNot(expected.getGiven_name())
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertFalse(actual.isPresent());
    }
    @Test
    public void testQueryAddressNodeOne() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .node("address")
                           .property("street_address").is(expected.getAddress().getStreet_address())
                           .end()
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertFalse(actual.isPresent());
    }
    @Test
    public void testQueryOr() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("id").is("fubar")
                           .or()
                           .property("id").is(expected.getId()).end()
                           .or("id").is("pickles")
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.get_id(), actual.get().get_id());
    }
    @Test
    public void testQueryAndSimple() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("given_name").is(expected.getGiven_name())
                           .and("id").is(expected.getId())
                           .and("family_name").is(expected.getFamily_name())
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.get_id(), actual.get().get_id());
    }
    @Test
    public void testQueryAnd() throws Exception  {
        initializeDatastoreService() ;
        DatastoreCollection<User> users = datastoreService.openCollection(User.class, "id");
        User expected = typeGenerator.generate(User.class);
        users.insert(expected);
        //@formatter:off
        Query query = users.buildQuery()
                           .root()
                           .property("given_name").is(expected.getGiven_name())
                           .and()
                           .property("id").is(expected.getId()).end()
                           .and("family_name").is(expected.getFamily_name())
                           .build();
        //@formatter:oon
        DatastoreCursor<User> cursor = users.find(query);
        Optional<User> actual = cursor.findFirst();
        assertTrue(actual.isPresent());
        assertEquals(expected.get_id(), actual.get().get_id());
    }
}