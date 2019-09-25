package org.daisleyharrison.security.services.vault.models;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Policy {
    public static final Policy DENY_ALL = new Policy("**", Capability.DENY);
    public static final Policy ALLOW_ALL = new Policy("**", Capability.CREATE, Capability.DELETE, Capability.LIST, Capability.READ, Capability.UPDATE, Capability.MANAGE);
    @JsonProperty("path")
    private String path;
    @JsonProperty("pathType")
    private PolicyPathType pathType;
    @JsonProperty("capabilities")
    private List<Capability> capabilities;
    private PathMatcher pathMatcher;

    public Policy() {
        this.pathType = PolicyPathType.GLOB;
        this.capabilities = new ArrayList<>();
        this.capabilities.add(Capability.DENY);
    }

    public Policy(String path) {
        this();
        setPath(path);
    }

    public Policy(String path, List<Capability> capabilities) {
        this.pathType = PolicyPathType.GLOB;
        setPath(path);
        this.capabilities = new ArrayList<>();
        this.capabilities.addAll(capabilities);
    }

    public Policy(String path, Capability... capabilities) {
        this(path, Arrays.asList(capabilities));
    }

    /**
     * @return String return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
        if(path == null){
            this.pathMatcher = null;
        } else {
            this.pathMatcher = FileSystems.getDefault().getPathMatcher(pathType.toString() + ":" + path);
        }
    }

    /**
     * @return List<Capability> return the capabilities
     */
    public List<Capability> getCapabilities() {
        return capabilities;
    }

    /**
     * @param capabilities the capabilities to set
     */
    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public boolean hasCapability(Capability capability) {
        return this.capabilities.contains(capability);
    }

    public Policy combine(Policy policy) {
        if (hasCapability(Capability.DENY)) {
            return this;
        } else if (policy.hasCapability(Capability.DENY)) {
            return policy;
        } else {
            Set<Capability> combined = new HashSet<Capability>();
            this.capabilities.forEach(capability -> combined.add(capability));
            policy.capabilities.forEach(capability -> combined.add(capability));
            Policy combinedPolicy = new Policy();
            combinedPolicy.setCapabilities(new ArrayList<>(capabilities));
            return combinedPolicy;
        }
    }

    public boolean applysToPath(Path path){
        if(pathMatcher == null){
            return true;
        } 
        return pathMatcher.matches(path);
    }


    /**
     * @return PolicyPathType return the pathType
     */
    public PolicyPathType getPathType() {
        return pathType;
    }

    /**
     * @param pathType the pathType to set
     */
    public void setPathType(PolicyPathType pathType) {
        this.pathType = pathType;
    }
    public String toCapabilitiesString() {
        // note stream() and join had a wierd problem here
        StringBuilder result = new StringBuilder();
        capabilities.forEach(capability->{
            if(result.length()>0){
                result.append(",");
            }
            result.append(capability.toString());
        });
        return result.toString();
    }

    @Override
    public String toString() {
        return this.pathType.toString() + ":" + this.path + " => " + toCapabilitiesString();
    }
}