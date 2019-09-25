package org.daisleyharrison.security.samples.spring.microservices.weaknessservice;

import org.daisleyharrison.security.samples.spring.microservices.weaknessservice.datafeeds.MitreCweDataFeed;
import org.daisleyharrison.security.samples.spring.microservices.weaknessservice.models.cwe.Weakness;
import org.daisleyharrison.security.common.exceptions.DatastoreException;
import org.daisleyharrison.security.common.models.datastore.DatastoreCollection;
import org.daisleyharrison.security.common.models.datastore.Query;
import org.daisleyharrison.security.common.spi.DatastoreServiceProvider;
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
    @Value("${weakness-service.cwe-datafeed.path}")
    private String cweDatafeedPath;

    @Value("${weakness-service.cwe-datafeed.insert}")
    private boolean cweDatafeedInsert;

    @Value("${weakness-service.cwe-datafeed.update}")
    private boolean cweDatafeedUpdate;

    @Value("${weakness-service.cwe-datafeed.skip}")
    private boolean cweDatafeedSkip;

    @Autowired
    private DatastoreServiceProvider datastoreService;

    private DatastoreCollection<Weakness> weaknessCollection;

    @Autowired
    public Bootstrapper() {
    }

    @PostConstruct
    public void init() {
        try {
            weaknessCollection = datastoreService.openCollection(Weakness.class, "name");
        } catch (DatastoreException ex) {
            LOGGER.error("Could not open the Weakness collection", ex);
        }
    }

    /**
     * Load the Common Weakness repository from the Mitre Corp. data feed
     */
    private void loadWeaknesses() throws IOException {
        Path cweDataFeedPathPath = Path.of(cweDatafeedPath);
        if (cweDatafeedSkip) {
            LOGGER.info("Skipping Mitre CWE data from {}", cweDatafeedPath);
            return; // Skipping
        }

        LOGGER.info("Loading Mitre CWE data from {}", cweDatafeedPath);

        if (weaknessCollection == null) {
            LOGGER.warn("Could not load data, weakness collection is missing");
        }

        MitreCweDataFeed dataFeed = new MitreCweDataFeed();

        DatafeedMetaData metaData = dataFeed.getMetaData();

        try (InputStream inputStream = Files.newInputStream(cweDataFeedPathPath)) {
            dataFeed.parse(inputStream, weakness -> {

                if (weakness == null) {
                    return true;
                }

                String name = weakness.getName();
                if (name == null) {
                    LOGGER.warn("Weakness has no name");
                    metaData.incrementErrors();
                    return true;
                }
                Query query = weaknessCollection.buildQuery().root().property("name").is(name).build();
                if (!cweDatafeedUpdate || this.weaknessCollection.find(query).findFirst().isEmpty()) {
                    if (cweDatafeedInsert) {
                        this.weaknessCollection.insert(weakness);
                        metaData.incrementInserts();
                    }
                } else if (cweDatafeedUpdate) {
                    this.weaknessCollection.save(weakness);
                    metaData.incrementUpdates();
                }

                return true;
            });
        }

        LOGGER.info("loaded {} new weaknesses, {} weaknesses updated, from {} total weaknesses in feed",
                metaData.getInserts(), metaData.getUpdates(), metaData.getItems(), metaData.getFeedTimestamp());
    }

    @Override
    public void run(String... strings) throws Exception {

        loadWeaknesses();
    }
}