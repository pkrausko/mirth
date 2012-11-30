/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.delimited;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.mirth.connect.connectors.BatchMessageProcessor;
import com.mirth.connect.connectors.BatchMessageProcessorException;
import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.converters.BatchAdaptor;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.XMLPrettyPrinter;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.builders.ErrorMessageBuilder;

public class DelimitedSerializer implements IXMLSerializer, BatchAdaptor {
    private Logger logger = Logger.getLogger(this.getClass());

    private DelimitedProperties props;
    private DelimitedReader delimitedBatchReader = null;

    public static Map<String, String> getDefaultProperties() {
        return DelimitedProperties.getDefaultProperties();
    }

    public DelimitedSerializer(Map delimitedProperties) {
        props = new DelimitedProperties(delimitedProperties);
    }
    
    @Override
    public boolean isTransformerRequired() {
        return props.isTransformerRequired();
    }
    
    @Override
    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer) {
        return message;
    }

    @Override
    public String fromXML(String source) throws SerializerException {

        StringBuilder builder = new StringBuilder();

        try {

            DelimitedXMLHandler handler = new DelimitedXMLHandler(props);
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            xr.parse(new InputSource(new StringReader(source)));
            builder.append(handler.getOutput());
        } catch (Exception e) {
            String exceptionMessage = e.getClass().getName() + ":" + e.getMessage();
            logger.error(exceptionMessage);
            throw new SerializerException(e, ErrorMessageBuilder.buildErrorMessage(Constants.ERROR_500, "Error converting XML to delimited text", e));
        }
        return builder.toString();
    }

    @Override
    public Map<String, String> getMetadataFromDocument(Document doc) throws SerializerException {
        Map<String, String> map = new HashMap<String, String>();
        populateMetadata(map);
        return map;
    }

    private void populateMetadata(Map<String, String> map) {
        // There is no meaningful meta data available in the delimited text case
        // for version, type and source, so populate empty strings.
        map.put("version", "");
        map.put("type", "delimited");
        map.put("source", "");
    }

    @Override
    public String toXML(String source) throws SerializerException {
        try {
            StringWriter stringWriter = new StringWriter();
            XMLPrettyPrinter serializer = new XMLPrettyPrinter(stringWriter);
            serializer.setEncodeEntities(true);
            DelimitedReader delimitedReader = new DelimitedReader(props);
            delimitedReader.setContentHandler(serializer);
            delimitedReader.parse(new InputSource(new StringReader(source)));
            return stringWriter.toString();
        } catch (Exception e) {
            logger.error("Error converting delimited text to XML.", e);
        }

        return new String();
    }

    /**
     * Finds the next message in the input stream and returns it.
     * 
     * @param in
     *            The input stream (it's a BufferedReader, because operations on
     *            it require in.mark()).
     * @param skipHeader
     *            Pass true to skip the configured number of header rows,
     *            otherwise false.
     * @return The next message, or null if there are no more messages.
     * @throws IOException
     * @throws InterruptedException 
     */
    public String getMessage(BufferedReader in, boolean skipHeader, String batchScriptId) throws IOException, InterruptedException {

        // Allocate a batch reader if not already allocated
        if (delimitedBatchReader == null) {
            delimitedBatchReader = new DelimitedReader(props);
        }
        return delimitedBatchReader.getMessage(in, skipHeader, batchScriptId);
    }

    @Override
    public void processBatch(Reader src, BatchMessageProcessor dest) throws Exception {
        BufferedReader in = new BufferedReader(src);
        String message;
        boolean skipHeader = true;
        boolean errored = false;
        
        while ((message = getMessage(in, skipHeader, dest.getBatchScriptId())) != null) {
            try {
                dest.processBatchMessage(message);
            } catch (BatchMessageProcessorException e) {
                errored = true;
                logger.error("Error processing message in batch.", e);
            }
            
            skipHeader = false;
        }
        
        if (errored) {
            throw new BatchMessageProcessorException("Error processing message in batch.");
        }
    }
    
    public DelimitedProperties getDelimitedProperties() {
        return props;
    }
}
