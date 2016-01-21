/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.ArrayList;
import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import com.mirth.connect.donkey.model.message.MessageSerializerException;
import com.mirth.connect.donkey.server.controllers.MessageController;
import com.mirth.connect.server.attachments.MirthAttachmentHandlerProvider;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.userutil.ImmutableConnectorMessage;

/**
 * Provides utility methods for creating, retrieving, and re-attaching message attachments.
 */
public class AttachmentUtil {
    private static EngineController engineController = ControllerFactory.getFactory().createEngineController();

    private AttachmentUtil() {}

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message as a byte array.
     * 
     * @param raw
     *            The message string to re-attach attachments to.
     * @param connectorMessage
     *            The connector message associated with the attachments.
     * @param charsetEncoding
     *            The charset encoding to use when returning the re-attached message
     * @param binary
     *            If true, the message string passed in is assumed to be Base64 encoded.
     * @return The resulting message as a byte array, with all applicable attachment content
     *         re-inserted.
     */
    public static byte[] reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage, String charsetEncoding, boolean binary) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(raw, connectorMessage, charsetEncoding, binary);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message.
     * 
     * @param connectorMessage
     *            The connector message associated with the attachments. The encoded data will be
     *            used as the raw message string to re-attach attachments to, if it exists.
     *            Otherwise, the connector message's raw data will be used.
     * @return The resulting message with all applicable attachment content re-inserted.
     */
    public static String reAttachMessage(ImmutableConnectorMessage connectorMessage) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(connectorMessage);
    }

    /**
     * Replaces any unique attachment tokens (e.g. "${ATTACH:id}") with the corresponding attachment
     * content, and returns the full post-replacement message.
     * 
     * @param raw
     *            The message string to re-attach attachments to.
     * @param connectorMessage
     *            The connector message associated with the attachments. The raw data will be used
     *            as the raw message string to re-attach attachments to.
     * @return The resulting message with all applicable attachment content re-inserted.
     */
    public static String reAttachMessage(String raw, ImmutableConnectorMessage connectorMessage) {
        return getAttachmentHandlerProvider(connectorMessage.getChannelId()).reAttachMessage(raw, connectorMessage);
    }

    /**
     * Retrieves all attachments associated with a connector message.
     * 
     * @param connectorMessage
     *            The connector message associated with the attachments.
     * @return A list of attachments associated with the connector message.
     */
    public static List<Attachment> getMessageAttachments(ImmutableConnectorMessage connectorMessage) throws MessageSerializerException {
        return convertFromDonkeyAttachmentList(MirthAttachmentHandlerProvider.getMessageAttachments(connectorMessage));
    }

    /**
     * Creates an Attachment and adds it to the provided list.
     * 
     * @param attachments
     *            The list of attachments to add to.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @return The attachment added to the list.
     * @throws UnsupportedDataTypeException
     */
    public static Attachment addAttachment(List<Attachment> attachments, Object content, String type) throws UnsupportedDataTypeException {
        Attachment userAttachment = convertFromDonkeyAttachment(MessageController.getInstance().createAttachment(content, type));
        attachments.add(userAttachment);
        return userAttachment;
    }

    /**
     * Creates an attachment associated with a given connector message, and inserts it into the
     * database.
     * 
     * @param connectorMessage
     *            The connector message to be associated with the attachment.
     * @param content
     *            The attachment content (must be a string or byte array).
     * @param type
     *            The MIME type of the attachment.
     * @return The attachment that was created and inserted.
     * @throws UnsupportedDataTypeException
     */
    public static Attachment createAttachment(ImmutableConnectorMessage connectorMessage, Object content, String type) throws UnsupportedDataTypeException {
        com.mirth.connect.donkey.model.message.attachment.Attachment attachment = MessageController.getInstance().createAttachment(content, type);
        MessageController.getInstance().insertAttachment(attachment, connectorMessage.getChannelId(), connectorMessage.getMessageId());
        return convertFromDonkeyAttachment(attachment);
    }

    static List<Attachment> convertFromDonkeyAttachmentList(List<com.mirth.connect.donkey.model.message.attachment.Attachment> attachments) {
        List<Attachment> list = new ArrayList<Attachment>();
        for (com.mirth.connect.donkey.model.message.attachment.Attachment attachment : attachments) {
            list.add(convertFromDonkeyAttachment(attachment));
        }
        return list;
    }

    static List<com.mirth.connect.donkey.model.message.attachment.Attachment> convertToDonkeyAttachmentList(List<Attachment> attachments) {
        List<com.mirth.connect.donkey.model.message.attachment.Attachment> list = new ArrayList<com.mirth.connect.donkey.model.message.attachment.Attachment>();
        for (Attachment attachment : attachments) {
            list.add(convertToDonkeyAttachment(attachment));
        }
        return list;
    }

    static Attachment convertFromDonkeyAttachment(com.mirth.connect.donkey.model.message.attachment.Attachment attachment) {
        return new Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
    }

    static com.mirth.connect.donkey.model.message.attachment.Attachment convertToDonkeyAttachment(Attachment attachment) {
        return new com.mirth.connect.donkey.model.message.attachment.Attachment(attachment.getId(), attachment.getContent(), attachment.getType());
    }

    private static MirthAttachmentHandlerProvider getAttachmentHandlerProvider(String channelId) {
        return (MirthAttachmentHandlerProvider) engineController.getDeployedChannel(channelId).getAttachmentHandlerProvider();
    }
}
