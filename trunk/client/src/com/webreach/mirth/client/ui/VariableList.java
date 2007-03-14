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

package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.ui.util.VariableListUtil;
import com.webreach.mirth.model.Rule;
import java.util.ArrayList;
import java.util.List;
import com.webreach.mirth.model.Step;
import java.util.LinkedHashSet;

/**
 * A panel that contains the MirthVariableList.
 */
public class VariableList extends javax.swing.JPanel
{
    private final String VAR_PATTERN = "[connector][channel][global][local]Map.put\\(['|\"]([^'|^\"]*)[\"|']";

    /** Creates new form VariableList */
    public VariableList()
    {
        initComponents();
    }

    /**
     * Set the variable list from a list of steps.
     */
    public void setVariableListInbound(List<Rule> rules, List<Step> steps)
    {
        LinkedHashSet<String> variables = new LinkedHashSet<String>();
        int i = 0;
        variables.add("Message ID");
        variables.add("Raw Data");
        variables.add("Transformed Data");
        variables.add("Encoded Data");
        variables.add("Message Source");
        variables.add("Message Type");
        variables.add("Message Version");
        variables.add("Date");
        variables.add("Formatted Date");
        variables.add("Timestamp");
        variables.add("Unique ID");
        variables.add("Original File Name");
        variables.add("Count");
        variables.add("Entity Encoder");
        variables.addAll(VariableListUtil.getRuleVariables(rules));
        variables.addAll(VariableListUtil.getStepVariables(steps));

        mirthVariableList.removeAll();
        mirthVariableList.setListData(variables.toArray());

        jScrollPane1.setViewportView(mirthVariableList);
    }

    public void setVariableListOutbound()
    {
        ArrayList<String> variables = new ArrayList<String>();
        variables.add("Raw Data");
        variables.add("Transformed Data");
        variables.add("Encoded Data");
        mirthVariableList.removeAll();
        mirthVariableList.setListData(variables.toArray());
        jScrollPane1.setViewportView(mirthVariableList);
    }

    public void setSourceMappingsLabel()
    {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Source Mappings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
    }

    public void setDestinationMappingsLabel()
    {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Destination Mappings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jScrollPane1 = new javax.swing.JScrollPane();
        mirthVariableList = new com.webreach.mirth.client.ui.components.MirthVariableList();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Variable List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0)));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthVariableList.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        mirthVariableList.setModel(new javax.swing.AbstractListModel()
        {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

            public int getSize()
            {
                return strings.length;
            }

            public Object getElementAt(int i)
            {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(mirthVariableList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;

    private com.webreach.mirth.client.ui.components.MirthVariableList mirthVariableList;
    // End of variables declaration//GEN-END:variables

}
