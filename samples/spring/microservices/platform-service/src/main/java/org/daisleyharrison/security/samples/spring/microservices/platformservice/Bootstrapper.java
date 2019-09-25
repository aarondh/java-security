package org.daisleyharrison.security.samples.spring.microservices.platformservice;

import org.daisleyharrison.security.common.exceptions.DatastoreException;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.datafeeds.NistCpeDictionaryDataFeed;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe.Platform;
import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.DatafeedMetaData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;

@Component
public class Bootstrapper implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrapper.class);
    @Value("${platform-service.cpe-datafeed.path}")
    private String cpeDatafeedPath;

    @Value("${platform-service.cpe-datafeed.insert}")
    private boolean cpeDatafeedInsert;

    @Value("${platform-service.cpe-datafeed.update}")
    private boolean cpeDatafeedUpdate;

    @Value("${platform-service.cpe-datafeed.skip}")
    private boolean cpeDatafeedSkip;

    @Autowired
    private DatastoreServiceProvider dataService;

    private DatastoreCollection<Platform> platformCollection;
    @Autowired
    public Bootstrapper() {
    }
    @PostConstruct
    public void init(){
        try {
        platformCollection = dataService.openCollection(Platform.class, "name");
        }
        catch(DatastoreException ex){
            LOGGER.error("Failed to open Platform collection", ex);
        }
    }
    /**
     * Load the Platforms repository from the NIST CPE dictionary data feed
     */
    private void loadPlatforms() throws IOException {
        Path cpeDataFeedPathPath = Path.of(cpeDatafeedPath);

        LOGGER.info("Loading NIST CPE dictionary data from {}", cpeDatafeedPath);

        if(platformCollection==null){
            LOGGER.info("Cannot loaded, platformCollection not available");
        }

        NistCpeDictionaryDataFeed datafeed = new NistCpeDictionaryDataFeed();
        DatafeedMetaData metaData = datafeed.getMetaData();

        try (InputStream inputStream = Files.newInputStream(cpeDataFeedPathPath)) {
            datafeed.parse(inputStream, platform -> {

                if (metaData.getProcessed() == 0) {
                    LOGGER.info("NIST CPE data dictionary feed: type {}, version {}, schema  {}, date {}, count {}",
                            metaData.getFeedType(), metaData.getFeedVerison(), metaData.getFeedSchema(),
                            metaData.getFeedTimestamp(), metaData.getItems());
                }

                if (cpeDatafeedSkip) {
                    LOGGER.info("Skipping NIST CPE dictionary feed");
                    return false; // Skipping
                }

                if (platform == null) {
                    return true;
                }

                String name = platform.getName();
                if (name == null) {
                    LOGGER.warn("Platform #{} name is null", metaData.getProcessed());
                    metaData.incrementErrors();
                    return true;
                }
                Query query = this.platformCollection.buildQuery().root().property("name").is(name).build();

                
                if (!cpeDatafeedUpdate || this.platformCollection.find(query).findFirst().isEmpty()) {
                    if (cpeDatafeedInsert) {
                        this.platformCollection.insert(platform);
                        metaData.incrementInserts();
                    }
                } else if (cpeDatafeedUpdate) {
                    this.platformCollection.save(platform);
                    metaData.incrementUpdates();
                }

                return true;

            });
        }

        LOGGER.info("loaded {} new platforms, {} platforms updated, from {} total platforms in feed from {}",
                metaData.getInserts(), metaData.getUpdates(),  metaData.getItems(), metaData.getFeedTimestamp());
    }


    @Override
    public void run(String... strings) throws Exception {

        loadPlatforms();

    }
}