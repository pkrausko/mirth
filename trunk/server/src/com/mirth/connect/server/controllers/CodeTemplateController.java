/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.CodeTemplateSummary;
import com.mirth.connect.model.ServerEventContext;

public abstract class CodeTemplateController extends Controller {

    public static CodeTemplateController getInstance() {
        return ControllerFactory.getFactory().createCodeTemplateController();
    }

    public abstract List<CodeTemplateLibrary> getLibraries(Set<String> libraryIds, boolean includeCodeTemplates) throws ControllerException;

    public abstract CodeTemplateLibrary getLibraryById(String libraryId) throws ControllerException;

    public abstract CodeTemplateLibrary getLibraryByName(String name) throws ControllerException;

    public abstract boolean updateLibraries(List<CodeTemplateLibrary> libraries, ServerEventContext context, boolean override) throws ControllerException;

    public abstract List<CodeTemplate> getCodeTemplates(Set<String> codeTemplateIds) throws ControllerException;

    public abstract List<CodeTemplateSummary> getCodeTemplateSummary(Map<String, Integer> clientRevisions) throws ControllerException;

    public abstract CodeTemplate getCodeTemplateById(String codeTemplateId) throws ControllerException;

    public abstract Map<String, Integer> getCodeTemplateRevisionsForChannel(String channelId) throws ControllerException;

    public abstract boolean updateCodeTemplate(CodeTemplate codeTemplate, ServerEventContext context, boolean override) throws ControllerException;

    public abstract void removeCodeTemplate(CodeTemplate codeTemplate, ServerEventContext context) throws ControllerException;

    public abstract CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(List<CodeTemplateLibrary> libraries, List<CodeTemplateLibrary> removedLibraries, List<CodeTemplate> updatedCodeTemplates, List<CodeTemplate> removedCodeTemplates, ServerEventContext context, boolean override);
}