/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.client.core;

import java.util.List;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.filters.MessageObjectFilter;

public class MessageListHandler implements ListHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectFilter filter;
	private ServerConnection connection;
	private ObjectXMLSerializer serializer = new ObjectXMLSerializer();
	private int currentPage;
	private int pageSize;
	
	public MessageListHandler(MessageObjectFilter filter, int pageSize, ServerConnection connection) {
		// TODO: have this method throw a ListHandlerException
		
		try {
			this.pageSize = pageSize;
			this.connection = connection;
			loadResults(filter);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void resetIndex() {
		currentPage = 0;
	}
	
	public List<MessageObject> getAllPages() throws ListHandlerException {
		logger.debug("retrieving all pages");
		return getMessagesByPage(-1);
	}
	
	public List<MessageObject> getFirstPage() throws ListHandlerException {
		logger.debug("retrieving first page of " + pageSize + " results");
		
		currentPage = 0;
		return getMessagesByPage(currentPage);
	}
	
	public List<MessageObject> getNextPage() throws ListHandlerException  {
		logger.debug("retrieving next page of " + pageSize + " results");
		
		currentPage++;
		return getMessagesByPage(currentPage);		
	}

	public List<MessageObject> getPreviousPage() throws ListHandlerException  {
		logger.debug("retrieving previous page of " + pageSize + " results");
		
		if (currentPage > 0) {
			currentPage--;	
			return getMessagesByPage(currentPage);
		} else {
			throw new ListHandlerException("Invalid page.");
		}
	}
	
	private void loadResults(MessageObjectFilter filter) throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "createTempMessagesTable"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			connection.executePostMethod(Client.MESSAGE_SERVLET, params);	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
	
	private List<MessageObject> getMessagesByPage(int page) throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessagesByPage"), new NameValuePair("page", String.valueOf(page)), new NameValuePair("pageSize", String.valueOf(pageSize)) };
		
		try {
			return (List<MessageObject>) serializer.fromXML(connection.executePostMethod(Client.MESSAGE_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}

	public int getSize() throws ListHandlerException {
		NameValuePair[] params = { new NameValuePair("op", "getMessageCount"), new NameValuePair("filter", serializer.toXML(filter)) };
		
		try {
			return Integer.parseInt(connection.executePostMethod(Client.MESSAGE_SERVLET, params));	
		} catch (ClientException e) {
			throw new ListHandlerException(e);
		}
	}
}
