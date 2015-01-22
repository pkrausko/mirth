/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import java.util.List;

import org.fife.rsta.ac.js.IconFactory;

public class ClassReference extends Reference {

    private List<String> aliases;

    public ClassReference(int scope, String category, String className, List<String> aliases, String summary) {
        super(Type.CLASS, scope, category, className);
        this.aliases = aliases;
        setSummary(summary);
        setIconName(IconFactory.DEFAULT_CLASS_ICON);
    }

    public List<String> getAliases() {
        return aliases;
    }
}