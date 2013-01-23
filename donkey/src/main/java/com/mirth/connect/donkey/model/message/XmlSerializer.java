/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.message;

public interface XmlSerializer {
    public String toXML(String message) throws SerializerException;

    public String fromXML(String message) throws SerializerException;

    public boolean isSerializationRequired(boolean toXml);

    public String transformWithoutSerializing(String message, XmlSerializer outboundSerializer);
}