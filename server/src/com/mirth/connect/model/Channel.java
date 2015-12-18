/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;
import com.mirth.connect.donkey.util.purge.PurgeUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Channel is the main element of the Mirth architecture. Channels connect a single source with
 * multiple destinations which are represented by Connectors.
 * 
 */

@XStreamAlias("channel")
public class Channel implements Serializable, Auditable, Migratable, Purgable, Cacheable<Channel> {
    private String id;
    private Integer nextMetaDataId;
    private String name;
    private String description;
    private boolean enabled;
    private Calendar lastModified;
    private int revision;
    private Connector sourceConnector;
    private List<Connector> destinationConnectors;
    private String preprocessingScript;
    private String postprocessingScript;
    private String deployScript;
    private String undeployScript;
    private ChannelProperties properties;
    private List<CodeTemplateLibrary> codeTemplateLibraries;

    public Channel() {
        enabled = true;
        destinationConnectors = new ArrayList<Connector>();
        properties = new ChannelProperties();
        nextMetaDataId = 1;
        codeTemplateLibraries = new ArrayList<CodeTemplateLibrary>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNextMetaDataId() {
        return nextMetaDataId;
    }

    public void setNextMetaDataId(Integer nextMetaDataId) {
        this.nextMetaDataId = nextMetaDataId;
    }

    @Override
    public Integer getRevision() {
        return this.revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Connector getSourceConnector() {
        return this.sourceConnector;
    }

    public void setSourceConnector(Connector sourceConnector) {
        sourceConnector.setMetaDataId(0);
        this.sourceConnector = sourceConnector;
    }

    public List<Connector> getDestinationConnectors() {
        return this.destinationConnectors;
    }

    public void addDestination(Connector destinationConnector) {
        destinationConnector.setMetaDataId(nextMetaDataId++);
        destinationConnectors.add(destinationConnector);
    }

    public List<Connector> getEnabledDestinationConnectors() {
        List<Connector> enabledConnectors = new ArrayList<Connector>();
        for (Connector connector : getDestinationConnectors()) {
            if (connector.isEnabled()) {
                enabledConnectors.add(connector);
            }
        }
        return enabledConnectors;
    }

    public String getPostprocessingScript() {
        return postprocessingScript;
    }

    public void setPostprocessingScript(String postprocessingScript) {
        this.postprocessingScript = postprocessingScript;
    }

    public String getPreprocessingScript() {
        return preprocessingScript;
    }

    public void setPreprocessingScript(String preprocessingScript) {
        this.preprocessingScript = preprocessingScript;
    }

    public String getDeployScript() {
        return this.deployScript;
    }

    public void setDeployScript(String deployScript) {
        this.deployScript = deployScript;
    }

    public String getUndeployScript() {
        return this.undeployScript;
    }

    public void setUndeployScript(String undeployScript) {
        this.undeployScript = undeployScript;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public ChannelProperties getProperties() {
        return properties;
    }

    public List<CodeTemplateLibrary> getCodeTemplateLibraries() {
        return codeTemplateLibraries;
    }

    public void setCodeTemplateLibraries(List<CodeTemplateLibrary> codeTemplateLibraries) {
        this.codeTemplateLibraries = codeTemplateLibraries;
    }

    @Override
    public Channel cloneIfNeeded() {
        return this;
    }

    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof Channel)) {
            return false;
        }

        Channel channel = (Channel) that;

        return ObjectUtils.equals(this.getId(), channel.getId()) && ObjectUtils.equals(this.getName(), channel.getName()) && ObjectUtils.equals(this.getDescription(), channel.getDescription()) && ObjectUtils.equals(this.isEnabled(), channel.isEnabled()) && ObjectUtils.equals(this.getLastModified(), channel.getLastModified()) && ObjectUtils.equals(this.getRevision(), channel.getRevision()) && ObjectUtils.equals(this.getSourceConnector(), channel.getSourceConnector()) && ObjectUtils.equals(this.getDestinationConnectors(), channel.getDestinationConnectors()) && ObjectUtils.equals(this.getUndeployScript(), channel.getUndeployScript()) && ObjectUtils.equals(this.getDeployScript(), channel.getDeployScript()) && ObjectUtils.equals(this.getPostprocessingScript(), channel.getPostprocessingScript()) && ObjectUtils.equals(this.getPreprocessingScript(), channel.getPreprocessingScript()) && ObjectUtils.equals(this.getCodeTemplateLibraries(), channel.getCodeTemplateLibraries());
    }

    public String toString() {
        return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("name", name).append("enabled", enabled).toString();
    }

    public String toAuditString() {
        return new ToStringBuilder(this, CalendarToStringStyle.instance()).append("id", id).append("name", name).toString();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        DonkeyElement shutdownScript = element.getChildElement("shutdownScript");

        if (shutdownScript != null) {
            shutdownScript.setNodeName("undeployScript");
        }
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        element.addChildElement("codeTemplateLibraries");
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("id", id);
        purgedProperties.put("nextMetaDataId", nextMetaDataId);
        purgedProperties.put("nameChars", PurgeUtil.countChars(name));
        purgedProperties.put("descriptionChars", PurgeUtil.countChars(description));
        purgedProperties.put("enabled", enabled);
        purgedProperties.put("lastModified", lastModified);
        Map<String, Object> sourceProperties = sourceConnector.getPurgedProperties();
        sourceProperties.put("messageStatistics", PurgeUtil.getMessageStatistics(id, sourceConnector.getMetaDataId()));
        purgedProperties.put("sourceConnector", sourceProperties);
        List<Map<String, Object>> purgedDestinationConnectors = new ArrayList<Map<String, Object>>();
        for (Connector connector : destinationConnectors) {
            Map<String, Object> destinationProperties = connector.getPurgedProperties();
            destinationProperties.put("messageStatistics", PurgeUtil.getMessageStatistics(id, connector.getMetaDataId()));
            purgedDestinationConnectors.add(destinationProperties);
        }
        purgedProperties.put("destinationConnectors", purgedDestinationConnectors);
        purgedProperties.put("preprocessingScriptLines", PurgeUtil.countLines(preprocessingScript));
        purgedProperties.put("postprocessingScriptLines", PurgeUtil.countLines(postprocessingScript));
        purgedProperties.put("deployScriptLines", PurgeUtil.countLines(deployScript));
        purgedProperties.put("undeployScriptLines", PurgeUtil.countLines(undeployScript));
        purgedProperties.put("properties", properties.getPurgedProperties());
        purgedProperties.put("messageStatistics", PurgeUtil.getMessageStatistics(id, null));

        List<Map<String, Object>> purgedCodeTemplateLibraries = new ArrayList<Map<String, Object>>();
        for (CodeTemplateLibrary codeTemplateLibrary : codeTemplateLibraries) {
            purgedCodeTemplateLibraries.add(codeTemplateLibrary.getPurgedProperties());
        }
        purgedProperties.put("codeTemplateLibraries", purgedCodeTemplateLibraries);

        return purgedProperties;
    }
}
