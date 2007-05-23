/* 
 * $Header: /home/projects/mule/scm/mule/providers/email/src/java/org/mule/providers/email/SmtpMessageDispatcher.java,v 1.10 2005/10/10 14:00:15 rossmason Exp $
 * $Revision: 1.10 $
 * $Date: 2005/10/10 14:00:15 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package com.webreach.mirth.server.mule.providers.email;

import java.util.Calendar;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;

import org.mule.MuleException;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.provider.DispatchException;
import org.mule.umo.provider.UMOConnector;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.mule.providers.email.transformers.MessageObjectToEmailMessage;

/**
 * @author Ross Mason
 */
public class SmtpMessageDispatcher extends AbstractMessageDispatcher {
	private Session session;
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = new AlertController();
	private SmtpConnector connector;

	/**
	 * @param connector
	 */
	public SmtpMessageDispatcher(SmtpConnector connector) {
		super(connector);
		this.connector = connector;
		String username = new String();
		String password = new String();
		if (connector.getUsername() != null){
			username = connector.getUsername();
		}
		if (connector.getPassword() != null){
			password = connector.getPassword();
		}
		URLName url = new URLName(connector.getProtocol(), connector.getHostname(), connector.getSmtpPort(), null, username, password);
		session = MailUtils.createMailSession(url, connector);
		session.setDebug(logger.isDebugEnabled());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.UMOConnector#dispatch(java.lang.Object,
	 *      org.mule.providers.MuleEndpoint)
	 */
	public void doDispatch(UMOEvent event) throws Exception {
		Message msg = null;
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);
		if (messageObject == null) {
			return;
		}
		try{
			MessageObjectToEmailMessage motoEmail = new MessageObjectToEmailMessage();
			motoEmail.setEndpoint(event.getEndpoint());
			
			Object data = motoEmail.transform(messageObject);

			if (!(data instanceof Message)) {
				throw new DispatchException(new org.mule.config.i18n.Message(Messages.TRANSFORM_X_UNEXPECTED_TYPE_X, data.getClass().getName(), Message.class.getName()), event.getMessage(), event.getEndpoint());
			} else {
				// Check the message for any unset data and use defaults
				msg = (Message) data;
			}

			sendMailMessage(msg);
			messageObjectController.setSuccess(messageObject, "Email successfully sent: " + connector.getToAddresses());
		} catch (Exception e) {
			alertController.sendAlerts(messageObject.getChannelId(),  Constants.ERROR_402, "Error sending email", e);
			messageObjectController.setError(messageObject, Constants.ERROR_402, "Error sending email", e);
			connector.handleException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#getDelegateSession()
	 */
	public Object getDelegateSession() throws UMOException {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#receive(java.lang.String,
	 *      org.mule.umo.UMOEvent)
	 */
	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		throw new UnsupportedOperationException("Cannot do a receive on an SmtpConnector");
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	protected void sendMailMessage(String to, String cc, String bcc, String subject, String body) throws MuleException, MessagingException {
		Message message = connector.createMessage(connector.getFromAddress(), to, cc, bcc, subject, body, session);
		sendMailMessage(message);
	}

	protected void sendMailMessage(Message message) throws MessagingException {
		// sent date
		message.setSentDate(Calendar.getInstance().getTime());
		Transport.send(message);
		if (logger.isDebugEnabled()) {
			StringBuffer msg = new StringBuffer();
			msg.append("Email message sent with subject'").append(message.getSubject()).append("' sent- ");
			msg.append("From: ").append(MailUtils.mailAddressesToString(message.getFrom())).append(" ");
			msg.append("To: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.TO))).append(" ");
			msg.append("Cc: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.CC))).append(" ");
			msg.append("Bcc: ").append(MailUtils.mailAddressesToString(message.getRecipients(Message.RecipientType.BCC))).append(" ");
			msg.append("ReplyTo: ").append(MailUtils.mailAddressesToString(message.getReplyTo()));

			logger.debug(msg.toString());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnectorSession#getConnector()
	 */
	public UMOConnector getConnector() {
		return connector;
	}

	public void doDispose() {
		session = null;
	}
}
