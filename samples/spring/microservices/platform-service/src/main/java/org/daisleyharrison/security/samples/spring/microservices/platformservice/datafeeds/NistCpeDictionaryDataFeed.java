package org.daisleyharrison.security.samples.spring.microservices.platformservice.datafeeds;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;

import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.Datafeed;
import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.DatafeedMetaData;
import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.DatafeedMetaData.Status;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe.Platform;
import org.daisleyharrison.security.samples.spring.microservices.platformservice.models.cpe.Reference;
import org.daisleyharrison.security.samples.spring.microservices.shared.utilities.XmlHelper;

public class NistCpeDictionaryDataFeed implements Datafeed<Platform> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NistCpeDictionaryDataFeed.class);
    private DatafeedMetaData metaData;
    public NistCpeDictionaryDataFeed() {
        metaData = new DatafeedMetaData();
    }

    @Override
    public DatafeedMetaData getMetaData() {
        return metaData;
    }
    
    public void parse(InputStream inputStream, Action<Platform> action) throws IOException {
        metaData.setStatus(Status.PROCESSING);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream, "https://nvd.nist.gov");
            Element root = document.getDocumentElement();
            Element generatorElement = XmlHelper.firstChildOf(root, "generator");
            metaData.setFeedType(XmlHelper.textFromFirstChildOf(generatorElement, "product_name"));
            metaData.setFeedVersion(XmlHelper.textFromFirstChildOf(generatorElement, "product_version"));
            metaData.setFeedSchema(XmlHelper.textFromFirstChildOf(generatorElement, "schema_version"));
            metaData.setFeedTimestamp(XmlHelper.textFromFirstChildOf(generatorElement, "timestamp"));
            metaData.setFeedOrganization("NIST");


            Iterable<Element> cpeItems = XmlHelper.iterable(root, "cpe-item");

            for (Element platformElement : cpeItems) {

                Platform platform = new Platform();

                platform.setName(platformElement.getAttribute("name"));

                platform.setTitle(XmlHelper.textFromFirstChildOf(platformElement,"title"));

                platform.setCpe23ItemName(XmlHelper.textFromFirstChildOf(platformElement,"cpe23-item"));

                List<Reference> references = XmlHelper.stream(platformElement, "references", "reference")
                    .map(item->{
                        Reference reference = new Reference();
                        reference.setHref(item.getAttribute("href"));
                        reference.setValue(item.getTextContent());
                        return reference;
                    }).collect(Collectors.toList());
                platform.setReferences(references);

                metaData.incrementItems();

                if (!action.action(platform)) {
                    metaData.incrementProcessed();
                    metaData.setStatus(Status.ABORTED);
                    return;
                }

                metaData.incrementProcessed();

                if (metaData.getErrors() >= metaData.getMaxErrorsAllowed()) {
                    metaData.setStatus(Status.ERROR);
                    LOGGER.error("Maximum number of errors ({}) exceeding. Data feed processing terminated",
                            metaData.getMaxErrorsAllowed());
                    throw new IOException("Max errors exceeded");
                }
            }
            metaData.setStatus(Status.COMPLETE);
        } catch (ParserConfigurationException | SAXException exception) {
            metaData.setStatus(Status.ERROR);
            metaData.incrementErrors();
            throw new IOException(exception.getMessage(), exception);
        }
    }
}