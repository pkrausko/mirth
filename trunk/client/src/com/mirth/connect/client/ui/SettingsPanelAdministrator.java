/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.components.KeyStrokeTextField;
import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.client.ui.components.MirthRadioButton;
import com.mirth.connect.client.ui.components.MirthTable;
import com.mirth.connect.client.ui.components.MirthTextField;
import com.mirth.connect.client.ui.components.rsta.MirthRSyntaxTextArea;
import com.mirth.connect.client.ui.components.rsta.RSTAPreferences;
import com.mirth.connect.client.ui.components.rsta.actions.ActionInfo;
import com.mirth.connect.model.User;

public class SettingsPanelAdministrator extends AbstractSettingsPanel {

    public static final String TAB_NAME = "Administrator";

    private static final int ACTION_INFO_COLUMN = 0;
    private static final int NAME_COLUMN = 1;
    private static final int DESCRIPTION_COLUMN = 2;
    private static final int KEY_COLUMN = 3;

    private static Preferences userPreferences;

    private User currentUser = getFrame().getCurrentUser(getFrame());
    private List<ActionInfo> shortcutKeyList;

    public SettingsPanelAdministrator(String tabName) {
        super(tabName);

        shortcutKeyList = new ArrayList<ActionInfo>();
        ResourceBundle resourceBundle = MirthRSyntaxTextArea.getResourceBundle();
        for (ActionInfo actionInfo : ActionInfo.values()) {
            if (!BooleanUtils.toBoolean(resourceBundle.getString(actionInfo.toString() + ".Toggle"))) {
                shortcutKeyList.add(actionInfo);
            }
        }

        initComponents();
        initLayout();
    }

    public void doRefresh() {
        if (getFrame().confirmLeave()) {
            dashboardRefreshIntervalField.setDocument(new MirthFieldConstraints(3, false, false, true));
            messageBrowserPageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
            eventBrowserPageSizeField.setDocument(new MirthFieldConstraints(3, false, false, true));
            userPreferences = Preferences.userNodeForPackage(Mirth.class);
            int interval = userPreferences.getInt("intervalTime", 10);
            dashboardRefreshIntervalField.setText(interval + "");

            int messageBrowserPageSize = userPreferences.getInt("messageBrowserPageSize", 20);
            messageBrowserPageSizeField.setText(messageBrowserPageSize + "");

            int eventBrowserPageSize = userPreferences.getInt("eventBrowserPageSize", 100);
            eventBrowserPageSizeField.setText(eventBrowserPageSize + "");

            if (userPreferences.getBoolean("messageBrowserFormatXml", true)) {
                formatXmlYesRadio.setSelected(true);
            } else {
                formatXmlNoRadio.setSelected(true);
            }

            if (userPreferences.getBoolean("textSearchWarning", true)) {
                textSearchWarningYesRadio.setSelected(true);
            } else {
                textSearchWarningNoRadio.setSelected(true);
            }

            final String workingId = getFrame().startWorking("Loading " + getTabName() + " settings...");

            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                private String checkForNotifications = null;

                public Void doInBackground() {
                    try {
                        checkForNotifications = getFrame().mirthClient.getUserPreference(currentUser, "checkForNotifications");
                    } catch (ClientException e) {
                        getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                    }
                    return null;
                }

                @Override
                public void done() {
                    if (checkForNotifications == null || BooleanUtils.toBoolean(checkForNotifications)) {
                        checkForNotificationsYesRadio.setSelected(true);
                    } else {
                        checkForNotificationsNoRadio.setSelected(true);
                    }
                    getFrame().stopWorking(workingId);
                }
            };

            worker.execute();

            updateShortcutKeyTable(MirthRSyntaxTextArea.getRSTAPreferences());
            updateRestoreDefaultsButton();
        }
    }

    public boolean doSave() {
        if (dashboardRefreshIntervalField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid interval time.");
            return false;
        }
        if (messageBrowserPageSizeField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid message browser page size.");
            return false;
        }
        if (eventBrowserPageSizeField.getText().length() == 0) {
            getFrame().alertWarning(this, "Please enter a valid event browser page size.");
            return false;
        }

        int interval = Integer.parseInt(dashboardRefreshIntervalField.getText());
        int messageBrowserPageSize = Integer.parseInt(messageBrowserPageSizeField.getText());
        int eventBrowserPageSize = Integer.parseInt(eventBrowserPageSizeField.getText());

        if (interval <= 0) {
            getFrame().alertWarning(this, "Please enter an interval time that is larger than 0.");
        } else if (messageBrowserPageSize <= 0) {
            getFrame().alertWarning(this, "Please enter an message browser page size larger than 0.");
        } else if (eventBrowserPageSize <= 0) {
            getFrame().alertWarning(this, "Please enter an event browser page size larger than 0.");
        } else {
            userPreferences.putInt("intervalTime", interval);
            userPreferences.putInt("messageBrowserPageSize", messageBrowserPageSize);
            userPreferences.putInt("eventBrowserPageSize", eventBrowserPageSize);
            userPreferences.putBoolean("messageBrowserFormatXml", formatXmlYesRadio.isSelected());
            userPreferences.putBoolean("textSearchWarning", textSearchWarningYesRadio.isSelected());
        }
        final String workingId = getFrame().startWorking("Saving " + getTabName() + " settings...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                try {
                    getFrame().mirthClient.setUserPreference(currentUser, "checkForNotifications", Boolean.toString(checkForNotificationsYesRadio.isSelected()));
                } catch (ClientException e) {
                    getFrame().alertException(getFrame(), e.getStackTrace(), e.getMessage());
                }

                return null;
            }

            @Override
            public void done() {
                getFrame().setSaveEnabled(false);
                getFrame().stopWorking(workingId);
            }
        };

        worker.execute();

        RSTAPreferences rstaPreferences = MirthRSyntaxTextArea.getRSTAPreferences();
        for (int row = 0; row < shortcutKeyTable.getRowCount(); row++) {
            ActionInfo actionInfo = (ActionInfo) shortcutKeyTable.getModel().getValueAt(row, ACTION_INFO_COLUMN);
            KeyStroke keyStroke = (KeyStroke) shortcutKeyTable.getModel().getValueAt(row, KEY_COLUMN);
            rstaPreferences.getKeyStrokeMap().put(actionInfo.getActionMapKey(), keyStroke);
        }
        MirthRSyntaxTextArea.updateRSTAPreferences(userPreferences);

        return true;
    }

    private void updateRestoreDefaultsButton() {
        boolean isDefault = true;
        Map<String, KeyStroke> defaultKeyStrokeMap = new RSTAPreferences().getKeyStrokeMap();

        for (int row = 0; row < shortcutKeyTable.getRowCount(); row++) {
            ActionInfo actionInfo = (ActionInfo) shortcutKeyTable.getModel().getValueAt(row, ACTION_INFO_COLUMN);
            KeyStroke keyStroke = (KeyStroke) shortcutKeyTable.getModel().getValueAt(row, KEY_COLUMN);

            if (!ObjectUtils.equals(keyStroke, defaultKeyStrokeMap.get(actionInfo.getActionMapKey()))) {
                isDefault = false;
                break;
            }
        }

        restoreDefaultsButton.setEnabled(!isDefault);
    }

    private void restoreDefaults() {
        if (PlatformUI.MIRTH_FRAME.alertOkCancel(PlatformUI.MIRTH_FRAME, "<html>This will reset all the code editor shortcut keys to their defaults.<br/>Are you sure you wish to continue?</html>")) {
            updateShortcutKeyTable(new RSTAPreferences());
            restoreDefaultsButton.setEnabled(false);
            PlatformUI.MIRTH_FRAME.setSaveEnabled(true);
        }
    }

    private void updateShortcutKeyTable(RSTAPreferences rstaPreferences) {
        ResourceBundle resourceBundle = MirthRSyntaxTextArea.getResourceBundle();
        Object[][] data = new Object[shortcutKeyList.size()][4];
        int i = 0;

        for (ActionInfo actionInfo : shortcutKeyList) {
            data[i][ACTION_INFO_COLUMN] = actionInfo;
            data[i][NAME_COLUMN] = resourceBundle.getString(actionInfo.toString() + ".Name");
            data[i][DESCRIPTION_COLUMN] = resourceBundle.getString(actionInfo.toString() + ".Desc");
            data[i][KEY_COLUMN] = rstaPreferences.getKeyStrokeMap().get(actionInfo.getActionMapKey());
            i++;
        }

        ((RefreshTableModel) shortcutKeyTable.getModel()).refreshDataVector(data);
    }

    private void initComponents() {
        setBackground(UIConstants.BACKGROUND_COLOR);

        systemSettingsPanel = new JPanel();
        systemSettingsPanel.setBackground(getBackground());
        systemSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "System Preferences", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        dashboardRefreshIntervalLabel = new JLabel("Dashboard refresh interval (seconds):");
        dashboardRefreshIntervalField = new MirthTextField();
        dashboardRefreshIntervalField.setToolTipText("<html>Interval in seconds at which to refresh the Dashboard. Decrement this for <br>faster updates, and increment it for slower servers with more channels.</html>");

        String toolTipText = "Sets the default page size for browsers (message, event, etc.)";
        messageBrowserPageSizeLabel = new JLabel("Message browser page size:");
        messageBrowserPageSizeField = new MirthTextField();
        messageBrowserPageSizeField.setToolTipText(toolTipText);

        eventBrowserPageSizeLabel = new JLabel("Event browser page size:");
        eventBrowserPageSizeField = new MirthTextField();
        eventBrowserPageSizeField.setToolTipText(toolTipText);

        formatXmlLabel = new JLabel("Format XML in message browser:");
        formatXmlButtonGroup = new ButtonGroup();

        toolTipText = "Pretty print messages in the message browser that are XML.";
        formatXmlYesRadio = new MirthRadioButton("Yes");
        formatXmlYesRadio.setBackground(systemSettingsPanel.getBackground());
        formatXmlYesRadio.setToolTipText(toolTipText);
        formatXmlButtonGroup.add(formatXmlYesRadio);

        formatXmlNoRadio = new MirthRadioButton("No");
        formatXmlNoRadio.setBackground(systemSettingsPanel.getBackground());
        formatXmlNoRadio.setToolTipText(toolTipText);
        formatXmlButtonGroup.add(formatXmlNoRadio);

        textSearchWarningLabel = new JLabel("Message browser text search confirmation:");
        textSearchWarningButtonGroup = new ButtonGroup();

        toolTipText = "<html>Show a confirmation dialog in the message browser when attempting a text search, warning users<br/>that the query may take a long time depending on the amount of messages being searched.</html>";
        textSearchWarningYesRadio = new MirthRadioButton("Yes");
        textSearchWarningYesRadio.setBackground(systemSettingsPanel.getBackground());
        textSearchWarningYesRadio.setToolTipText(toolTipText);
        textSearchWarningButtonGroup.add(textSearchWarningYesRadio);

        textSearchWarningNoRadio = new MirthRadioButton("No");
        textSearchWarningNoRadio.setBackground(systemSettingsPanel.getBackground());
        textSearchWarningNoRadio.setToolTipText(toolTipText);
        textSearchWarningButtonGroup.add(textSearchWarningNoRadio);

        userSettingsPanel = new JPanel();
        userSettingsPanel.setBackground(getBackground());
        userSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "User Preferences", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        checkForNotificationsLabel = new JLabel("Check for new notifications on login:");
        notificationButtonGroup = new ButtonGroup();

        checkForNotificationsYesRadio = new MirthRadioButton("Yes");
        checkForNotificationsYesRadio.setBackground(userSettingsPanel.getBackground());
        checkForNotificationsYesRadio.setToolTipText("<html>Checks for notifications from Mirth (announcements, available updates, etc.)<br/>relevant to this version of Mirth Connect whenever user logs in.</html>");
        notificationButtonGroup.add(checkForNotificationsYesRadio);

        checkForNotificationsNoRadio = new MirthRadioButton("No");
        checkForNotificationsNoRadio.setBackground(userSettingsPanel.getBackground());
        checkForNotificationsNoRadio.setToolTipText("<html>Checks for notifications from Mirth (announcements, available updates, etc.)<br/>relevant to this version of Mirth Connect whenever user logs in.</html>");
        notificationButtonGroup.add(checkForNotificationsNoRadio);

        codeEditorSettingsPanel = new JPanel();
        codeEditorSettingsPanel.setBackground(getBackground());
        codeEditorSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(204, 204, 204)), "Code Editor Preferences", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", 1, 11)));

        shortcutKeyTable = new MirthTable();
        shortcutKeyTable.setModel(new RefreshTableModel(new Object[] { "Action Info", "Name",
                "Description", "Shortcut Key Mapping" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == KEY_COLUMN;
            }
        });

        shortcutKeyTable.setDragEnabled(false);
        shortcutKeyTable.setRowSelectionAllowed(false);
        shortcutKeyTable.setRowHeight(UIConstants.ROW_HEIGHT);
        shortcutKeyTable.setFocusable(false);
        shortcutKeyTable.setOpaque(true);
        shortcutKeyTable.getTableHeader().setReorderingAllowed(false);
        shortcutKeyTable.setSortable(false);

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            shortcutKeyTable.setHighlighters(HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR));
        }

        shortcutKeyTable.getColumnModel().getColumn(NAME_COLUMN).setMinWidth(145);
        shortcutKeyTable.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(145);

        shortcutKeyTable.getColumnModel().getColumn(DESCRIPTION_COLUMN).setPreferredWidth(600);

        shortcutKeyTable.getColumnModel().getColumn(KEY_COLUMN).setMinWidth(120);
        shortcutKeyTable.getColumnModel().getColumn(KEY_COLUMN).setPreferredWidth(150);
        shortcutKeyTable.getColumnModel().getColumn(KEY_COLUMN).setCellRenderer(new KeyStrokeCellRenderer());
        shortcutKeyTable.getColumnModel().getColumn(KEY_COLUMN).setCellEditor(new KeyStrokeCellEditor());

        shortcutKeyTable.removeColumn(shortcutKeyTable.getColumnModel().getColumn(ACTION_INFO_COLUMN));

        shortcutKeyTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent evt) {
                updateRestoreDefaultsButton();
            }
        });

        shortcutKeyScrollPane = new JScrollPane(shortcutKeyTable);

        restoreDefaultsButton = new JButton("Restore Defaults");
        restoreDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                restoreDefaults();
            }
        });
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 12, novisualpadding, hidemode 3, fill, gap 6 6", "", "[][][][grow]"));

        systemSettingsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6 6", "12[]13[][grow]", ""));
        systemSettingsPanel.add(dashboardRefreshIntervalLabel, "right");
        systemSettingsPanel.add(dashboardRefreshIntervalField, "w 30!");
        systemSettingsPanel.add(messageBrowserPageSizeLabel, "newline, right");
        systemSettingsPanel.add(messageBrowserPageSizeField, "w 30!");
        systemSettingsPanel.add(eventBrowserPageSizeLabel, "newline, right");
        systemSettingsPanel.add(eventBrowserPageSizeField, "w 30!");
        systemSettingsPanel.add(formatXmlLabel, "newline, right");
        systemSettingsPanel.add(formatXmlYesRadio, "split");
        systemSettingsPanel.add(formatXmlNoRadio);
        systemSettingsPanel.add(textSearchWarningLabel, "newline, right");
        systemSettingsPanel.add(textSearchWarningYesRadio, "split");
        systemSettingsPanel.add(textSearchWarningNoRadio);
        add(systemSettingsPanel, "grow");

        userSettingsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6 6", "12[]13[][grow]", ""));
        userSettingsPanel.add(checkForNotificationsLabel, "right");
        userSettingsPanel.add(checkForNotificationsYesRadio, "split");
        userSettingsPanel.add(checkForNotificationsNoRadio);
        add(userSettingsPanel, "newline, grow");

        codeEditorSettingsPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 6 6", "12[][]", ""));
        codeEditorSettingsPanel.add(shortcutKeyScrollPane, "grow, push, h ::179");
        codeEditorSettingsPanel.add(restoreDefaultsButton, "top");
        add(codeEditorSettingsPanel, "newline, grow");
    }

    private class KeyStrokeCellRenderer extends KeyStrokeTextField implements TableCellRenderer {

        public KeyStrokeCellRenderer() {
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setKeyStroke((KeyStroke) value);
            return this;
        }
    }

    private class KeyStrokeCellEditor extends DefaultCellEditor {

        private KeyStrokeTextField textField;

        public KeyStrokeCellEditor() {
            super(new KeyStrokeTextField());
            textField = (KeyStrokeTextField) getComponent();
        }

        @Override
        public Object getCellEditorValue() {
            return textField.getKeyStroke();
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (evt != null && evt instanceof MouseEvent) {
                return ((MouseEvent) evt).getClickCount() >= 2;
            }
            return false;
        }

        @Override
        public boolean stopCellEditing() {
            KeyStroke keyStroke = (KeyStroke) getCellEditorValue();
            if (keyStroke != null) {
                for (int row = 0; row < shortcutKeyTable.getRowCount(); row++) {
                    if (keyStroke.equals((KeyStroke) shortcutKeyTable.getModel().getValueAt(row, KEY_COLUMN))) {
                        cancelCellEditing();
                    }
                }
            }

            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textField.setKeyStroke((KeyStroke) value);
            return textField;
        }
    }

    private JPanel systemSettingsPanel;
    private JLabel dashboardRefreshIntervalLabel;
    private JTextField dashboardRefreshIntervalField;
    private JLabel messageBrowserPageSizeLabel;
    private JTextField messageBrowserPageSizeField;
    private JLabel eventBrowserPageSizeLabel;
    private JTextField eventBrowserPageSizeField;
    private JLabel formatXmlLabel;
    private ButtonGroup formatXmlButtonGroup;
    private JRadioButton formatXmlYesRadio;
    private JRadioButton formatXmlNoRadio;
    private JLabel textSearchWarningLabel;
    private ButtonGroup textSearchWarningButtonGroup;
    private JRadioButton textSearchWarningYesRadio;
    private JRadioButton textSearchWarningNoRadio;
    private JPanel userSettingsPanel;
    private JLabel checkForNotificationsLabel;
    private ButtonGroup notificationButtonGroup;
    private JRadioButton checkForNotificationsYesRadio;
    private JRadioButton checkForNotificationsNoRadio;
    private JPanel codeEditorSettingsPanel;
    private JScrollPane shortcutKeyScrollPane;
    private MirthTable shortcutKeyTable;
    private JButton restoreDefaultsButton;
}