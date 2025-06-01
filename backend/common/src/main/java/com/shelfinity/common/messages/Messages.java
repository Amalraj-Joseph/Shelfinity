/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.common.messages;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    private Messages() {
        // this class is not meant to be instantiated.
    }

    public static String resolveMessage(String code, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale != null ? locale : Locale.ENGLISH);
        return bundle.containsKey(code) ? bundle.getString(code) : code;
    }

    //public static final String BUNDLE_NAME = Messages.class.getName();
    public static final String BUNDLE_NAME = "messages.messages";

    // Registration Status
    public static final String SFUI001 = "SFUI001";
    public static final String SFUI002 = "SFUI002";
    public static final String SFUI003 = "SFUI003";

    // Common Response Status
    public static final String SFUI004 = "SFUI004";
    public static final String SFUI005 = "SFUI005";
}
