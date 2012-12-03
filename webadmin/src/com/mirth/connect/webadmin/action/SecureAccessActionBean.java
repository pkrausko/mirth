package com.mirth.connect.webadmin.action;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;

public class SecureAccessActionBean extends BaseActionBean {
	@DefaultHandler
	public Resolution secureAccess() {
		HttpServletRequest request = getContext().getRequest();
		StringBuffer requestURL = request.getRequestURL();
		String hostName = request.getRemoteHost();

		// Get hostName
		try {
			URL url = new URL(requestURL.toString());
			hostName = url.getHost();
		} catch (MalformedURLException e) {
			// Ignore
		}
		
		return new RedirectResolution("https://" + hostName + ":" + getContext().getHttpsPort() + "/webadmin/Index.action", false);
	}
}