/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListPanel;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory.ListType;

public class MirthEditorPane extends JPanel
{
    // transformer constants
    public static final int STEP_NUMBER_COL = 0;

    public static final int STEP_NAME_COL = 1;

    public static final int STEP_TYPE_COL = 2;

    public static final int STEP_DATA_COL = 3;

    // filter constants
    public static final int RULE_NUMBER_COL = 0;

    public static final int RULE_OP_COL = 1;

    public static final int RULE_NAME_COL = 2;

    public static final int RULE_SCRIPT_COL = 3;

    // a list of panels to load
    public static final String BLANK_TYPE = "";

    public static final String MESSAGE_TYPE = "Message Builder";

    public static final String MAPPER_TYPE = "Mapper";

    public static final String JAVASCRIPT_TYPE = "JavaScript";

    public int prevSelRow = -1;

    public boolean updating = false;

    public boolean modified = false;

    public Frame parent = PlatformUI.MIRTH_FRAME;

    public JScrollPane referenceScrollPane;

    public JPanel refPanel;

    public TabbedTemplatePanel tabTemplatePanel;

    public MirthEditorPane()
    {
        super();
        this.setBorder(BorderFactory.createEmptyBorder());
        ReferenceListFactory builder = new ReferenceListFactory();
        tabTemplatePanel = new TabbedTemplatePanel(this);
        tabTemplatePanel.addPanel(new ReferenceListPanel("All", builder.getVariableListItems(ListType.ALL)), "All");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Utility Functions", builder.getVariableListItems(ListType.UTILITY)), "Utility Functions");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Date Functions", builder.getVariableListItems(ListType.DATE)), "Date Functions");

        tabTemplatePanel.addPanel(new ReferenceListPanel("Conversion Functions", builder.getVariableListItems(ListType.CONVERSION)), "Conversion Functions");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Logging and Alerts", builder.getVariableListItems(ListType.LOGGING_AND_ALERTS)), "Logging and Alerts");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Database Functions", builder.getVariableListItems(ListType.DATABASE)), "Database Functions");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Message Functions", builder.getVariableListItems(ListType.MESSAGE)), "Message Functions");
        //tabTemplatePanel.addPanel(new ReferenceListPanel("XML Functions", builder.getVariableListItems(ListType.XML)), "XML Functions");
        //tabTemplatePanel.addPanel(new ReferenceListPanel("HL7 Functions", builder.getVariableListItems(ListType.HL7)), "HL7 Functions");
        tabTemplatePanel.addPanel(new ReferenceListPanel("Map Functions", builder.getVariableListItems(ListType.MAP)), "Map Functions");
        
        tabTemplatePanel.setReferencePanel();
        tabTemplatePanel.setBorder(BorderFactory.createEmptyBorder());
        refPanel = new JPanel();
        refPanel.setBorder(BorderFactory.createEmptyBorder());
        refPanel.setLayout(new BorderLayout());
        refPanel.add(tabTemplatePanel, BorderLayout.CENTER);
        // let the parent decide how big this should be
        this.setPreferredSize(new Dimension(0, 0));
    }

    public void updateTaskPane()
    {
    }

    public int getSelectedRow()
    {
        return 0;
    }

    public DefaultTableModel getTableModel()
    {
        return null;
    }
}
