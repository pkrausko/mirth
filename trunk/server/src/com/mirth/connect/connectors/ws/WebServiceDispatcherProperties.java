/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DispatcherConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.QueueConnectorProperties;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class WebServiceDispatcherProperties extends ConnectorProperties implements DispatcherConnectorPropertiesInterface {

    private QueueConnectorProperties queueConnectorProperties;

    private String wsdlUrl;
    private String service;
    private String port;
    private String operation;
    private boolean useAuthentication;
    private String username;
    private String password;
    private String envelope;
    private boolean oneWay;
    private boolean useMtom;
    private List<String> attachmentNames;
    private List<String> attachmentContents;
    private List<String> attachmentTypes;
    private String soapAction;
    private Map<String, Map<String, List<String>>> wsdlDefinitionMap;

    public static final String WEBSERVICE_DEFAULT_DROPDOWN = "Press Get Operations";

    public WebServiceDispatcherProperties() {
        queueConnectorProperties = new QueueConnectorProperties();

        this.wsdlUrl = "";
        this.operation = WEBSERVICE_DEFAULT_DROPDOWN;
        this.wsdlDefinitionMap = new HashMap<String, Map<String, List<String>>>();
        this.service = "";
        this.port = "";
        this.useAuthentication = false;
        this.username = "";
        this.password = "";
        this.envelope = "";
        this.oneWay = false;
        this.useMtom = false;
        this.attachmentNames = new ArrayList<String>();
        this.attachmentContents = new ArrayList<String>();
        this.attachmentTypes = new ArrayList<String>();
        this.soapAction = "";
    }

    public WebServiceDispatcherProperties(WebServiceDispatcherProperties props) {
        super(props);
        queueConnectorProperties = new QueueConnectorProperties(props.getQueueConnectorProperties());

        wsdlUrl = props.getWsdlUrl();
        operation = props.getOperation();
        wsdlDefinitionMap = props.getWsdlDefinitionMap();
        service = props.getService();
        port = props.getPort();
        useAuthentication = props.isUseAuthentication();
        username = props.getUsername();
        password = props.getPassword();
        envelope = props.getEnvelope();
        oneWay = props.isOneWay();
        useMtom = props.isUseMtom();
        attachmentNames = new ArrayList<String>(props.getAttachmentNames());
        attachmentContents = new ArrayList<String>(props.getAttachmentContents());
        attachmentTypes = new ArrayList<String>(props.getAttachmentTypes());
        soapAction = props.getSoapAction();
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    public void setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
    }

    public boolean isUseMtom() {
        return useMtom;
    }

    public void setUseMtom(boolean useMtom) {
        this.useMtom = useMtom;
    }

    public List<String> getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(List<String> attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public List<String> getAttachmentContents() {
        return attachmentContents;
    }

    public void setAttachmentContents(List<String> attachmentContents) {
        this.attachmentContents = attachmentContents;
    }

    public List<String> getAttachmentTypes() {
        return attachmentTypes;
    }

    public void setAttachmentTypes(List<String> attachmentTypes) {
        this.attachmentTypes = attachmentTypes;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public Map<String, Map<String, List<String>>> getWsdlDefinitionMap() {
        return wsdlDefinitionMap;
    }

    public void setWsdlDefinitionMap(Map<String, Map<String, List<String>>> wsdlDefinitionMap) {
        this.wsdlDefinitionMap = wsdlDefinitionMap;
    }

    @Override
    public String getProtocol() {
        return "WS";
    }

    @Override
    public String getName() {
        return "Web Service Sender";
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        builder.append("URL: ");
        builder.append(wsdlUrl);
        builder.append(newLine);

        if (StringUtils.isNotBlank(username)) {
            builder.append("USERNAME: ");
            builder.append(username);
            builder.append(newLine);
        }

        if (StringUtils.isNotBlank(service)) {
            builder.append("SERVICE: ");
            builder.append(service);
            builder.append(newLine);
        }

        if (StringUtils.isNotBlank(port)) {
            builder.append("PORT / ENDPOINT: ");
            builder.append(port);
            builder.append(newLine);
        }

        if (StringUtils.isNotBlank(soapAction)) {
            builder.append("SOAP ACTION: ");
            builder.append(soapAction);
            builder.append(newLine);
        }

        builder.append(newLine);
        builder.append("[ATTACHMENTS]");
        for (int i = 0; i < attachmentNames.size(); i++) {
            builder.append(newLine);
            builder.append(attachmentNames.get(i));
            builder.append(" (");
            builder.append(attachmentTypes.get(i));
            builder.append(")");
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(envelope);
        return builder.toString();
    }

    @Override
    public QueueConnectorProperties getQueueConnectorProperties() {
        return queueConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new WebServiceDispatcherProperties(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("queueConnectorProperties", queueConnectorProperties.getPurgedProperties());
        purgedProperties.put("useAuthentication", useAuthentication);
        purgedProperties.put("envelopeLines", PurgeUtil.countLines(envelope));
        purgedProperties.put("oneWay", oneWay);
        purgedProperties.put("useMtom", useMtom);
        purgedProperties.put("attachmentNamesCount", attachmentNames.size());
        purgedProperties.put("attachmentContentCount", attachmentContents.size());
        purgedProperties.put("wsdlDefinitionMapCount", wsdlDefinitionMap.size());
        return purgedProperties;
    }
}
