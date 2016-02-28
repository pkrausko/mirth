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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.client.ui.ChannelFilter.ChannelFilterSaveTask;
import com.mirth.connect.client.ui.components.IconButton;
import com.mirth.connect.client.ui.components.IconToggleButton;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.plugins.DashboardColumnPlugin;
import com.mirth.connect.plugins.DashboardPanelPlugin;
import com.mirth.connect.plugins.DashboardTabPlugin;
import com.mirth.connect.plugins.DashboardTablePlugin;

public class DashboardPanel extends JPanel {

    private static final String STATUS_COLUMN_NAME = "Status";
    private static final String NAME_COLUMN_NAME = "Name";
    private static final String RECEIVED_COLUMN_NAME = "Received";
    private static final String QUEUED_COLUMN_NAME = "Queued";
    private static final String SENT_COLUMN_NAME = "Sent";
    private static final String ERROR_COLUMN_NAME = "Errored";
    private static final String FILTERED_COLUMN_NAME = "Filtered";
    private static final String LAST_DEPLOYED_COLUMN_NAME = "Last Deployed";
    private static final String DEPLOYED_REVISION_DELTA_COLUMN_NAME = "Rev \u0394";
    private static final String[] defaultColumns = new String[] { STATUS_COLUMN_NAME,
            NAME_COLUMN_NAME, DEPLOYED_REVISION_DELTA_COLUMN_NAME, LAST_DEPLOYED_COLUMN_NAME,
            RECEIVED_COLUMN_NAME, FILTERED_COLUMN_NAME, QUEUED_COLUMN_NAME, SENT_COLUMN_NAME,
            ERROR_COLUMN_NAME };

    private Frame parent;
    private boolean showLifetimeStats = false;

    private JMenuItem menuItem;
    private Set<String> defaultVisibleColumns;
    private Set<DeployedState> haltableStates = new HashSet<DeployedState>();

    public DashboardPanel() {
        this.parent = PlatformUI.MIRTH_FRAME;

        haltableStates.add(DeployedState.DEPLOYING);
        haltableStates.add(DeployedState.UNDEPLOYING);
        haltableStates.add(DeployedState.STARTING);
        haltableStates.add(DeployedState.STOPPING);
        haltableStates.add(DeployedState.PAUSING);
        haltableStates.add(DeployedState.SYNCING);
        haltableStates.add(DeployedState.UNKNOWN);

        initComponents();
        initLayout();

        loadTabPlugins();
        ChangeListener changeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
                int index = sourceTabbedPane.getSelectedIndex();

                if (LoadedExtensions.getInstance().getDashboardTabPlugins().size() > 0) {
                    loadPanelPlugin(LoadedExtensions.getInstance().getDashboardTabPlugins().get(sourceTabbedPane.getTitleAt(index)));
                }
            }
        };
        tabPane.addChangeListener(changeListener);

        defaultVisibleColumns = new LinkedHashSet<String>();

        makeStatusTable();
        loadTablePlugins();

        this.setDoubleBuffered(true);

        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();

        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("dashboardGroupViewEnabled", true)) {
            tableModeGroupsButton.setSelected(true);
            tableModeGroupsButton.setContentFilled(true);
            tableModeChannelsButton.setContentFilled(false);
            model.setGroupModeEnabled(true);
        } else {
            tableModeChannelsButton.setSelected(true);
            tableModeChannelsButton.setContentFilled(true);
            tableModeGroupsButton.setContentFilled(false);
            model.setGroupModeEnabled(false);
        }
    }

    public void loadTabPlugins() {
        if (LoadedExtensions.getInstance().getDashboardTabPlugins().size() > 0) {
            for (DashboardTabPlugin plugin : LoadedExtensions.getInstance().getDashboardTabPlugins().values()) {
                if (plugin.getTabComponent() != null) {
                    tabPane.addTab(plugin.getPluginPointName(), plugin.getTabComponent());
                }
            }

            splitPane.setBottomComponent(tabPane);
            splitPane.setDividerSize(6);
            splitPane.setDividerLocation(3 * Preferences.userNodeForPackage(Mirth.class).getInt("height", UIConstants.MIRTH_HEIGHT) / 5);
            splitPane.setResizeWeight(0.5);
        }
    }

    private void loadTablePlugins() {
        pluginContainerPanel.setLayout(new MigLayout("fillx, insets 0 0 0 0", "[grow,fill]", "[grow,fill]"));

        for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
            for (JComponent component : plugin.getToolbarComponents(dashboardTable)) {
                pluginContainerPanel.add(component, "grow");
            }
        }

        for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
            plugin.onDashboardInit(dashboardTable);
        }

        repaint();
    }

    private void loadPanelPlugin(final DashboardPanelPlugin plugin) {
        final List<DashboardStatus> selectedStatuses = getSelectedStatuses();

        QueuingSwingWorkerTask<Void, Void> task = new QueuingSwingWorkerTask<Void, Void>(plugin.getPluginPointName(), "Updating " + plugin.getPluginPointName() + " dashboard plugin...") {
            @Override
            public Void doInBackground() {
                try {
                    if (selectedStatuses.size() != 0) {
                        plugin.prepareData(selectedStatuses);
                    } else {
                        plugin.prepareData();
                    }
                } catch (ClientException e) {
                    parent.alertThrowable(parent, e);
                }
                return null;
            }

            @Override
            public void done() {
                if (selectedStatuses.size() != 0) {
                    plugin.update(selectedStatuses);
                } else {
                    plugin.update();
                }
            }
        };

        new QueuingSwingWorker<Void, Void>(task, true).executeDelegate();
    }

    private DashboardTabPlugin getCurrentTabPlugin() {
        return LoadedExtensions.getInstance().getDashboardTabPlugins().get(tabPane.getTitleAt(tabPane.getSelectedIndex()));
    }

    /**
     * Makes the status table with all current server information.
     */
    public void makeStatusTable() {
        List<String> columns = new ArrayList<String>();

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        columns.addAll(Arrays.asList(defaultColumns));

        for (DashboardColumnPlugin plugin : LoadedExtensions.getInstance().getDashboardColumnPlugins().values()) {
            if (!plugin.isDisplayFirst()) {
                columns.add(plugin.getColumnHeader());
            }
        }

        DashboardTreeTableModel model = new DashboardTreeTableModel();
        model.setColumnIdentifiers(columns);
        model.setNodeFactory(new DefaultDashboardTableNodeFactory());

        for (DashboardTablePlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
            dashboardTable = plugin.getTable();

            if (dashboardTable != null) {
                break;
            }
        }

        defaultVisibleColumns.addAll(columns);

        if (dashboardTable == null) {
            dashboardTable = new MirthTreeTable("dashboardPanel", defaultVisibleColumns);
        }

        dashboardTable.setColumnFactory(new DashboardTableColumnFactory());
        dashboardTable.setTreeTableModel(model);
        dashboardTable.setDoubleBuffered(true);
        dashboardTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dashboardTable.setHorizontalScrollEnabled(true);
        dashboardTable.packTable(UIConstants.COL_MARGIN);
        dashboardTable.setRowHeight(UIConstants.ROW_HEIGHT);
        dashboardTable.setOpaque(true);
        dashboardTable.setRowSelectionAllowed(true);
        dashboardTable.setSortable(true);
        dashboardTable.putClientProperty("JTree.lineStyle", "Horizontal");
        dashboardTable.setAutoCreateColumnsFromModel(false);
        dashboardTable.setShowGrid(true, true);
        dashboardTable.restoreColumnPreferences();
        dashboardTable.setMirthColumnControlEnabled(true);

        dashboardTable.setTreeCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

                TreePath path = dashboardTable.getPathForRow(row);
                if (path != null) {
                    AbstractDashboardTableNode node = ((AbstractDashboardTableNode) path.getLastPathComponent());
                    if (node.isGroupNode()) {
                        setIcon(UIConstants.ICON_GROUP);
                    } else {
                        DashboardStatus status = node.getDashboardStatus();
                        if (status.getStatusType() == StatusType.CHANNEL) {
                            setIcon(UIConstants.ICON_CHANNEL);
                        } else {
                            setIcon(UIConstants.ICON_CONNECTOR);
                        }
                    }
                }

                return label;
            }
        });
        dashboardTable.setLeafIcon(UIConstants.ICON_CONNECTOR);
        dashboardTable.setOpenIcon(UIConstants.ICON_CHANNEL);
        dashboardTable.setClosedIcon(UIConstants.ICON_CHANNEL);

        dashboardTableScrollPane.setViewportView(dashboardTable);

        dashboardTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                checkSelectionAndPopupMenu(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                checkSelectionAndPopupMenu(event);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                int clickedRow = dashboardTable.rowAtPoint(new Point(event.getX(), event.getY()));
                if (clickedRow == -1) {
                    return;
                }

                TreePath path = dashboardTable.getPathForRow(clickedRow);
                if (path != null && ((AbstractDashboardTableNode) path.getLastPathComponent()).isGroupNode()) {
                    return;
                }

                if (event.getClickCount() >= 2 && dashboardTable.getSelectedRowCount() == 1 && dashboardTable.getSelectedRow() == clickedRow) {
                    parent.doShowMessages();
                }
            }
        });

        dashboardTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                /*
                 * MIRTH-3199: Only update the panel plugin if the selection is finished. This does
                 * mean that the logs aren't updated live when adding to or removing from a
                 * currently adjusting selection, but it's much more efficient when it comes to the
                 * number of requests being done from the client. Plus, it actually will still
                 * update while a selection is adjusting if the refresh interval on the dashboard
                 * elapses. We can change this so that plugins are updated during a selection
                 * adjustment, but it would first require a major rewrite of the connection log /
                 * status column plugin.
                 */
                updatePopupMenu(!event.getValueIsAdjusting());
            }
        });
    }

    /**
     * Shows the popup menu when the trigger button (right-click) has been pushed. Deselects the
     * rows if no row was selected.
     */
    private void checkSelectionAndPopupMenu(MouseEvent event) {
        TreePath path = dashboardTable.getPathForLocation(event.getX(), event.getY());

        /*
         * On mouse events we don't need to update the dashboard panel plugins. They will already
         * have been updated because of the ListSelectionEvent, and multiple mouse events will enter
         * here (as many as three, one pressed and two released) so we would basically be doing four
         * times the work.
         */
        if (path == null) {
            deselectRows(false);
        } else {
            updatePopupMenu(false);
        }

        if (event.isPopupTrigger()) {
            TreeSelectionModel selectionModel = dashboardTable.getTreeSelectionModel();

            if (!selectionModel.isPathSelected(path)) {
                deselectRows(false);
                selectionModel.addSelectionPath(path);
            }

            parent.dashboardPopupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    /*
     * Action when something on the status list has been selected. Sets all appropriate tasks
     * visible.
     */
    private void updatePopupMenu(boolean loadPanelPlugin) {
        // @formatter:off
        /*
         * 0 - Refresh
         * 1 - Send Message
         * 2 - View Messages
         * 3 - Remove All Messages
         * 4 - Clear Statistics
         * 5 - Start
         * 6 - Pause
         * 7 - Stop
         * 8 - Halt
         * 9 - Undeploy Channel
         * 10 - Start Connector
         * 11 - Stop Connector
         */
        // @formatter:on

        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, -1, false); // hide all
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 2, 2, true); // show "View Messages"
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 4, 4, true); // show "Clear Statistics"

        // Indicates if any channel nodes are selected.
        boolean useChannelOptions = true;
        // Indicates if only nodes for a single channel is selected. This is true even if only a connector node is selected.
        boolean singleChannel = true;
        // Stores all channel ids that are selected, even if only a channel's connector is selected
        Set<String> selectedChannelIds = new HashSet<String>();
        // Stores all channel ids that have their channel node selected.
        Set<String> selectedChannelNodes = new HashSet<String>();

        List<AbstractDashboardTableNode> selectedNodes = dashboardTable.getSelectedNodes();

        if (selectedNodes.size() > 0) {
            for (AbstractDashboardTableNode node : selectedNodes) {
                if (!node.isGroupNode()) {
                    selectedChannelIds.add(node.getChannelId());
                    if (node.getDashboardStatus().getStatusType() == StatusType.CHANNEL) {
                        selectedChannelNodes.add(node.getChannelId());
                    } else if (!selectedChannelNodes.contains(node.getChannelId())) {
                        useChannelOptions = false;
                    }
                }

                if (selectedChannelIds.size() > 1) {
                    singleChannel = false;
                }
            }
        } else {
            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 2, 2, false);
            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 4, 4, false);
        }

        for (AbstractDashboardTableNode node : selectedNodes) {
            if (!node.isGroupNode()) {
                DashboardStatus status = node.getDashboardStatus();
                StatusType statusType = status.getStatusType();

                if (useChannelOptions) {
                    if (statusType == StatusType.CHANNEL) {

                        switch (status.getState()) {
                            case STARTED:
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 6, 7, true);
                                break;
                            case PAUSED:
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 5, true);
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 7, 7, true);
                                break;
                            case STOPPED:
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 5, true);
                                break;
                            default:
                                break;
                        }

                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, 1, true);
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 3, 3, true);
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 9, 9, true);

                        if (isHaltable(node)) {
                            // Hide tasks that can not be performed on a channel that is haltable
                            if (status.getState() == DeployedState.DEPLOYING || status.getState() == DeployedState.UNDEPLOYING) {
                                // If the channel is still deploying or undeploying, don't show anything except refresh
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, 9, false);
                            } else {
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 3, 3, false);
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 5, 9, false);
                            }

                            if (singleChannel) {
                                // Show the halt task only if a single channel is selected
                                parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 8, 8, true);
                            } else {
                                break;
                            }
                        }
                    }
                } else if (selectedChannelNodes.size() == 0) {
                    AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) node.getParent();
                    if (channelNode.getDashboardStatus().getState() != DeployedState.STARTED && channelNode.getDashboardStatus().getState() != DeployedState.PAUSED) {
                        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 10, 11, false);
                        break;
                    }

                    switch (status.getState()) {
                        case STARTED:
                            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 11, 11, true);
                            break;
                        case STOPPED:
                            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 10, 10, true);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (showLifetimeStats) {
            parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 4, 4, false);
        }

        if (loadPanelPlugin) {
            for (DashboardPanelPlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
                loadPanelPlugin(plugin);
            }

            loadPanelPlugin(getCurrentTabPlugin());
        }
    }

    /**
     * Checks if this node or any of its children are in a haltable state
     * 
     * @param node
     * @return
     */
    private boolean isHaltable(AbstractDashboardTableNode node) {
        DeployedState nodeState = node.getDashboardStatus().getState();

        if (haltableStates.contains(nodeState)) {
            return true;
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (isHaltable((AbstractDashboardTableNode) node.getChildAt(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    public synchronized void updateTableChannelNodes(List<DashboardStatus> intermediateStatuses) {
        ChannelTagInfo channelTagInfo = parent.getChannelTagInfo(true);

        if (channelTagInfo.isEnabled()) {
            List<DashboardStatus> filteredStatuses = new ArrayList<DashboardStatus>();

            for (DashboardStatus currentStatus : intermediateStatuses) {
                if (channelTagInfo.isEnabled() && CollectionUtils.containsAny(channelTagInfo.getVisibleTags(), currentStatus.getTags())) {
                    filteredStatuses.add(currentStatus);
                }
            }

            intermediateStatuses = filteredStatuses;
        }

        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        model.setStatuses(intermediateStatuses);
        model.setShowLifetimeStats(showLifetimeStatsButton.isSelected());

        updateTableHighlighting();
    }

    public synchronized void finishUpdatingTable(List<DashboardStatus> finishedStatuses, Collection<ChannelGroupStatus> channelGroupStatuses) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        ChannelTagInfo channelTagInfo = parent.getChannelTagInfo(true);

        int totalChannelCount = finishedStatuses.size();
        int totalGroupCount = channelGroupStatuses.size();
        List<DashboardStatus> filteredDashboardStatuses = new ArrayList<DashboardStatus>();
        List<ChannelGroupStatus> filteredGroupStatuses = new ArrayList<ChannelGroupStatus>();

        if (channelTagInfo.isEnabled()) {
            for (DashboardStatus currentStatus : finishedStatuses) {
                if (channelTagInfo.isEnabled() && CollectionUtils.containsAny(channelTagInfo.getVisibleTags(), currentStatus.getTags())) {
                    filteredDashboardStatuses.add(currentStatus);
                }
            }

            finishedStatuses = filteredDashboardStatuses;
        }

        model.finishStatuses(finishedStatuses);
        model.setShowLifetimeStats(showLifetimeStatsButton.isSelected());

        // The ListSelectionListener is not notified that the tree table model has changed so we must update the menu items manually.
        // If we switch everything to use a TreeSelectionListener then we should remove this.
        if (dashboardTable.getSelectedRowCount() == 0) {
            deselectRows(true);
        } else {
            updatePopupMenu(true);
        }
        updateTableHighlighting();

        if (channelTagInfo.isEnabled()) {
            Map<String, DashboardStatus> statusMap = new HashMap<String, DashboardStatus>();
            for (DashboardStatus status : parent.status) {
                statusMap.put(status.getChannelId(), status);
            }

            for (ChannelGroupStatus groupStatus : channelGroupStatuses) {
                ChannelGroup group = groupStatus.getGroup();

                boolean addGroupStatus = false;
                for (Channel channel : group.getChannels()) {
                    DashboardStatus dashboardStatus = statusMap.get(channel.getId());

                    if (dashboardStatus != null && CollectionUtils.containsAny(channelTagInfo.getVisibleTags(), dashboardStatus.getTags())) {
                        addGroupStatus = true;
                        break;
                    }
                }

                if (addGroupStatus) {
                    filteredGroupStatuses.add(groupStatus);
                }
            }

            model.setGroupStatuses(filteredGroupStatuses);
        } else {
            model.setGroupStatuses(channelGroupStatuses);
        }

        updateTagsLabel(totalGroupCount, filteredGroupStatuses.size(), totalChannelCount, filteredDashboardStatuses.size());
    }

    private void updateTagsLabel(int totalGroupCount, int visibleGroupCount, int totalChannelCount, int visibleChannelCount) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        ChannelTagInfo channelTagInfo = parent.getChannelTagInfo(true);

        if (channelTagInfo.isEnabled()) {
            StringBuilder builder = new StringBuilder();

            if (model.isGroupModeEnabled()) {
                if (totalGroupCount == visibleGroupCount) {
                    builder.append(totalGroupCount);
                } else {
                    builder.append(visibleGroupCount).append(" of ").append(totalGroupCount);
                }

                builder.append(" Group");
                if (totalGroupCount != 1) {
                    builder.append('s');
                }

                if (totalGroupCount != visibleGroupCount) {
                    builder.append(" (").append(totalGroupCount - visibleGroupCount).append(" filtered)");
                }
                builder.append(", ");
            }

            if (totalChannelCount == visibleChannelCount) {
                builder.append(totalChannelCount);
            } else {
                builder.append(visibleChannelCount).append(" of ").append(totalChannelCount);
            }

            builder.append(" Deployed Channel");
            if (totalChannelCount != 1) {
                builder.append('s');
            }

            if (totalChannelCount != visibleChannelCount) {
                builder.append(" (").append(totalChannelCount - visibleChannelCount).append(" filtered)");
            }

            builder.append(" (");
            for (Iterator<String> it = channelTagInfo.getVisibleTags().iterator(); it.hasNext();) {
                builder.append(it.next());
                if (it.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(')');

            tagsLabel.setText(builder.toString());
        } else if (model.isGroupModeEnabled()) {
            tagsLabel.setText(totalGroupCount + " Groups, " + totalChannelCount + " Deployed Channels");
        } else {
            tagsLabel.setText(totalChannelCount + " Deployed Channels");
        }
    }

    public void updateTableState(TableState tableState) {
        restoreTableState(tableState);
        updatePopupMenu(false);
    }

    public synchronized void updateTableHighlighting() {
        // MIRTH-2301
        // Since we are using addHighlighter here instead of using setHighlighters, we need to remove the old ones first.
        dashboardTable.setHighlighters();

        // Add the highlighters. Always add the error highlighter.
        if (Preferences.userNodeForPackage(Mirth.class).getBoolean("highlightRows", true)) {
            Highlighter highlighter = HighlighterFactory.createAlternateStriping(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR);
            dashboardTable.addHighlighter(highlighter);
        }

        HighlightPredicate queuedHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == dashboardTable.getColumnViewIndex(QUEUED_COLUMN_NAME)) {
                    Long value = (Long) dashboardTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value.longValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };

        dashboardTable.addHighlighter(new ColorHighlighter(queuedHighlighterPredicate, new Color(240, 230, 140), Color.BLACK, new Color(240, 230, 140), Color.BLACK));

        HighlightPredicate errorHighlighterPredicate = new HighlightPredicate() {

            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == dashboardTable.getColumnViewIndex(ERROR_COLUMN_NAME)) {
                    Long value = (Long) dashboardTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value.longValue() > 0) {
                        return true;
                    }
                }
                return false;
            }
        };

        Highlighter errorHighlighter = new ColorHighlighter(errorHighlighterPredicate, Color.PINK, Color.BLACK, Color.PINK, Color.BLACK);
        dashboardTable.addHighlighter(errorHighlighter);

        HighlightPredicate revisionDeltaHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == dashboardTable.getColumnViewIndex(DEPLOYED_REVISION_DELTA_COLUMN_NAME)) {
                    Integer value = (Integer) dashboardTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value.intValue() > 0) {
                        return true;
                    }

                    TreePath path = dashboardTable.getPathForRow(adapter.row);
                    if (path != null) {
                        AbstractDashboardTableNode dashboardTableNode = (AbstractDashboardTableNode) path.getLastPathComponent();
                        if (!dashboardTableNode.isGroupNode()) {
                            DashboardStatus status = dashboardTableNode.getDashboardStatus();
                            if (status.getCodeTemplatesChanged() != null && status.getCodeTemplatesChanged()) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        };

        dashboardTable.addHighlighter(new ColorHighlighter(revisionDeltaHighlighterPredicate, new Color(255, 204, 0), Color.BLACK, new Color(255, 204, 0), Color.BLACK));

        HighlightPredicate lastDeployedHighlighterPredicate = new HighlightPredicate() {
            public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
                if (adapter.column == dashboardTable.getColumnViewIndex(LAST_DEPLOYED_COLUMN_NAME)) {
                    Calendar checkAfter = Calendar.getInstance();
                    checkAfter.add(Calendar.MINUTE, -2);

                    Object value = dashboardTable.getValueAt(adapter.row, adapter.column);

                    if (value != null && value instanceof Calendar && ((Calendar) value).after(checkAfter)) {
                        return true;
                    }
                }
                return false;
            }
        };

        dashboardTable.addHighlighter(new ColorHighlighter(lastDeployedHighlighterPredicate, new Color(240, 230, 140), Color.BLACK, new Color(240, 230, 140), Color.BLACK));
    }

    /**
     * Gets the index of the selected status row.
     */
    public synchronized List<DashboardStatus> getSelectedStatuses() {
        List<DashboardStatus> selectedStatuses = new ArrayList<DashboardStatus>();
        List<AbstractDashboardTableNode> selectedNodes = dashboardTable.getSelectedNodes();

        for (AbstractDashboardTableNode node : selectedNodes) {
            if (!node.isGroupNode()) {
                selectedStatuses.add(node.getDashboardStatus());
            }
        }

        return selectedStatuses;
    }

    public synchronized Set<DashboardStatus> getSelectedChannelStatuses() {
        Set<DashboardStatus> selectedStatuses = new HashSet<DashboardStatus>();
        List<AbstractDashboardTableNode> selectedNodes = dashboardTable.getSelectedNodes();

        for (TreeTableNode treeNode : selectedNodes) {
            while (treeNode != null && treeNode instanceof AbstractDashboardTableNode) {
                AbstractDashboardTableNode node = (AbstractDashboardTableNode) treeNode;
                if (!node.isGroupNode() && node.getDashboardStatus().getStatusType() == StatusType.CHANNEL) {
                    if (!selectedStatuses.contains(node.getDashboardStatus())) {
                        selectedStatuses.add(node.getDashboardStatus());
                    }

                    break;
                }

                treeNode = treeNode.getParent();
            }
        }

        return selectedStatuses;
    }

    public synchronized List<DashboardStatus> getSelectedStatusesRecursive() {
        List<DashboardStatus> selectedStatuses = new ArrayList<DashboardStatus>();
        List<AbstractDashboardTableNode> selectedNodes = dashboardTable.getSelectedNodes();

        for (AbstractDashboardTableNode node : selectedNodes) {
            if (!node.isGroupNode()) {
                selectedStatuses.add(node.getDashboardStatus());
                selectedStatuses.addAll(getAllChildStatuses(node.getDashboardStatus()));
            }
        }

        return selectedStatuses;
    }

    public Set<DashboardStatus> getAllChildStatuses(DashboardStatus status) {
        Set<DashboardStatus> statuses = new HashSet<DashboardStatus>();

        for (DashboardStatus childStatus : status.getChildStatuses()) {
            if (!statuses.contains(childStatus)) {
                statuses.add(childStatus);
            }

            statuses.addAll(getAllChildStatuses(childStatus));
        }

        return statuses;
    }

    public Map<Integer, String> getDestinationConnectorNames(String channelId) {
        Map<Integer, String> destinationConnectors = new LinkedHashMap<Integer, String>();
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        TreeTableNode root = model.getRoot();
        int channelCount = model.getChildCount(root);

        for (int i = 0; i < channelCount; i++) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) root.getChildAt(i);

            if (node.isGroupNode()) {
                for (Enumeration<? extends MutableTreeTableNode> channelNodes = node.children(); channelNodes.hasMoreElements();) {
                    populateDestinationConnectorNames(channelId, (AbstractDashboardTableNode) channelNodes.nextElement(), destinationConnectors);
                }
            } else {
                populateDestinationConnectorNames(channelId, node, destinationConnectors);
            }
        }

        return destinationConnectors;
    }

    private void populateDestinationConnectorNames(String channelId, AbstractDashboardTableNode channelNode, Map<Integer, String> destinationConnectors) {
        if (channelNode.getDashboardStatus().getChannelId() == channelId) {
            int connectorCount = channelNode.getChildCount();

            for (int j = 0; j < connectorCount; j++) {
                AbstractDashboardTableNode connectorNode = (AbstractDashboardTableNode) channelNode.getChildAt(j);
                DashboardStatus status = connectorNode.getDashboardStatus();
                Integer metaDataId = status.getMetaDataId();

                if (metaDataId > 0) {
                    destinationConnectors.put(metaDataId, status.getName());
                }
            }
        }
    }

    public void deselectRows(boolean loadPanelPlugin) {
        dashboardTable.clearSelection();
        parent.setVisibleTasks(parent.dashboardTasks, parent.dashboardPopupMenu, 1, -1, false);

        if (loadPanelPlugin) {
            loadPanelPlugin(getCurrentTabPlugin());

            for (DashboardPanelPlugin plugin : LoadedExtensions.getInstance().getDashboardTablePlugins().values()) {
                loadPanelPlugin(plugin);
            }
        }
    }

    public static int getNumberOfDefaultColumns() {
        return defaultColumns.length;
    }

    private static class DefaultDashboardTableNodeFactory implements DashboardTableNodeFactory {
        @Override
        public AbstractDashboardTableNode createNode(ChannelGroupStatus groupStatus) {
            return new DashboardTableNode(groupStatus);
        }

        @Override
        public AbstractDashboardTableNode createNode(String channelId, DashboardStatus status) {
            return new DashboardTableNode(channelId, status);
        }
    }

    private void initComponents() {
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setOneTouchExpandable(true);

        topPanel = new JPanel();
        topPanel.setBackground(UIConstants.BACKGROUND_COLOR);

        dashboardTable = null;
        dashboardTableScrollPane = new JScrollPane();
        dashboardTableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dashboardTableScrollPane.setViewportView(dashboardTable);
        dashboardTableScrollPane.setDoubleBuffered(true);

        controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(164, 164, 164)));
        controlPanel.setPreferredSize(new Dimension(100, 20));

        tagFilterButton = new IconButton();
        tagFilterButton.setIcon(new ImageIcon(getClass().getResource("/com/mirth/connect/client/ui/images/wrench.png"))); // NOI18N
        tagFilterButton.setToolTipText("Show Channel Filter");
        tagFilterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tagFilterButtonActionPerformed();
            }
        });

        tagsLabel = new JLabel();

        showLabel = new JLabel("Show:");
        ButtonGroup showStatsButtonGroup = new ButtonGroup();

        showCurrentStatsButton = new JRadioButton("Current Statistics");
        showCurrentStatsButton.setSelected(true);
        showCurrentStatsButton.setToolTipText("Show the statistics accumulated since the last time the statistics were reset");
        showCurrentStatsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showCurrentStatsButtonActionPerformed();
            }
        });
        showStatsButtonGroup.add(showCurrentStatsButton);

        showLifetimeStatsButton = new JRadioButton("Lifetime Statistics");
        showLifetimeStatsButton.setToolTipText("Show the statistics accumulated over the entire lifetime of the channel");
        showLifetimeStatsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showLifetimeStatsButtonActionPerformed();
            }
        });
        showStatsButtonGroup.add(showLifetimeStatsButton);

        pluginContainerPanel = new JPanel();

        tableModeLabel = new JLabel("Table View:");
        ButtonGroup tableModeButtonGroup = new ButtonGroup();

        tableModeGroupsButton = new IconToggleButton(UIConstants.ICON_GROUP);
        tableModeGroupsButton.setToolTipText("Groups");
        tableModeGroupsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                switchTableMode(true);
            }
        });
        tableModeButtonGroup.add(tableModeGroupsButton);

        tableModeChannelsButton = new IconToggleButton(UIConstants.ICON_CHANNEL);
        tableModeChannelsButton.setToolTipText("Channels");
        tableModeChannelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                switchTableMode(false);
            }
        });
        tableModeButtonGroup.add(tableModeChannelsButton);

        tabPane = new JTabbedPane();

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(tabPane);
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0"));

        topPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3, fill, gap 0"));
        topPanel.add(dashboardTableScrollPane, "grow, push");

        controlPanel.setLayout(new MigLayout("insets 0 12 0 12, novisualpadding, hidemode 3, fill, gap 12"));
        controlPanel.add(tagFilterButton);
        controlPanel.add(tagsLabel, "left, growx, push");
        controlPanel.add(showLabel, "right, split 4, gapafter 12");
        controlPanel.add(showCurrentStatsButton);
        controlPanel.add(showLifetimeStatsButton);

        pluginContainerPanel.setLayout(new MigLayout("insets 0, novisualpadding, hidemode 3"));
        controlPanel.add(pluginContainerPanel);

        controlPanel.add(tableModeLabel, "right, split 3, gapafter 12");
        controlPanel.add(tableModeGroupsButton, "gapafter 0");
        controlPanel.add(tableModeChannelsButton);
        topPanel.add(controlPanel, "newline, growx");

        add(splitPane, "grow, push");
    }

    private void showCurrentStatsButtonActionPerformed() {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        showLifetimeStats = false;
        model.setShowLifetimeStats(showLifetimeStats);
        if (dashboardTable.getSelectedRowCount() == 0) {
            deselectRows(false);
        } else {
            updatePopupMenu(false);
        }

        // TODO: updateTableHighlighting() is called to force the table to refresh, there is probably a more direct way to do this
        updateTableHighlighting();
    }

    private void showLifetimeStatsButtonActionPerformed() {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        showLifetimeStats = true;
        model.setShowLifetimeStats(showLifetimeStats);
        if (dashboardTable.getSelectedRowCount() == 0) {
            deselectRows(false);
        } else {
            updatePopupMenu(false);
        }

        // TODO: updateTableHighlighting() is called to force the table to refresh, there is probably a more direct way to do this
        updateTableHighlighting();
    }

    private void tagFilterButtonActionPerformed() {
        new ChannelFilter(parent.getChannelTagInfo(true), new ChannelFilterSaveTask() {
            @Override
            public void save(ChannelTagInfo channelTagInfo) {
                parent.setFilteredChannelTags(true, channelTagInfo.getVisibleTags(), channelTagInfo.isEnabled());
                parent.doRefreshStatuses(true);
            }
        });
    }

    private void switchTableMode(boolean groupModeEnabled) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        if (model.isGroupModeEnabled() != groupModeEnabled) {
            Preferences.userNodeForPackage(Mirth.class).putBoolean("dashboardGroupViewEnabled", groupModeEnabled);

            if (groupModeEnabled) {
                tableModeChannelsButton.setContentFilled(false);
            } else {
                tableModeGroupsButton.setContentFilled(false);
            }

            TableState tableState = getCurrentTableState();
            model.setGroupModeEnabled(groupModeEnabled);
            restoreTableState(tableState);
            updatePopupMenu(false);

            int totalGroupCount = parent.channelPanel.getCachedGroupStatuses().size();
            int totalChannelCount = parent.status.size();

            ChannelTagInfo channelTagInfo = parent.getChannelTagInfo(true);
            if (channelTagInfo.isEnabled()) {
                int visibleGroupCount = 0;
                int visibleChannelCount = 0;

                if (model.isGroupModeEnabled()) {
                    for (Enumeration<? extends MutableTreeTableNode> groupNodes = ((MutableTreeTableNode) model.getRoot()).children(); groupNodes.hasMoreElements();) {
                        visibleGroupCount++;
                        visibleChannelCount += ((MutableTreeTableNode) groupNodes.nextElement()).getChildCount();
                    }
                } else {
                    visibleChannelCount = ((MutableTreeTableNode) model.getRoot()).getChildCount();
                }

                updateTagsLabel(totalGroupCount, visibleGroupCount, totalChannelCount, visibleChannelCount);
            } else {
                updateTagsLabel(totalGroupCount, totalGroupCount, totalChannelCount, totalChannelCount);
            }
        }
    }

    public TableState getCurrentTableState() {
        Set<String> selectedIds = new HashSet<String>();
        Map<String, Set<Integer>> selectedConnectors = new HashMap<String, Set<Integer>>();
        Set<String> expandedIds = new HashSet<String>();
        Set<String> collapsedIds = new HashSet<String>();

        int[] selectedRows = dashboardTable.getSelectedModelRows();
        for (int row : selectedRows) {
            AbstractDashboardTableNode node = (AbstractDashboardTableNode) dashboardTable.getPathForRow(row).getLastPathComponent();

            if (node.isGroupNode()) {
                selectedIds.add(node.getGroupStatus().getGroup().getId());
            } else {
                DashboardStatus status = node.getDashboardStatus();

                if (status.getStatusType() == StatusType.CHANNEL) {
                    selectedIds.add(status.getChannelId());
                } else if (status.getStatusType() == StatusType.SOURCE_CONNECTOR || status.getStatusType() == StatusType.DESTINATION_CONNECTOR) {
                    Set<Integer> selectedMetaDataIds = selectedConnectors.get(status.getChannelId());
                    if (selectedMetaDataIds == null) {
                        selectedMetaDataIds = new HashSet<Integer>();
                        selectedConnectors.put(status.getChannelId(), selectedMetaDataIds);
                    }
                    selectedMetaDataIds.add(status.getMetaDataId());
                }
            }
        }

        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();

        if (root != null) {
            if (model.isGroupModeEnabled()) {
                for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                    AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();

                    if (dashboardTable.isExpanded(new TreePath(new Object[] { root, groupNode }))) {
                        expandedIds.add(groupNode.getGroupStatus().getGroup().getId());
                    } else if (groupNode.getChildCount() > 0) {
                        collapsedIds.add(groupNode.getGroupStatus().getGroup().getId());
                    }

                    addChannelNodeExpansionStates(groupNode, expandedIds, collapsedIds);
                }
            } else {
                addChannelNodeExpansionStates(root, expandedIds, collapsedIds);
            }
        }

        return new TableState(selectedIds, selectedConnectors, expandedIds, collapsedIds);
    }

    private void addChannelNodeExpansionStates(MutableTreeTableNode parent, Set<String> expandedIds, Set<String> collapsedIds) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();

        for (Enumeration<? extends MutableTreeTableNode> channelNodes = parent.children(); channelNodes.hasMoreElements();) {
            AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) channelNodes.nextElement();

            if (dashboardTable.isExpanded(new TreePath(model.getPathToRoot(channelNode)))) {
                expandedIds.add(channelNode.getChannelId());
            } else {
                collapsedIds.add(channelNode.getChannelId());
            }
        }
    }

    private void restoreTableState(TableState tableState) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();
        MutableTreeTableNode root = (MutableTreeTableNode) model.getRoot();

        if (model.isGroupModeEnabled()) {
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();
                String groupId = groupNode.getGroupStatus().getGroup().getId();

                if (tableState.getExpandedIds().contains(groupId) || !tableState.getCollapsedIds().contains(groupId)) {
                    dashboardTable.expandPath(new TreePath(new Object[] { root, groupNode }));
                } else if (tableState.getCollapsedIds().contains(groupId)) {
                    dashboardTable.collapsePath(new TreePath(new Object[] { root, groupNode }));
                }

                setChannelNodeExpansionStates(groupNode, tableState);
            }
        } else {
            setChannelNodeExpansionStates(root, tableState);
        }

        final List<TreePath> selectionPaths = new ArrayList<TreePath>();

        if (model.isGroupModeEnabled()) {
            for (Enumeration<? extends MutableTreeTableNode> groupNodes = root.children(); groupNodes.hasMoreElements();) {
                AbstractDashboardTableNode groupNode = (AbstractDashboardTableNode) groupNodes.nextElement();

                if (tableState.getSelectedIds().contains(groupNode.getGroupStatus().getGroup().getId())) {
                    selectionPaths.add(new TreePath(new Object[] { root, groupNode }));
                }

                selectChannelNodes(groupNode, tableState, selectionPaths);
            }
        } else {
            selectChannelNodes(root, tableState, selectionPaths);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dashboardTable.getTreeSelectionModel().setSelectionPaths(selectionPaths.toArray(new TreePath[selectionPaths.size()]));
            }
        });
    }

    private void setChannelNodeExpansionStates(MutableTreeTableNode parent, TableState tableState) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();

        for (Enumeration<? extends MutableTreeTableNode> channelNodes = parent.children(); channelNodes.hasMoreElements();) {
            AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) channelNodes.nextElement();
            String channelId = channelNode.getChannelId();

            if (tableState.getExpandedIds().contains(channelId)) {
                dashboardTable.expandPath(new TreePath(model.getPathToRoot(channelNode)));
            } else if (tableState.getCollapsedIds().contains(channelId)) {
                dashboardTable.collapsePath(new TreePath(model.getPathToRoot(channelNode)));
            }
        }
    }

    private void selectChannelNodes(MutableTreeTableNode parent, TableState tableState, List<TreePath> selectionPaths) {
        DashboardTreeTableModel model = (DashboardTreeTableModel) dashboardTable.getTreeTableModel();

        for (Enumeration<? extends MutableTreeTableNode> channelNodes = parent.children(); channelNodes.hasMoreElements();) {
            AbstractDashboardTableNode channelNode = (AbstractDashboardTableNode) channelNodes.nextElement();

            if (tableState.getSelectedIds().contains(channelNode.getChannelId())) {
                selectionPaths.add(new TreePath(model.getPathToRoot(channelNode)));
            }

            for (Enumeration<? extends MutableTreeTableNode> connectorNodes = channelNode.children(); connectorNodes.hasMoreElements();) {
                AbstractDashboardTableNode connectorNode = (AbstractDashboardTableNode) connectorNodes.nextElement();

                if (tableState.getSelectedConnectors().containsKey(connectorNode.getChannelId()) && tableState.getSelectedConnectors().get(connectorNode.getChannelId()).contains(connectorNode.getDashboardStatus().getMetaDataId())) {
                    selectionPaths.add(new TreePath(model.getPathToRoot(connectorNode)));
                }
            }
        }
    }

    public class TableState {
        private Set<String> selectedIds = new HashSet<String>();
        private Map<String, Set<Integer>> selectedConnectors = new HashMap<String, Set<Integer>>();
        private Set<String> expandedIds = new HashSet<String>();
        private Set<String> collapsedIds = new HashSet<String>();

        public TableState(Set<String> selectedIds, Map<String, Set<Integer>> selectedConnectors, Set<String> expandedIds, Set<String> collapsedIds) {
            this.selectedIds = selectedIds;
            this.selectedConnectors = selectedConnectors;
            this.expandedIds = expandedIds;
            this.collapsedIds = collapsedIds;
        }

        public Set<String> getSelectedIds() {
            return selectedIds;
        }

        public Map<String, Set<Integer>> getSelectedConnectors() {
            return selectedConnectors;
        }

        public Set<String> getExpandedIds() {
            return expandedIds;
        }

        public Set<String> getCollapsedIds() {
            return collapsedIds;
        }
    }

    private JSplitPane splitPane;

    private JPanel topPanel;
    public MirthTreeTable dashboardTable;
    private JScrollPane dashboardTableScrollPane;

    private JPanel controlPanel;
    private IconButton tagFilterButton;
    private JLabel tagsLabel;
    private JLabel showLabel;
    private JRadioButton showCurrentStatsButton;
    private JRadioButton showLifetimeStatsButton;
    private JPanel pluginContainerPanel;
    private JLabel tableModeLabel;
    private IconToggleButton tableModeGroupsButton;
    private IconToggleButton tableModeChannelsButton;

    private JTabbedPane tabPane;
}
