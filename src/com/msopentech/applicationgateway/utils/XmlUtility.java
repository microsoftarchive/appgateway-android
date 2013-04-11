/*
 *  Copyright (c) Microsoft Open Technologies
 *  All rights reserved. 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0  
 *  THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT 
 *  LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, 
 *  MERCHANTABLITY OR NON-INFRINGEMENT. 
 *  See the Apache Version 2.0 License for specific language governing permissions and limitations under the License.
 */
package com.msopentech.applicationgateway.utils;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.msopentech.applicationgateway.ApplicationGateway;

/**
 * XML utility helper class.
 */
public class XmlUtility {

    /**
     * Retrieves child node by specified name or null if node wasn't found.
     * 
     * @param node XML node.
     * @param childNodeName Child node name.
     * 
     * @return Found child node, or null if child node wasn't found.
     */
    public static Node getChildNode(Node node, String childNodeName) {
        try {
            if (node == null || childNodeName == null || childNodeName.length() == 0) {
                return null;
            }

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeName().equalsIgnoreCase(childNodeName)) {
                    return childNode;
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(XmlUtility.class.getSimpleName() + ".getChildNode(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }

        return null;
    }

    /**
     * Retrieves child node value by specified child node name or null if
     * anything is wrong.
     * 
     * @param node XML node.
     * @param childNodeName Child node name.
     * 
     * @return Child node value or null if anything is wrong.
     */
    public static String getChildNodeValue(Node node, String childNodeName) {
        try {
            return getNodeValue(getChildNode(node, childNodeName));
        } catch (final Exception e) {
            Utility.showAlertDialog(XmlUtility.class.getSimpleName() + ".getChildNodeValue(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }

        return null;
    }

    /**
     * Retrieves node value or null if node empty.
     * 
     * @param node XML node.
     * 
     * @return Node value or null if node empty.
     */
    public static String getNodeValue(Node node) {
        try {
            return getNodeValue(node, null);
        } catch (final Exception e) {
            Utility.showAlertDialog(XmlUtility.class.getSimpleName() + ".getNodeValue(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }

        return null;
    }

    /**
     * Retrieves node value or default value if node is empty.
     * 
     * @param node XML node.
     * @param defaultValue Default value.
     * 
     * @return Node value or default value if node is empty.
     */
    public static String getNodeValue(Node node, String defaultValue) {
        if (node == null) {
            return defaultValue;
        }

        try {
            NodeList childs = node.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    return child.getNodeValue();
                }
            }
        } catch (final Exception e) {
            Utility.showAlertDialog(XmlUtility.class.getSimpleName() + ".getNodeValue(): Failed. " + e.toString(), ApplicationGateway.getAppContext());
        }

        return defaultValue;
    }
}
