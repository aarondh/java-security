package org.daisleyharrison.security.samples.spring.microservices.weaknessservice.datafeeds;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.*;
import java.io.*;

import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.DatafeedMetaData;
import org.daisleyharrison.security.samples.spring.microservices.shared.datafeed.Datafeed;
import org.daisleyharrison.security.samples.spring.microservices.weaknessservice.models.cwe.Weakness;
import org.daisleyharrison.security.samples.spring.microservices.shared.utilities.EnumConverter;
import org.daisleyharrison.security.samples.spring.microservices.shared.utilities.XmlHelper;

public class MitreCweDataFeed implements Datafeed<Weakness>, ErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(MitreCweDataFeed.class);
    private DatafeedMetaData metaData;

    public MitreCweDataFeed() {
        metaData = new DatafeedMetaData();
    }

    @Override
    public DatafeedMetaData getMetaData() {
        return metaData;
    }

    @Override
    public void parse(InputStream inputStream, Action<Weakness> action) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(this);
            Document document = builder.parse(inputStream, "https://cwe.mitre.org");
            Element root = document.getDocumentElement();

            metaData.setFeedOrganization("Mitre");
            metaData.setFeedType(root.getAttribute("Name"));
            metaData.setFeedVersion(root.getAttribute("Version"));
            metaData.setFeedSchema(root.getNamespaceURI());
            metaData.setStatus(DatafeedMetaData.Status.PROCESSING);

            EnumConverter<Weakness.Abstraction> abstractionConverter = new EnumConverter<>(Weakness.Abstraction.class);
            EnumConverter<Weakness.Nature> natureConverter = new EnumConverter<>(Weakness.Nature.class);
            EnumConverter<Weakness.Ordinal> ordinalConverter = new EnumConverter<>(Weakness.Ordinal.class);
            EnumConverter<Weakness.Likelihood> likelihoodConverter = new EnumConverter<>(Weakness.Likelihood.class);
            EnumConverter<Weakness.Effectiveness> effectivenessConverter = new EnumConverter<>(
                    Weakness.Effectiveness.class);
            EnumConverter<Weakness.Phase> phaseConverter = new EnumConverter<>(Weakness.Phase.class);
            EnumConverter<Weakness.Prevalence> prevalenceConverter = new EnumConverter<>(Weakness.Prevalence.class);
            EnumConverter<Weakness.NoteType> noteTypeConverter = new EnumConverter<>(Weakness.NoteType.class);

            Iterable<Element> weaknessElements = XmlHelper.iterable(root, "Weaknesses", "Weakness");

            for (Element weaknessElement : weaknessElements) {
                try {
                    final Weakness weakness = new Weakness();
                    weakness.setId(weaknessElement.getAttribute("ID"));
                    weakness.setName(weaknessElement.getAttribute("Name"));
                    weakness.setAbstraction(abstractionConverter.valueOf(weaknessElement.getAttribute("Abstraction")));
                    weakness.setDescription(XmlHelper.textFromFirstChildOf(weaknessElement, "Description"));
                    weakness.setExtendedDescription(
                            XmlHelper.textFromFirstChildOf(weaknessElement, "Extended_Description"));

                    final List<Weakness.RelatedWeakness> relatedWeaknesses = XmlHelper
                            .stream(weaknessElement, "Related_Weaknesses", "Related_Weakness").map(related -> {
                                Weakness.RelatedWeakness relatedWeakness = new Weakness.RelatedWeakness();
                                relatedWeakness.setNature(natureConverter.valueOf(related.getAttribute("Nature")));
                                relatedWeakness.setCweId(related.getAttribute("CWE_ID"));
                                relatedWeakness.setViewId(related.getAttribute("View_ID"));
                                relatedWeakness.setOrdinal(ordinalConverter.valueOf(related.getAttribute("Ordinal")));
                                return relatedWeakness;
                            }).collect(Collectors.toList());
                    weakness.setRelatedWeaknesses(relatedWeaknesses);

                    final List<Weakness.ApplicablePlatform> applicablePlatforms = XmlHelper
                            .stream(weaknessElement, "Applicable_Platforms", "*").map(item -> {
                                String tagName = item.getTagName();
                                if (tagName.equals("Language")) {
                                    Weakness.Language language = new Weakness.Language();
                                    language.setClassName(item.getAttribute("Class"));
                                    language.setName(item.getAttribute("Name"));
                                    language.setPrevalence(
                                            prevalenceConverter.valueOf(item.getAttribute("Prevalence")));
                                    return language;
                                } else if (tagName.equals("Paradigm")) {
                                    Weakness.Paradigm paradigm = new Weakness.Paradigm();
                                    paradigm.setClassName(item.getAttribute("Class"));
                                    paradigm.setName(item.getAttribute("Name"));
                                    paradigm.setPrevalence(
                                            prevalenceConverter.valueOf(item.getAttribute("Prevalence")));
                                    return paradigm;

                                } else if (tagName.equals("Technology")) {
                                    Weakness.Technology technology = new Weakness.Technology();
                                    technology.setClassName(item.getAttribute("Class"));
                                    technology.setName(item.getAttribute("Name"));
                                    technology.setPrevalence(
                                            prevalenceConverter.valueOf(item.getAttribute("Prevalence")));
                                    return technology;
                                } else {
                                    LOGGER.warn("Unsupported Applicable_Platforms element {}", tagName);
                                    return new Weakness.ApplicablePlatform();
                                }
                            }).collect(Collectors.toList());
                    weakness.setApplicablePlatforms(applicablePlatforms);

                    weakness.setBackgroundDetails(XmlHelper.textForEachChildOfChild(weaknessElement,
                            "Background_Details", "Background_Detail"));

                    List<Weakness.Introduction> modesOfIntroductions = XmlHelper
                            .stream(weaknessElement, "Modes_Of_Introduction", "Introduction").map(item -> {
                                Weakness.Introduction introduction = new Weakness.Introduction();
                                introduction.setPhase(
                                        phaseConverter.valueOf(XmlHelper.textFromFirstChildOf(item, "Phase")));
                                introduction.setNote(XmlHelper.textFromFirstChildOf(item, "Note"));
                                return introduction;
                            }).collect(Collectors.toList());
                    weakness.setModesOfIntroduction(modesOfIntroductions);

                    weakness.setLikelihoodOfExploit(likelihoodConverter
                            .valueOf(XmlHelper.textFromFirstChildOf(weaknessElement, "Likelihood_Of_Exploit")));

                    List<Weakness.Consequence> commonConsequences = XmlHelper
                            .stream(weaknessElement, "Common_Consequences", "Consequence").map(item -> {
                                Weakness.Consequence consequence = new Weakness.Consequence();
                                consequence.setImpact(XmlHelper.textForEachChildOf(item, "Impact"));
                                consequence.setScope(XmlHelper.textForEachChildOf(item, "Scope"));
                                consequence.setNote(XmlHelper.textFromFirstChildOf(item, "Note"));
                                return consequence;
                            }).collect(Collectors.toList());
                    weakness.setCommonConsequences(commonConsequences);

                    List<Weakness.DetectionMethod> detectionMethods = XmlHelper
                            .stream(weaknessElement, "Detection_Methods", "Detection_Method").map(item -> {
                                Weakness.DetectionMethod detectionMethod = new Weakness.DetectionMethod();
                                detectionMethod.setMethod(XmlHelper.textFromFirstChildOf(item, "Method"));
                                detectionMethod.setDescription(XmlHelper.htmlFromFirstChildOf(item, "Description"));
                                detectionMethod.setEffectiveness(effectivenessConverter
                                        .valueOf(XmlHelper.textFromFirstChildOf(item, "Effectiveness")));
                                return detectionMethod;
                            }).collect(Collectors.toList());
                    weakness.setDetectionMethods(detectionMethods);

                    List<Weakness.Mitigation> potentialMitigations = XmlHelper
                            .stream(weaknessElement, "Potential_Mitigations", "Mitigation").map(item -> {
                                Weakness.Mitigation mitigation = new Weakness.Mitigation();
                                mitigation.setPhase(
                                        phaseConverter.valueOf(XmlHelper.textFromFirstChildOf(item, "Phase")));
                                mitigation.setDescription(XmlHelper.htmlFromFirstChildOf(item, "Description"));
                                mitigation.setEffectiveness(effectivenessConverter
                                        .valueOf(XmlHelper.textFromFirstChildOf(item, "Effectiveness")));
                                return mitigation;
                            }).collect(Collectors.toList());
                    weakness.setPotentialMitigations(potentialMitigations);

                    List<Weakness.DemonstrativeExample> demonstrativeExamples = XmlHelper
                            .stream(weaknessElement, "Demonstrative_Examples", "Demonstrative_Example").map(item -> {
                                Weakness.DemonstrativeExample demonstrativeExample = new Weakness.DemonstrativeExample();
                                List<Weakness.Example> examples = XmlHelper.stream(item, "*").map(item2 -> {
                                    String tagName = item.getLocalName();
                                    if (tagName.equals("Intro_Text")) {
                                        return new Weakness.IntroText(item2.getTextContent());
                                    } else if (tagName.equals("Body_Text")) {
                                        Weakness.BodyText bodyText = new Weakness.BodyText(XmlHelper.htmlOf(item2));
                                        return bodyText;
                                    } else if (tagName.equals("Example_Code")) {
                                        Weakness.CodeExample exampleCode = new Weakness.CodeExample();
                                        exampleCode.setNature(natureConverter.valueOf(item2.getAttribute("Nature")));
                                        exampleCode.setLanguage(item2.getAttribute("Language"));
                                        exampleCode.setText(XmlHelper.htmlOf(item2));
                                        return exampleCode;
                                    } else {
                                        LOGGER.warn("Unsupported tag inside Demonstrative_Example {}", tagName);
                                        return new Weakness.IntroText();
                                    }
                                }).collect(Collectors.toList());
                                demonstrativeExample.setExample(examples);
                                return demonstrativeExample;
                            }).collect(Collectors.toList());
                    weakness.setDemonstrativeExamples(demonstrativeExamples);

                    List<Weakness.Reference> references = XmlHelper.stream(weaknessElement, "References", "Reference")
                            .map(item -> {
                                Weakness.Reference reference = new Weakness.Reference();
                                reference.setExternalReferenceId(item.getAttribute("External_Reference_ID"));
                                reference.setSection(item.getAttribute("Section"));
                                return reference;
                            }).collect(Collectors.toList());
                    weakness.setReferences(references);

                    List<Weakness.ObservedExample> observedExamples = XmlHelper
                            .stream(weaknessElement, "Observed_Examples", "Observed_Example").map(item -> {
                                Weakness.ObservedExample observedExample = new Weakness.ObservedExample();
                                observedExample.setReference(XmlHelper.textFromFirstChildOf(item, "Reference"));
                                observedExample.setDescription(XmlHelper.textFromFirstChildOf(item, "Description"));
                                observedExample.setLink(XmlHelper.textFromFirstChildOf(item, "Link"));
                                return observedExample;
                            }).collect(Collectors.toList());
                    weakness.setObservedExamples(observedExamples);

                    List<Weakness.Note> notes = XmlHelper.stream(weaknessElement, "Notes", "Note").map(item -> {
                        Weakness.Note note = new Weakness.Note();
                        note.setType(noteTypeConverter.valueOf(item.getAttribute("Type")));
                        note.setValue(XmlHelper.htmlOf(item));
                        return note;
                    }).collect(Collectors.toList());
                    weakness.setNotes(notes);

                    List<Weakness.TaxonomyMapping> taxonomyMappings = XmlHelper
                            .stream(weaknessElement, "Taxonomy_Mappings", "Taxonomy_Mapping").map(item -> {
                                Weakness.TaxonomyMapping taxonomyMapping = new Weakness.TaxonomyMapping();
                                taxonomyMapping.setTaxonomyName(item.getAttribute("Taxonomy_Name"));
                                taxonomyMapping.setId(XmlHelper.textFromFirstChildOf(item, "Entry_ID"));
                                taxonomyMapping.setName(XmlHelper.textFromFirstChildOf(item, "Entry_Name"));
                                return taxonomyMapping;
                            }).collect(Collectors.toList());
                    weakness.setTaxonomyMappings(taxonomyMappings);

                    Element contentHistoryElement = XmlHelper.firstChildOf(weaknessElement, "Content_History");
                    if (contentHistoryElement != null) {
                        final Weakness.ContentHistory contentHistory = new Weakness.ContentHistory();
                        Element submissionElement = XmlHelper.firstChildOf(contentHistoryElement, "Submission");
                        if (submissionElement != null) {
                            Weakness.Submission submission = new Weakness.Submission();
                            submission.setName(XmlHelper.textFromFirstChildOf(submissionElement, "Submission_Name"));
                            submission.setOrganization(
                                    XmlHelper.textFromFirstChildOf(submissionElement, "Submission_Organization"));
                            submission.setDate(XmlHelper.textFromFirstChildOf(submissionElement, "Submission_Date"));
                            contentHistory.setSubmission(submission);
                        }
                        List<Weakness.Modification> modifications = XmlHelper
                                .stream(contentHistoryElement, "Modification").map(item -> {
                                    Weakness.Modification modification = new Weakness.Modification();
                                    modification.setName(XmlHelper.textFromFirstChildOf(item, "Modification_Name"));
                                    modification.setOrganization(
                                            XmlHelper.textFromFirstChildOf(item, "Modification_Organization"));
                                    modification.setDate(XmlHelper.textFromFirstChildOf(item, "Modification_Date"));
                                    modification
                                            .setComment(XmlHelper.textFromFirstChildOf(item, "Modification_Comment"));
                                    return modification;
                                }).collect(Collectors.toList());
                        contentHistory.setModifications(modifications);
                        weakness.setContentHistory(contentHistory);
                    }

                    metaData.incrementItems();

                    if (!action.action(weakness)) {
                        metaData.incrementProcessed();
                        metaData.setStatus(DatafeedMetaData.Status.ABORTED);
                        return;
                    }

                    metaData.incrementProcessed();

                } catch (Exception exception) {
                    metaData.incrementErrors();
                    LOGGER.error("Error processing weakness #{} ID=\"{}\": {}", metaData.getItems(),
                            weaknessElement.getAttribute("ID"), exception.getMessage());
                }
                
                if (metaData.getErrors() >= metaData.getMaxErrorsAllowed()) {
                    LOGGER.error("Maximum number of errors ({}) exceeding. Data feed processing terminated",
                            metaData.getMaxErrorsAllowed());
                    metaData.setStatus(DatafeedMetaData.Status.ERROR);
                    throw new IOException("Maximum number of errors exceeding");
                }

            } // while weaknessElement != null

            metaData.setStatus(DatafeedMetaData.Status.COMPLETE);

        } catch (ParserConfigurationException | SAXException exception) {
            metaData.incrementErrors();
            metaData.setStatus(DatafeedMetaData.Status.ERROR);
            throw new IOException(exception.getMessage(), exception);
        }
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        LOGGER.warn("XMl Parser warning at line {}", exception.getLineNumber(), exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        LOGGER.error("XMl Parser error at line {}", exception.getLineNumber(), exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        LOGGER.error("XMl Parser fatal error at line {}", exception.getLineNumber(), exception.getMessage());
    }
}