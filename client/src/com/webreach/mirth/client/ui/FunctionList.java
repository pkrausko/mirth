/*
 * FunctionList.java
 *
 * Created on July 9, 2007, 6:06 PM
 */

package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory.ListType;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListPanel;
import java.util.LinkedHashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

/**
 *
 * @author  brendanh
 */
public class FunctionList extends javax.swing.JPanel
{
    private LinkedHashMap<String, JPanel> panels;
    
    public FunctionList()
    {
        initComponents();
    }
    
    /** Creates new form FunctionList */
    public FunctionList(int context)
    {
        initComponents();
        panels = new LinkedHashMap<String, JPanel>();
        ReferenceListFactory builder = new ReferenceListFactory();
        addPanel(new ReferenceListPanel("All", builder.getVariableListItems(ListType.ALL, context)), "All");
        addPanel(new ReferenceListPanel("Utility Functions", builder.getVariableListItems(ListType.UTILITY, context)), "Utility Functions");
        addPanel(new ReferenceListPanel("Date Functions", builder.getVariableListItems(ListType.DATE, context)), "Date Functions");
        addPanel(new ReferenceListPanel("Conversion Functions", builder.getVariableListItems(ListType.CONVERSION, context)), "Conversion Functions");
        addPanel(new ReferenceListPanel("Logging and Alerts", builder.getVariableListItems(ListType.LOGGING_AND_ALERTS, context)), "Logging and Alerts");
        addPanel(new ReferenceListPanel("Database Functions", builder.getVariableListItems(ListType.DATABASE, context)), "Database Functions");
        if(context >= ReferenceListFactory.MESSAGE_CONTEXT)
            addPanel(new ReferenceListPanel("Message Functions", builder.getVariableListItems(ListType.MESSAGE, context)), "Message Functions");
        addPanel(new ReferenceListPanel("Map Functions", builder.getVariableListItems(ListType.MAP, context)), "Map Functions");
        setReferencePanel();
    }
    
    public void setReferencePanel()
    {
        variableReferenceDropDownActionPerformed(null);
    }
    
    public void addPanel(JPanel panel, String name)
    {
        panels.put(name, panel);

        String[] items = new String[panels.keySet().size()];
        int i = 0;
        for (String s : panels.keySet())
        {
            items[i] = s;
            i++;
        }

        variableReferenceDropDown.setModel(new DefaultComboBoxModel(items));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        variableReferenceDropDown = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        variableScrollPane = new javax.swing.JScrollPane();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        variableReferenceDropDown.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                variableReferenceDropDownActionPerformed(evt);
            }
        });

        jLabel1.setText("Filter:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(variableReferenceDropDown, 0, 91, Short.MAX_VALUE)
                .addContainerGap())
            .add(variableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(variableReferenceDropDown, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(variableScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void variableReferenceDropDownActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_variableReferenceDropDownActionPerformed
    {//GEN-HEADEREND:event_variableReferenceDropDownActionPerformed
        variableScrollPane.setViewportView((panels.get((String) variableReferenceDropDown.getSelectedItem())));
    }//GEN-LAST:event_variableReferenceDropDownActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JComboBox variableReferenceDropDown;
    private javax.swing.JScrollPane variableScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
