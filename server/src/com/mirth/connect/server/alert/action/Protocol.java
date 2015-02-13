package com.mirth.connect.server.alert.action;

import java.util.List;
import java.util.Map;

public interface Protocol {
    /**
     * Returns the unique name of the alert action protocol.
     */
    public String getName();

    /**
     * Returns a map of id -> name recipients or null if this protocol allows free text input of
     * recipients.
     */
    public Map<String, String> getRecipientOptions();

    /**
     * From the given list of recipients, obtain a list of email addresses that will receive the
     * dispatched alert.
     */
    public List<String> getEmailAddressesForDispatch(List<String> recipients);

    /**
     * Dispatch an alert using a custom (non-email) method.
     */
    public void doCustomDispatch(List<String> recipients, String subject, String content);
}
