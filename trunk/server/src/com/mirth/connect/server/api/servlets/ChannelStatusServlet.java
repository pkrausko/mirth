/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.ChannelStatusServletInterface;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.server.api.CheckAuthorizedChannelId;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.channel.ErrorTaskHandler;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelStatusServlet extends MirthServlet implements ChannelStatusServletInterface {

    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();

    public ChannelStatusServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    @CheckAuthorizedChannelId
    public DashboardStatus getChannelStatus(String channelId) {
        DashboardStatus status = engineController.getChannelStatus(channelId);
        if (status == null) {
            throw new MirthApiException(Status.NOT_FOUND);
        }
        return status;
    }

    @Override
    public List<DashboardStatus> getChannelStatusList(Set<String> channelIds, boolean includeUndeployed) {
        if (CollectionUtils.isEmpty(channelIds)) {
            return redactChannelStatuses(engineController.getChannelStatusList());
        } else {
            return engineController.getChannelStatusList(redactChannelIds(channelIds));
        }
    }

    @Override
    public DashboardChannelInfo getDashboardChannelInfo(int fetchSize) {
        // Return a partial dashboard status list, and a list of remaining channel IDs
        Set<String> remainingChannelIds = engineController.getDeployedIds();

        Set<String> channelIds;
        if (remainingChannelIds.size() > fetchSize) {
            channelIds = new HashSet<String>(fetchSize);

            for (Iterator<String> it = remainingChannelIds.iterator(); it.hasNext() && channelIds.size() < fetchSize;) {
                channelIds.add(it.next());
                it.remove();
            }
        } else {
            channelIds = remainingChannelIds;
            remainingChannelIds = Collections.emptySet();
        }

        List<DashboardStatus> channelStatuses = engineController.getChannelStatusList(redactChannelIds(channelIds));
        return new DashboardChannelInfo(channelStatuses, remainingChannelIds);
    }

    @Override
    @CheckAuthorizedChannelId
    public void startChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.startChannels(Collections.singleton(channelId), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void startChannels(Set<String> channelIds, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.startChannels(redactChannelIds(channelIds), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void stopChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.stopChannels(Collections.singleton(channelId), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void stopChannels(Set<String> channelIds, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.stopChannels(redactChannelIds(channelIds), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void haltChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.haltChannels(Collections.singleton(channelId), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void haltChannels(Set<String> channelIds, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.haltChannels(redactChannelIds(channelIds), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void pauseChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.pauseChannels(Collections.singleton(channelId), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void pauseChannels(Set<String> channelIds, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.pauseChannels(redactChannelIds(channelIds), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void resumeChannel(String channelId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.resumeChannels(Collections.singleton(channelId), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void resumeChannels(Set<String> channelIds, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.resumeChannels(redactChannelIds(channelIds), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void startConnector(String channelId, Integer metaDataId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.startConnector(Collections.singletonMap(channelId, Collections.singletonList(metaDataId)), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void startConnectors(Map<String, List<Integer>> connectorInfo, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.startConnector(redactConnectorInfo(connectorInfo), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    @CheckAuthorizedChannelId
    public void stopConnector(String channelId, Integer metaDataId, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.stopConnector(Collections.singletonMap(channelId, Collections.singletonList(metaDataId)), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    @Override
    public void stopConnectors(Map<String, List<Integer>> connectorInfo, boolean returnErrors) {
        ErrorTaskHandler handler = new ErrorTaskHandler();
        engineController.stopConnector(redactConnectorInfo(connectorInfo), handler);
        if (returnErrors && handler.isErrored()) {
            throw new MirthApiException(handler.getError());
        }
    }

    private List<DashboardStatus> redactChannelStatuses(List<DashboardStatus> channelStatuses) {
        if (userHasChannelRestrictions) {
            List<String> authorizedChannelIds = getAuthorizedChannelIds();
            List<DashboardStatus> authorizedStatuses = new ArrayList<DashboardStatus>();

            for (DashboardStatus status : channelStatuses) {
                if (authorizedChannelIds.contains(status.getChannelId())) {
                    authorizedStatuses.add(status);
                }
            }

            return authorizedStatuses;
        } else {
            return channelStatuses;
        }
    }

    private Map<String, List<Integer>> redactConnectorInfo(Map<String, List<Integer>> connectorInfo) {
        if (userHasChannelRestrictions) {
            List<String> authorizedChannelIds = getAuthorizedChannelIds();
            Map<String, List<Integer>> finishedConnectorInfo = new HashMap<String, List<Integer>>();

            for (String channelId : connectorInfo.keySet()) {
                if (authorizedChannelIds.contains(channelId)) {
                    finishedConnectorInfo.put(channelId, connectorInfo.get(channelId));
                }
            }

            return finishedConnectorInfo;
        } else {
            return connectorInfo;
        }
    }
}