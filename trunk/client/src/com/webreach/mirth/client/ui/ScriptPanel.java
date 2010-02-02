package com.webreach.mirth.client.ui;

import java.util.Map;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.syntax.jedit.SyntaxDocument;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

public class ScriptPanel extends javax.swing.JPanel {

    private static SyntaxDocument scriptDoc;
    private String selectedItem = null;
    private int context;
    public static String DEPLOY_SCRIPT = "Deploy";
    public static String SHUTDOWN_SCRIPT = "Shutdown";
    public static String PREPROCESSOR_SCRIPT = "Preprocessor";
    public static String POSTPROCESSOR_SCRIPT = "Postprocessor";
    Map<String, String> loadedScripts;
    private String[] scripts = new String[]{DEPLOY_SCRIPT, SHUTDOWN_SCRIPT, PREPROCESSOR_SCRIPT, POSTPROCESSOR_SCRIPT};

    public ScriptPanel() {
        initComponents();
        scriptList.setModel(new DefaultComboBoxModel(scripts));
    }

    /** Creates new form ScriptPanel */
    public ScriptPanel(int context) {
        this.context = context;

        initComponents();
        scriptDoc = new SyntaxDocument();
        scriptDoc.setTokenMarker(new JavaScriptTokenMarker());
        script.setDocument(scriptDoc);

        scriptList.setModel(new DefaultComboBoxModel(scripts));
    }

    public void setScripts(Map<String, String> scripts) {
        this.loadedScripts = scripts;
        selectedItem = null;
        scriptListActionPerformed(null);
        functionList1.setDefaultDropDownValue();
    }

    public Map<String, String> getScripts() {
        loadedScripts.put(((String) scriptList.getSelectedItem()), script.getText());
        return this.loadedScripts;
    }

    public void validateCurrentScript() {
        StringBuilder sb = new StringBuilder();
        Context context = Context.enter();
        try {
            context.compileString("function rhinoWrapper() {" + script.getText() + "\n}", PlatformUI.MIRTH_FRAME.mirthClient.getGuid(), 1, null);
            sb.append("JavaScript was successfully validated.");
        } catch (EvaluatorException e) {
            sb.append("Error on line " + e.lineNumber() + ": " + e.getMessage() + " of the current script.");
        } catch (Exception e) {
            sb.append("Unknown error occurred during validation.");
        }

        Context.exit();

        PlatformUI.MIRTH_FRAME.alertInformation(this, sb.toString());
    }

    public String validateScript(String script) {
        String error = null;
        Context context = Context.enter();
        try {
            context.compileString("function rhinoWrapper() {" + script + "\n}", UUID.randomUUID().toString(), 1, null);
        } catch (EvaluatorException e) {
            error = "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
        } catch (Exception e) {
            error = "Unknown error occurred during validation.";
        }

        Context.exit();

        return error;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        functionList1 = new FunctionList(this.context);
        script = new com.webreach.mirth.client.ui.components.MirthSyntaxTextArea(true,true,this.context);
        jLabel5 = new javax.swing.JLabel();
        scriptList = new javax.swing.JComboBox();

        setBackground(new java.awt.Color(255, 255, 255));

        script.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel5.setText("Script:");

        scriptList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        scriptList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scriptListActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scriptList, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(script, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(functionList1, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(functionList1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(scriptList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(script, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void scriptListActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_scriptListActionPerformed
    {//GEN-HEADEREND:event_scriptListActionPerformed
        if (selectedItem != null) {
            loadedScripts.put(selectedItem, script.getText());
        }

        selectedItem = (String) scriptList.getSelectedItem();
        script.setText(loadedScripts.get(selectedItem));
    }//GEN-LAST:event_scriptListActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.webreach.mirth.client.ui.FunctionList functionList1;
    private javax.swing.JLabel jLabel5;
    private com.webreach.mirth.client.ui.components.MirthSyntaxTextArea script;
    private javax.swing.JComboBox scriptList;
    // End of variables declaration//GEN-END:variables
}
