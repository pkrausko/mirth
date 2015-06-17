package com.mirth.connect.connectors.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.ui.Mirth;
import com.mirth.connect.client.ui.MirthDialog;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.RefreshTableModel;
import com.mirth.connect.client.ui.TextFieldCellEditor;
import com.mirth.connect.client.ui.UIConstants;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;

public class AdvancedSftpSettingsDialog extends MirthDialog implements AdvancedSettingsDialog {
    private final int NAME_COLUMN = 0;
    private final String NAME_COLUMN_NAME = "Name";
    private final String VALUE_COLUMN_NAME = "Value";

    private SftpSchemeProperties fileSchemeProperties;

    public AdvancedSftpSettingsDialog() {
        super(PlatformUI.MIRTH_FRAME, true);

        this.fileSchemeProperties = (SftpSchemeProperties) fileSchemeProperties;

        setTitle("Method Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 400));
        setResizable(false);
        setLayout(new MigLayout("insets 8 8 0 8, novisualpadding, hidemode 3"));
        getContentPane().setBackground(UIConstants.BACKGROUND_COLOR);

        initComponents();
        initLayout();
    }

    @Override
    public void setDialogVisible(boolean visible) {
        keyLocationField.setBackground(null);
        knownHostsField.setBackground(null);
        setLocationRelativeTo(PlatformUI.MIRTH_FRAME);
        setVisible(visible);
    }

    @Override
    public boolean isDefaultProperties() {
        boolean isDefault = true;

        if (!usePasswordRadio.isSelected()) {
            isDefault = false;
        }

        if (StringUtils.isNotEmpty(keyLocationField.getText())) {
            isDefault = false;
        }

        if (StringUtils.isNotEmpty(passphraseField.getText())) {
            isDefault = false;
        }

        if (!useKnownHostsAskRadio.isSelected()) {
            isDefault = false;
        }

        if (StringUtils.isNotEmpty(knownHostsField.getText())) {
            isDefault = false;
        }

        if (configurationsTable.getRowCount() > 0) {
            isDefault = false;
        }

        return isDefault;
    }

    @Override
    public void setFileSchemeProperties(SchemeProperties schemeProperties) {
        fileSchemeProperties = (SftpSchemeProperties) schemeProperties;

        if (fileSchemeProperties.isPasswordAuth() && fileSchemeProperties.isKeyAuth()) {
            useBothRadio.setSelected(true);
        } else {
            if (fileSchemeProperties.isKeyAuth()) {
                usePrivateKeyRadio.setSelected(true);
            } else {
                usePasswordRadio.setSelected(true);
            }
        }

        keyLocationField.setText(fileSchemeProperties.getKeyFile());
        passphraseField.setText(fileSchemeProperties.getPassPhrase());

        String hostnameVerification = fileSchemeProperties.getHostChecking();
        if (hostnameVerification.equals("yes")) {
            useKnownHostsYesRadio.setSelected(true);
        } else if (hostnameVerification.equals("ask")) {
            useKnownHostsAskRadio.setSelected(true);
        } else {
            useKnownHostsNoRadio.setSelected(true);
        }

        knownHostsField.setText(fileSchemeProperties.getKnownHostsFile());

        Map<String, String> configurationSettings = fileSchemeProperties.getConfigurationSettings();
        if (configurationSettings != null && configurationSettings.size() > 0) {
            DefaultTableModel model = (DefaultTableModel) configurationsTable.getModel();
            model.setNumRows(0);

            for (Map.Entry<String, String> entry : configurationSettings.entrySet()) {
                model.addRow(new Object[] { entry.getKey(), entry.getValue() });
            }

            deleteButton.setEnabled(true);
        } else {
            configurationsTable.setModel(new RefreshTableModel(new Object[0][1], new String[] {
                    "Name", "Value" }));
            configurationsTable.getColumnModel().getColumn(configurationsTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new ConfigTableCellEditor(true));
            configurationsTable.getColumnModel().getColumn(configurationsTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new ConfigTableCellEditor(false));
        }

        authenticationRadioButtonActionPerformed();
    }

    private void getProperties() {
        if (usePasswordRadio.isSelected()) {
            fileSchemeProperties.setPasswordAuth(true);
            fileSchemeProperties.setKeyAuth(false);
        } else if (usePrivateKeyRadio.isSelected()) {
            fileSchemeProperties.setPasswordAuth(false);
            fileSchemeProperties.setKeyAuth(true);
        } else {
            fileSchemeProperties.setPasswordAuth(true);
            fileSchemeProperties.setKeyAuth(true);
        }

        fileSchemeProperties.setKeyFile(keyLocationField.getText());
        fileSchemeProperties.setPassPhrase(passphraseField.getText());

        if (useKnownHostsYesRadio.isSelected()) {
            fileSchemeProperties.setHostChecking("yes");
        } else if (useKnownHostsAskRadio.isSelected()) {
            fileSchemeProperties.setHostChecking("ask");
        } else {
            fileSchemeProperties.setHostChecking("no");
        }

        fileSchemeProperties.setKnownHostsFile(knownHostsField.getText());

        Map<String, String> configurationSettings = new LinkedHashMap<String, String>();

        for (int rowCount = 0; rowCount < configurationsTable.getRowCount(); rowCount++) {
            configurationSettings.put((String) configurationsTable.getValueAt(rowCount, 0), (String) configurationsTable.getValueAt(rowCount, 1));
        }

        fileSchemeProperties.setConfigurationSettings(configurationSettings);
    }

    @Override
    public SchemeProperties getFileSchemeProperties() {
        return fileSchemeProperties;
    }

    @Override
    public SchemeProperties getDefaultProperties() {
        return new SftpSchemeProperties();
    }

    @Override
    public String getSummaryText() {
        List<String> list = new ArrayList<String>();

        String authType = "Password";
        if (fileSchemeProperties.isPasswordAuth() && fileSchemeProperties.isKeyAuth()) {
            authType = "Password and Private Key";
        } else if (fileSchemeProperties.isKeyAuth()) {
            authType = "Private Key";
        }

        list.add(authType + " Authentication");

        String verification = "Ask";
        if (fileSchemeProperties.getHostChecking().equals("yes")) {
            verification = "On";
        } else if (fileSchemeProperties.getHostChecking().equals("no")) {
            verification = "Off";
        }
        list.add("Hostname Checking " + verification);

        return StringUtils.join(list, " / ");
    }

    @Override
    public boolean validateProperties() {
        boolean valid = true;

        String errors = "";
        if (!usePasswordRadio.isSelected() && StringUtils.isEmpty(keyLocationField.getText())) {
            valid = false;
            errors += "Key File cannot be blank.\n";
            keyLocationField.setBackground(UIConstants.INVALID_COLOR);
        } else {
            keyLocationField.setBackground(null);
        }

        if (useKnownHostsYesRadio.isSelected() && StringUtils.isEmpty(knownHostsField.getText())) {
            valid = false;
            errors += "Known Hosts cannot be blank.";
            knownHostsField.setBackground(UIConstants.INVALID_COLOR);
        } else {
            knownHostsField.setBackground(null);
        }

        if (StringUtils.isNotBlank(errors)) {
            PlatformUI.MIRTH_FRAME.alertError(this, errors);
            return valid;
        }

        return valid;
    }

    private void initComponents() {
        authenticationLabel = new JLabel("Authentication:");

        usePasswordRadio = new MirthRadioButton("Password");
        usePasswordRadio.setFocusable(false);
        usePasswordRadio.setBackground(new Color(255, 255, 255));
        usePasswordRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePasswordRadio.setToolTipText("Select this option to use a password to gain access to the server.");
        usePasswordRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticationRadioButtonActionPerformed();
            }
        });

        usePrivateKeyRadio = new MirthRadioButton("Private Key");
        usePrivateKeyRadio.setSelected(true);
        usePrivateKeyRadio.setFocusable(false);
        usePrivateKeyRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        usePrivateKeyRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usePrivateKeyRadio.setToolTipText("Select this option to use a private key to gain access to the server.");
        usePrivateKeyRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticationRadioButtonActionPerformed();
            }
        });

        useBothRadio = new MirthRadioButton("Both");
        useBothRadio.setSelected(true);
        useBothRadio.setFocusable(false);
        useBothRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useBothRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useBothRadio.setToolTipText("Select this option to use both a password and a private key to gain access to the server.");
        useBothRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticationRadioButtonActionPerformed();
            }
        });

        privateKeyButtonGroup = new ButtonGroup();
        privateKeyButtonGroup.add(usePasswordRadio);
        privateKeyButtonGroup.add(usePrivateKeyRadio);
        privateKeyButtonGroup.add(useBothRadio);

        keyLocationLabel = new JLabel("Key File:");
        keyLocationField = new MirthTextField();
        keyLocationField.setToolTipText("The absolute file path of the local Private Key used to gain access to the remote server.");

        passphraseLabel = new JLabel("Passphrase:");
        passphraseField = new MirthTextField();
        passphraseField.setToolTipText("The passphrase associated with the local Private Key.");

        useKnownHostsLabel = new JLabel("Host Key Checking:");

        useKnownHostsYesRadio = new MirthRadioButton("Yes");
        useKnownHostsYesRadio.setFocusable(false);
        useKnownHostsYesRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useKnownHostsYesRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useKnownHostsYesRadio.setToolTipText("<html>Select this option to validate the server's host key within the provided<br>Known Hosts file. Known Hosts file is required.</html>");

        useKnownHostsAskRadio = new MirthRadioButton("Ask");
        useKnownHostsAskRadio.setFocusable(false);
        useKnownHostsAskRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useKnownHostsAskRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useKnownHostsAskRadio.setToolTipText("<html>Select this option to ask the user to add the server's host key to the provided<br>Known Hosts file. Known Hosts file is optional.</html>");

        useKnownHostsNoRadio = new MirthRadioButton("No");
        useKnownHostsNoRadio.setSelected(true);
        useKnownHostsNoRadio.setFocusable(false);
        useKnownHostsNoRadio.setBackground(UIConstants.BACKGROUND_COLOR);
        useKnownHostsNoRadio.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        useKnownHostsNoRadio.setToolTipText("<html>Select this option to always add the server's host key to the provided<br>Known Hosts file. Known Hosts file is optional.</html>");

        knownHostsButtonGroup = new ButtonGroup();
        knownHostsButtonGroup.add(useKnownHostsYesRadio);
        knownHostsButtonGroup.add(useKnownHostsAskRadio);
        knownHostsButtonGroup.add(useKnownHostsNoRadio);

        knownHostsLocationLabel = new JLabel("Known Hosts:");
        knownHostsField = new MirthTextField();
        knownHostsField.setToolTipText("The absolute file path of the local Known Hosts file used to authenticate the remote server.");

        configurationsLabel = new JLabel("Configuration Options:");
        configurationsTable = new MirthTable();

        Object[][] tableData = new Object[0][1];
        configurationsTable.setModel(new RefreshTableModel(tableData, new String[] { "Name",
                "Value" }));
        configurationsTable.setOpaque(true);
        configurationsTable.getColumnModel().getColumn(configurationsTable.getColumnModel().getColumnIndex(NAME_COLUMN_NAME)).setCellEditor(new ConfigTableCellEditor(true));
        configurationsTable.getColumnModel().getColumn(configurationsTable.getColumnModel().getColumnIndex(VALUE_COLUMN_NAME)).setCellEditor(new ConfigTableCellEditor(false));

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            configurationsTable.setHighlighters(highlighter);
        }

        configurationsScrollPane = new JScrollPane();
        configurationsScrollPane.getViewport().add(configurationsTable);

        newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) configurationsTable.getModel();

                Vector<String> row = new Vector<String>();
                String header = "Property";

                for (int i = 1; i <= configurationsTable.getRowCount() + 1; i++) {
                    boolean exists = false;
                    for (int index = 0; index < configurationsTable.getRowCount(); index++) {
                        if (((String) configurationsTable.getValueAt(index, 0)).equalsIgnoreCase(header + i)) {
                            exists = true;
                        }
                    }

                    if (!exists) {
                        row.add(header + i);
                        break;
                    }
                }

                model.addRow(row);

                int rowSelectionNumber = configurationsTable.getRowCount() - 1;
                configurationsTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);

                Boolean enabled = deleteButton.isEnabled();
                if (!enabled) {
                    deleteButton.setEnabled(true);
                }
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowSelectionNumber = configurationsTable.getSelectedRow();
                if (rowSelectionNumber > -1) {
                    DefaultTableModel model = (DefaultTableModel) configurationsTable.getModel();
                    model.removeRow(rowSelectionNumber);

                    rowSelectionNumber--;
                    if (rowSelectionNumber > -1) {
                        configurationsTable.setRowSelectionInterval(rowSelectionNumber, rowSelectionNumber);
                    }

                    if (configurationsTable.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }
                }
            }
        });

        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okCancelButtonActionPerformed(true);
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                okCancelButtonActionPerformed(false);
            }
        });

        authenticationRadioButtonActionPerformed();
    }

    private void initLayout() {
        JPanel propertiesPanel = new JPanel(new MigLayout("insets 12, novisualpadding, hidemode 3, fillx", "[right][left]"));
        propertiesPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        propertiesPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(204, 204, 204)), "SFTP Settings", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        propertiesPanel.add(authenticationLabel);
        propertiesPanel.add(usePasswordRadio, "split 3");
        propertiesPanel.add(usePrivateKeyRadio);
        propertiesPanel.add(useBothRadio, "push, wrap");

        propertiesPanel.add(keyLocationLabel);
        propertiesPanel.add(keyLocationField, "w 200!, wrap");

        propertiesPanel.add(passphraseLabel);
        propertiesPanel.add(passphraseField, "w 125!, wrap");

        propertiesPanel.add(useKnownHostsLabel);
        propertiesPanel.add(useKnownHostsYesRadio, "split 3");
        propertiesPanel.add(useKnownHostsAskRadio);
        propertiesPanel.add(useKnownHostsNoRadio, "push, wrap");

        propertiesPanel.add(knownHostsLocationLabel);
        propertiesPanel.add(knownHostsField, "w 200!, wrap");

        propertiesPanel.add(configurationsLabel, "aligny top");
        propertiesPanel.add(configurationsScrollPane, "span, grow, split 2");

        JPanel configurationsButtonPanel = new JPanel(new MigLayout("insets 0, novisualpadding, hidemode 3, fill"));
        configurationsButtonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        configurationsButtonPanel.add(newButton, "w 50!, wrap");
        configurationsButtonPanel.add(deleteButton, "w 50!");

        propertiesPanel.add(configurationsButtonPanel, "top");

        add(propertiesPanel, "grow, push, top, wrap");

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0 8 8 8, novisualpadding, hidemode 3, fill"));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        buttonPanel.add(new JSeparator(), "growx, sx, wrap");
        buttonPanel.add(okButton, "newline, w 50!, sx, right, split");
        buttonPanel.add(cancelButton, "w 50!");

        add(buttonPanel, "south, span");
    }

    private void okCancelButtonActionPerformed(boolean saveProperties) {
        if (configurationsTable.isEditing()) {
            configurationsTable.getCellEditor().stopCellEditing();
        }

        if (saveProperties) {
            if (!validateProperties()) {
                return;
            }

            getProperties();
        } else {
            setFileSchemeProperties(fileSchemeProperties);
        }

        dispose();
    }

    private void authenticationRadioButtonActionPerformed() {
        boolean enabled = usePrivateKeyRadio.isSelected() || useBothRadio.isSelected();

        passphraseLabel.setEnabled(enabled);
        passphraseField.setEnabled(enabled);
        keyLocationLabel.setEnabled(enabled);
        keyLocationField.setEnabled(enabled);
    }

    class ConfigTableCellEditor extends TextFieldCellEditor {
        boolean checkProperties;

        public ConfigTableCellEditor(boolean checkProperties) {
            super();
            this.checkProperties = checkProperties;
        }

        public boolean checkUniqueProperty(String property) {
            boolean exists = false;

            for (int i = 0; i < configurationsTable.getRowCount(); i++) {
                if (configurationsTable.getValueAt(i, NAME_COLUMN) != null && ((String) configurationsTable.getValueAt(i, NAME_COLUMN)).equalsIgnoreCase(property)) {
                    exists = true;
                }
            }

            return exists;
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            boolean editable = super.isCellEditable(evt);

            if (editable) {
                deleteButton.setEnabled(false);
            }

            return editable;
        }

        @Override
        protected boolean valueChanged(String value) {
            deleteButton.setEnabled(true);

            if (checkProperties && (value.length() == 0 || checkUniqueProperty(value))) {
                return false;
            }

            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
            return true;
        }
    }

    private JLabel authenticationLabel;
    private MirthRadioButton usePasswordRadio;
    private MirthRadioButton usePrivateKeyRadio;
    private MirthRadioButton useBothRadio;
    private ButtonGroup privateKeyButtonGroup;

    private JLabel keyLocationLabel;
    private MirthTextField keyLocationField;

    private JLabel passphraseLabel;
    private MirthTextField passphraseField;

    private JLabel useKnownHostsLabel;
    private MirthRadioButton useKnownHostsYesRadio;
    private MirthRadioButton useKnownHostsAskRadio;
    private MirthRadioButton useKnownHostsNoRadio;
    private ButtonGroup knownHostsButtonGroup;
    private JLabel knownHostsLocationLabel;
    private MirthTextField knownHostsField;

    private JLabel configurationsLabel;
    private MirthTable configurationsTable;
    private JScrollPane configurationsScrollPane;
    private JButton newButton;
    private JButton deleteButton;

    private JButton okButton;
    private JButton cancelButton;
}