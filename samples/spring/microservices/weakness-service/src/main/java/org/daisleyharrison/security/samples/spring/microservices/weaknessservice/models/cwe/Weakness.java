package org.daisleyharrison.security.samples.spring.microservices.weaknessservice.models.cwe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "weaknesses", schemaVersion = "1.0")
public class Weakness {
    public enum Abstraction {
        @JsonProperty("Variant")
        VARIANT,
        @JsonProperty("Base")
        BASE,
        @JsonProperty("Class")
        CLASS,
        @JsonProperty("Compound")
        COMPOUND
    }

    public enum Structure {
        @JsonProperty("Simple")
        SIMPLE, @JsonProperty("Base")
        BASE
    }

    public enum Status {
        @JsonProperty("Simple")
        DRAFT
    }

    public enum Nature {
        @JsonProperty("ChildOf")
        CHILD_OF, @JsonProperty("PeerOf")
        PEER_OF, @JsonProperty("CanPrecede")
        CAN_PRECEDE, @JsonProperty("good")
        GOOD, @JsonProperty("bad")
        BAD, @JsonProperty("attack")
        ATTACK, @JsonProperty("result")
        RESULT, @JsonProperty("CanAlsoBe")
        CAN_ALSO_BE, @JsonProperty("Requires")
        REQUIRES
    }

    public enum Ordinal {
        @JsonProperty("Primary")
        PRIMARY, @JsonProperty("Indirect")
        INDIRECT, @JsonProperty("Resultant")
        RESULTANT
    }

    public enum Scope {
        @JsonProperty("Other")
        OTHER, @JsonProperty("Confidentiality")
        CONFIFENTIALITY, @JsonProperty("Availability")
        AVAILABILITY, @JsonProperty("Integrity")
        INTEGRITY, @JsonProperty("Access Control")
        ACCESS_CONTROL

    }

    public enum Impact {
        @JsonProperty("Other")
        OTHER
    }

    public enum Likelihood {
        @JsonProperty("Undetermined")
        UNDETERMINED, @JsonProperty("Low")
        LOW, @JsonProperty("Medium")
        MEDIUM, @JsonProperty("High")
        HIGH
    }

    public enum Phase {
        @JsonProperty("Other")
        OTHER, @JsonProperty("Architecture and Design")
        ARCHITECTURE_AND_DESIGN, @JsonProperty("Implementation")
        IMPLEMENTATION, @JsonProperty("Operation")
        OPERATION, @JsonProperty("System Configuration")
        SYSTEM_CONFIGURATION, @JsonProperty("Testing")
        TESTING, @JsonProperty("Requirements")
        REQUIREMENTS, @JsonProperty("Build and Compilation")
        BUILD_AND_COMPILATION, @JsonProperty("Installation")
        INSTALLATION, @JsonProperty("Distribution")
        DISTRIBUTION, @JsonProperty("Policy")
        POLICY, @JsonProperty("Documentation")
        DOCUMENTATION
    }

    public enum NoteType {
        @JsonProperty("Other")
        OTHER, @JsonProperty("Relationship")
        RELATIONSHIP, @JsonProperty("Terminology")
        TERMINOLOGY, @JsonProperty("Maintenance")
        MAINTENANCE, @JsonProperty("Theoretical")
        THEORETICAL, @JsonProperty("Research")
        RESEARCH, @JsonProperty("Research Gap")
        RESEARCH_GAP, @JsonProperty("Applicable Platform")
        APPLICABLE_PLATFORM
    }

    public enum Prevalence {
        @JsonProperty("Other")
        OTHER, @JsonProperty("Other")
        UNDETERMINED, @JsonProperty("Other")
        RARELY
    }

    public enum Effectiveness {
        @JsonProperty("Undetermined")
        UNDETERMINED, @JsonProperty("Opportunistic")
        OPPORTUNISTIC, @JsonProperty("Limited")
        LIMITED, @JsonProperty("Moderate")
        MODERATE, @JsonProperty("High")
        HIGH, @JsonProperty("SOAR Partial")
        SOAR_PARTIAL, @JsonProperty("Defense in Depth")
        DEFENCE_IN_DEPTH, @JsonProperty("None")
        NONE
    }

    public static class RelatedWeakness {
        private Nature nature;
        private String cweId;
        private String viewId;
        private Ordinal ordinal;

        public RelatedWeakness() {
        }

        public Nature getNature() {
            return nature;
        }

        public Ordinal getOrdinal() {
            return ordinal;
        }

        public void setOrdinal(Ordinal ordinal) {
            this.ordinal = ordinal;
        }

        public String getViewId() {
            return viewId;
        }

        public void setViewId(String viewId) {
            this.viewId = viewId;
        }

        public String getCweId() {
            return cweId;
        }

        public void setCweId(String cweId) {
            this.cweId = cweId;
        }

        public void setNature(Nature nature) {
            this.nature = nature;
        }
    }

    public static class WeaknessOrdinality {
        private Ordinal ordinality;

        public Ordinal getOrdinality() {
            return ordinality;
        }

        public void setOrdinality(Ordinal ordinality) {
            this.ordinality = ordinality;
        }
    }

    public static class Introduction {
        private Phase phase;
        private String note;

        public Introduction() {
        }

        public Phase getPhase() {
            return phase;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public void setPhase(Phase phase) {
            this.phase = phase;
        }
    }

    public static class ApplicablePlatform {
        @JsonProperty("type")
        private String type;
        @JsonProperty("class")
        private String className;
        @JsonProperty("prevalence")
        private Prevalence prevalence;

        private String name;

        public ApplicablePlatform(String type) {
            this.type = type;
        }

        public ApplicablePlatform() {
            this.type = "Undefined";
        }

        public Prevalence getPrevalence() {
            return prevalence;
        }

        public void setPrevalence(Prevalence prevalence) {
            this.prevalence = prevalence;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Language extends ApplicablePlatform {
        public Language() {
            super("Language");
        }
    }

    public static class Paradigm extends ApplicablePlatform {
        public Paradigm() {
            super("Paradigm");
        }
    }

    public static class Technology extends ApplicablePlatform {
        public Technology() {
            super("Technology");
        }
    }

    public static class OperatingSystem extends ApplicablePlatform {
        public OperatingSystem() {
            super("Operating_System");
        }
    }

    public static class AlternateTerm {
        private String term;
        private String description;

        public AlternateTerm() {
        }

        public String getTerm() {
            return term;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTerm(String term) {
            this.term = term;
        }
    }

    public static class Consequence {
        private List<String> scope;
        private List<String> impact;
        private String note;

        public Consequence() {
        }

        public void setScope(List<String> scope) {
            this.scope = scope;
        }

        public List<String> getScope() {
            return scope;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public List<String> getImpact() {
            return impact;
        }

        public void setImpact(List<String> impact) {
            this.impact = impact;
        }

    }

    public static class DetectionMethod {
        private String id;
        private String method;
        private String description;
        private Effectiveness effectiveness;

        public String getId() {
            return id;
        }

        public Effectiveness getEffectiveness() {
            return effectiveness;
        }

        public void setEffectiveness(Effectiveness effectiveness) {
            this.effectiveness = effectiveness;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class Mitigation {
        private Phase phase;
        private String description;
        private Effectiveness effectiveness;

        public Mitigation() {
        }

        public Effectiveness getEffectiveness() {
            return effectiveness;
        }

        public void setEffectiveness(Effectiveness effectiveness) {
            this.effectiveness = effectiveness;
        }

        public Phase getPhase() {
            return phase;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPhase(Phase phase) {
            this.phase = phase;
        }
    }

    public static class Note {
        private NoteType type;
        private String value;

        public Note() {
        }

        public Note(NoteType type, String value) {
            this.type = type;
            this.value = value;
        }

        public NoteType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setType(NoteType type) {
            this.type = type;
        }
    }

    public static class Example {
        private String type;

        public Example() {

        }

        public Example(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class TextExample extends Example {
        private String text;

        public TextExample(String type) {
            super(type);
        }

        public TextExample() {

        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

    }

    public static class IntroText extends TextExample {
        public IntroText() {
            super("Intro_Text");
        }

        public IntroText(String text) {
            this();
            setText(text);
        }
    }

    public static class BodyText extends TextExample {
        public BodyText() {
            super("Body_Text");
        }

        public BodyText(String text) {
            this();
            setText(text);
        }
    }

    public static class CodeExample extends TextExample {
        private Nature nature;
        private String language;

        public CodeExample() {
            super("Example_Code");
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Nature getNature() {
            return nature;
        }

        public void setNature(Nature nature) {
            this.nature = nature;
        }

        public CodeExample(String text) {
            this();
            setText(text);
        }
    }

    public static class DemonstrativeExample {
        private List<Example> example;

        public DemonstrativeExample() {
        }

        public List<Example> getExample() {
            return example;
        }

        public void setExample(List<Example> example) {
            this.example = example;
        }
    }

    public static class ObservedExample {
        private String reference;
        private String description;
        private String link;

        public ObservedExample() {
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }
    }

    public static class RelatedAttackPattern {
        private String capec_id;

        public RelatedAttackPattern() {
        }

        public String getCapec_id() {
            return capec_id;
        }

        public void setCapec_id(String capec_id) {
            this.capec_id = capec_id;
        }
    }

    public static class Reference {
        private String externalReferenceId;
        private String section;

        public Reference() {
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getExternalReferenceId() {
            return externalReferenceId;
        }

        public void setExternalReferenceId(String externalReferenceId) {
            this.externalReferenceId = externalReferenceId;
        }
    }

    public static class TaxonomyMapping {
        private String taxonomyName;
        private String id;
        private String name;

        public TaxonomyMapping() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTaxonomyName() {
            return taxonomyName;
        }

        public void setTaxonomyName(String taxonomyName) {
            this.taxonomyName = taxonomyName;
        }
    }

    public static class Submission {
        private String name;
        private String organization;
        private String date;

        public Submission() {

        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Modification extends Submission {
        private String comment;

        public Modification() {
            super();
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public static class ContentHistory {
        private Submission submission;
        private List<Modification> modifications;

        public ContentHistory() {
        }

        public Submission getSubmission() {
            return submission;
        }

        public List<Modification> getModifications() {
            return modifications;
        }

        public void setModifications(List<Modification> modifications) {
            this.modifications = modifications;
        }

        public void setSubmission(Submission submission) {
            this.submission = submission;
        }
    }

    @Id
    private String id;
    private String name;
    private Abstraction abstraction;
    private Structure structure;
    private Status status;
    private String description;
    private String extendedDescription;
    private List<RelatedWeakness> relatedWeaknesses;
    private List<ApplicablePlatform> applicablePlatforms;
    private List<String> backgroundDetails;
    private List<AlternateTerm> alternateTerms;
    private List<Introduction> modesOfIntroduction;
    private List<DetectionMethod> detectionMethods;
    private Likelihood likelihoodOfExploit;
    private List<Consequence> commonConsequences;
    private List<Mitigation> potentialMitigations;
    private List<DemonstrativeExample> demonstrativeExamples;
    private List<RelatedAttackPattern> relatedAttackPatterns;
    private List<Reference> references;
    private List<ObservedExample> observedExamples;
    private List<TaxonomyMapping> taxonomyMappings;
    private List<Note> notes;
    private ContentHistory contentHistory;

    public String getId() {
        return id;
    }

    public List<TaxonomyMapping> getTaxonomyMappings() {
        return taxonomyMappings;
    }

    public void setTaxonomyMappings(List<TaxonomyMapping> taxonomyMappings) {
        this.taxonomyMappings = taxonomyMappings;
    }

    public ContentHistory getContentHistory() {
        return contentHistory;
    }

    public void setContentHistory(ContentHistory contentHistory) {
        this.contentHistory = contentHistory;
    }

    public List<ObservedExample> getObservedExamples() {
        return observedExamples;
    }

    public void setObservedExamples(List<ObservedExample> observedExamples) {
        this.observedExamples = observedExamples;
    }

    public List<DetectionMethod> getDetectionMethods() {
        return detectionMethods;
    }

    public void setDetectionMethods(List<DetectionMethod> detectionMethods) {
        this.detectionMethods = detectionMethods;
    }

    public List<String> getBackgroundDetails() {
        return backgroundDetails;
    }

    public void setBackgroundDetails(List<String> backgroundDetails) {
        this.backgroundDetails = backgroundDetails;
    }

    public Likelihood getLikelihoodOfExploit() {
        return likelihoodOfExploit;
    }

    public void setLikelihoodOfExploit(Likelihood likelihoodOfExploit) {
        this.likelihoodOfExploit = likelihoodOfExploit;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public List<RelatedAttackPattern> getRelatedAttackPatterns() {
        return relatedAttackPatterns;
    }

    public void setRelatedAttackPatterns(List<RelatedAttackPattern> relatedAttackPatterns) {
        this.relatedAttackPatterns = relatedAttackPatterns;
    }

    public List<DemonstrativeExample> getDemonstrativeExamples() {
        return demonstrativeExamples;
    }

    public void setDemonstrativeExamples(List<DemonstrativeExample> demonstrativeExamples) {
        this.demonstrativeExamples = demonstrativeExamples;
    }

    public List<Mitigation> getPotentialMitigations() {
        return potentialMitigations;
    }

    public void setPotentialMitigations(List<Mitigation> potentialMitigations) {
        this.potentialMitigations = potentialMitigations;
    }

    public List<Consequence> getCommonConsequences() {
        return commonConsequences;
    }

    public void setCommonConsequences(List<Consequence> commonConsequences) {
        this.commonConsequences = commonConsequences;
    }

    public List<Introduction> getModesOfIntroduction() {
        return modesOfIntroduction;
    }

    public void setModesOfIntroduction(List<Introduction> modesOfIntroduction) {
        this.modesOfIntroduction = modesOfIntroduction;
    }

    public List<AlternateTerm> getAlternateTerms() {
        return alternateTerms;
    }

    public void setAlternateTerms(List<AlternateTerm> alternateTerms) {
        this.alternateTerms = alternateTerms;
    }

    public List<ApplicablePlatform> getApplicablePlatforms() {
        return applicablePlatforms;
    }

    public void setApplicablePlatforms(List<ApplicablePlatform> applicablePlatforms) {
        this.applicablePlatforms = applicablePlatforms;
    }

    public List<RelatedWeakness> getRelatedWeaknesses() {
        return relatedWeaknesses;
    }

    public void setRelatedWeaknesses(List<RelatedWeakness> relatedWeaknesses) {
        this.relatedWeaknesses = relatedWeaknesses;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public Abstraction getAbstraction() {
        return abstraction;
    }

    public void setAbstraction(Abstraction abstraction) {
        this.abstraction = abstraction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}