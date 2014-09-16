/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.http;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorProperties;
import com.mirth.connect.donkey.model.channel.DestinationConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.purge.PurgeUtil;

public class HttpDispatcherProperties extends ConnectorProperties implements DestinationConnectorPropertiesInterface {

    private DestinationConnectorProperties destinationConnectorProperties;

    private String host;
    private boolean useProxyServer;
    private String proxyAddress;
    private String proxyPort;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private boolean responseXmlBody;
    private boolean responseParseMultipart;
    private boolean responseIncludeMetadata;
    private String responseBinaryMimeTypes;
    private boolean responseBinaryMimeTypesRegex;
    private boolean multipart;
    private boolean useAuthentication;
    private String authenticationType;
    private boolean usePreemptiveAuthentication;
    private String username;
    private String password;
    private String content;
    private String contentType;
    private boolean dataTypeBinary;
    private String charset;
    private String socketTimeout;

    public HttpDispatcherProperties() {
        destinationConnectorProperties = new DestinationConnectorProperties(false);

        this.host = "";
        this.useProxyServer = false;
        this.proxyAddress = "";
        this.proxyPort = "";
        this.method = "post";
        this.headers = new LinkedHashMap<String, String>();
        this.parameters = new LinkedHashMap<String, String>();
        this.responseXmlBody = false;
        this.responseParseMultipart = true;
        this.responseIncludeMetadata = false;
        this.responseBinaryMimeTypes = "application/, image/, video/, audio/";
        this.responseBinaryMimeTypesRegex = false;
        this.multipart = false;
        this.useAuthentication = false;
        this.authenticationType = "Basic";
        this.usePreemptiveAuthentication = false;
        this.username = "";
        this.password = "";
        this.content = "";
        this.contentType = "text/plain";
        this.dataTypeBinary = false;
        this.charset = "UTF-8";
        this.socketTimeout = "30000";
    }

    public HttpDispatcherProperties(HttpDispatcherProperties props) {
        super(props);
        destinationConnectorProperties = new DestinationConnectorProperties(props.getDestinationConnectorProperties());

        host = props.getHost();
        useProxyServer = props.isUseProxyServer();
        proxyAddress = props.getProxyAddress();
        proxyPort = props.getProxyPort();
        method = props.getMethod();
        headers = new LinkedHashMap<String, String>(props.getHeaders());
        parameters = new LinkedHashMap<String, String>(props.getParameters());
        responseXmlBody = props.isResponseXmlBody();
        responseParseMultipart = props.isResponseParseMultipart();
        responseIncludeMetadata = props.isResponseIncludeMetadata();
        responseBinaryMimeTypes = props.getResponseBinaryMimeTypes();
        responseBinaryMimeTypesRegex = props.isResponseBinaryMimeTypesRegex();
        multipart = props.isMultipart();
        useAuthentication = props.isUseAuthentication();
        authenticationType = props.getAuthenticationType();
        usePreemptiveAuthentication = props.isUsePreemptiveAuthentication();
        username = props.getUsername();
        password = props.getPassword();
        content = props.getContent();
        contentType = props.getContentType();
        dataTypeBinary = props.isDataTypeBinary();
        charset = props.getCharset();
        socketTimeout = props.getSocketTimeout();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isUseProxyServer() {
        return useProxyServer;
    }

    public void setUseProxyServer(boolean useProxyServer) {
        this.useProxyServer = useProxyServer;
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public void setProxyAddress(String proxyAddress) {
        this.proxyAddress = proxyAddress;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isResponseXmlBody() {
        return responseXmlBody;
    }

    public void setResponseXmlBody(boolean responseXmlBody) {
        this.responseXmlBody = responseXmlBody;
    }

    public boolean isResponseParseMultipart() {
        return responseParseMultipart;
    }

    public void setResponseParseMultipart(boolean responseParseMultipart) {
        this.responseParseMultipart = responseParseMultipart;
    }

    public boolean isResponseIncludeMetadata() {
        return responseIncludeMetadata;
    }

    public void setResponseIncludeMetadata(boolean responseIncludeMetadata) {
        this.responseIncludeMetadata = responseIncludeMetadata;
    }

    public String getResponseBinaryMimeTypes() {
        return responseBinaryMimeTypes;
    }

    public void setResponseBinaryMimeTypes(String responseBinaryMimeTypes) {
        this.responseBinaryMimeTypes = responseBinaryMimeTypes;
    }

    public boolean isResponseBinaryMimeTypesRegex() {
        return responseBinaryMimeTypesRegex;
    }

    public void setResponseBinaryMimeTypesRegex(boolean responseBinaryMimeTypesRegex) {
        this.responseBinaryMimeTypesRegex = responseBinaryMimeTypesRegex;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public void setMultipart(boolean multipart) {
        this.multipart = multipart;
    }

    public boolean isUseAuthentication() {
        return useAuthentication;
    }

    public void setUseAuthentication(boolean useAuthentication) {
        this.useAuthentication = useAuthentication;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public boolean isUsePreemptiveAuthentication() {
        return usePreemptiveAuthentication;
    }

    public void setUsePreemptiveAuthentication(boolean usePreemptiveAuthentication) {
        this.usePreemptiveAuthentication = usePreemptiveAuthentication;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isDataTypeBinary() {
        return dataTypeBinary;
    }

    public void setDataTypeBinary(boolean dataTypeBinary) {
        this.dataTypeBinary = dataTypeBinary;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    @Override
    public String getProtocol() {
        return "HTTP";
    }

    @Override
    public String getName() {
        return "HTTP Sender";
    }

    @Override
    public String toFormattedString() {
        StringBuilder builder = new StringBuilder();
        String newLine = "\n";

        builder.append("URL: ");
        builder.append(host);
        builder.append(newLine);

        builder.append("METHOD: ");
        builder.append(method.toUpperCase());
        builder.append(newLine);

        if (StringUtils.isNotBlank(username)) {
            builder.append("USERNAME: ");
            builder.append(username);
            builder.append(newLine);
        }

        builder.append(newLine);
        builder.append("[HEADERS]");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            builder.append(newLine);
            builder.append(header.getKey() + ": " + header.getValue());
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[PARAMETERS]");
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            builder.append(newLine);
            builder.append(parameter.getKey() + ": " + parameter.getValue());
        }
        builder.append(newLine);

        builder.append(newLine);
        builder.append("[CONTENT]");
        builder.append(newLine);
        builder.append(content);
        return builder.toString();
    }

    @Override
    public DestinationConnectorProperties getDestinationConnectorProperties() {
        return destinationConnectorProperties;
    }

    @Override
    public ConnectorProperties clone() {
        return new HttpDispatcherProperties(this);
    }

    @Override
    public boolean canValidateResponse() {
        return true;
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
    public void migrate3_1_0(DonkeyElement element) {
        super.migrate3_1_0(element);

        boolean responseXmlBody = Boolean.parseBoolean(element.removeChild("includeHeadersInResponse").getTextContent());
        element.addChildElement("responseXmlBody", Boolean.toString(responseXmlBody));
        element.addChildElement("responseParseMultipart", Boolean.toString(!responseXmlBody));
        element.addChildElement("responseIncludeMetadata", Boolean.toString(responseXmlBody));

        element.addChildElement("useProxyServer", "false");
        element.addChildElement("proxyAddress", "");
        element.addChildElement("proxyPort", "");

        if (responseXmlBody) {
            element.addChildElement("responseBinaryMimeTypes", "application/, image/, video/, audio/");
        } else {
            element.addChildElement("responseBinaryMimeTypes");
        }
        element.addChildElement("responseBinaryMimeTypesRegex", "false");

        boolean useAuthentication = Boolean.parseBoolean(element.getChildElement("useAuthentication").getTextContent());
        element.addChildElement("usePreemptiveAuthentication", Boolean.toString(useAuthentication));

        element.addChildElement("dataTypeBinary", "false");
    }

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("destinationConnectorProperties", destinationConnectorProperties.getPurgedProperties());
        purgedProperties.put("method", method);
        purgedProperties.put("headerCount", headers.size());
        purgedProperties.put("parameterCount", parameters.size());
        purgedProperties.put("multipart", multipart);
        purgedProperties.put("responseBinaryMimeTypesRegex", responseBinaryMimeTypesRegex);
        purgedProperties.put("useAuthentication", useAuthentication);
        purgedProperties.put("contentLines", PurgeUtil.countLines(content));
        purgedProperties.put("dataTypeBinary", dataTypeBinary);
        purgedProperties.put("charset", charset);
        purgedProperties.put("socketTimeout", PurgeUtil.getNumericValue(socketTimeout));
        return purgedProperties;
    }
}