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
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.purge.Purgable;

public class ChannelMetadata implements Serializable, Purgable {

    private boolean enabled;
    private Calendar lastModified;
    private ChannelPruningSettings pruningSettings;
    private Set<String> tags;

    public ChannelMetadata() {
        enabled = true;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public ChannelPruningSettings getPruningSettings() {
        if (pruningSettings == null) {
            pruningSettings = new ChannelPruningSettings();
        }
        return pruningSettings;
    }

    public void setPruningSettings(ChannelPruningSettings pruningSettings) {
        this.pruningSettings = pruningSettings;
    }

    public Set<String> getTags() {
        if (tags == null) {
            tags = new LinkedHashSet<String>();
        }
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();

        purgedProperties.put("enabled", enabled);
        purgedProperties.put("lastModified", lastModified);

        if (pruningSettings != null) {
            purgedProperties.put("pruningSettings", pruningSettings.getPurgedProperties());
        }

        if (tags != null) {
            purgedProperties.put("tagsCount", tags.size());
        }

        return purgedProperties;
    }
}