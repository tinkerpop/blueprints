/***************************************************************************
 * Copyright 2012 TXT e-solutions SpA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This work was performed within the IoT_at_Work Project
 * and partially funded by the European Commission's
 * 7th Framework Programme under the research area ICT-2009.1.3
 * Internet of Things and enterprise environments.
 *
 * Authors:
 *      Salvatore Piccione (TXT e-solutions SpA)
 *
 * Contributors:
 *        Domenico Rotondi (TXT e-solutions SpA)
 **************************************************************************/
package com.tinkerpop.blueprints.pgm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler;

/**
 * @author Salvatore Piccione (salvo.picci@gmail.com)
 *
 */
public class DateHandler implements ExtraTypeHandler<Date>{
    
    private static final String ATTRIBUTE_NAME = "isDate";
    
    private static final String TYPE_NAME = "date";
    
    private static final String GRAPHML_TYPE = "string";
    
    private static final String DATE_FORMAT = "yyyyMMdd";

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#getAttributeName()
     */
    public String getAttributeName() {
        return ATTRIBUTE_NAME;
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#getTypeName()
     */
    public String getTypeName() {
        return TYPE_NAME;
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#getAttributeValue()
     */
    public String getAttributeValue() {
        return String.valueOf(true);
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#getType(java.lang.String, java.lang.String)
     */
    public String getType(String currentGraphMLType, String extraTypeValue)
            throws IllegalArgumentException, NullPointerException {
        if (currentGraphMLType.isEmpty())
            throw new IllegalArgumentException("The GraphML type cannot be an empty string.");
        if (GRAPHML_TYPE.equals(currentGraphMLType) && Boolean.parseBoolean(extraTypeValue))
            return TYPE_NAME;
        else
            return currentGraphMLType;
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#unmarshal(com.tinkerpop.blueprints.pgm.Graph, java.lang.Object)
     */
    public Date unmarshal(Graph targetGraph, Object propertyValue) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(propertyValue.toString());
        } catch (ParseException e) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#canHandle(com.tinkerpop.blueprints.pgm.Graph, java.lang.Object)
     */
    public boolean canHandle(Graph targetGraph, Object propertyValue) {
        return Date.class.isInstance(propertyValue);
    }

    /* (non-Javadoc)
     * @see com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler#marshal(com.tinkerpop.blueprints.pgm.Graph, com.tinkerpop.blueprints.pgm.Element, java.lang.String)
     */
    public String marshal(Graph targetGraph, Element element,
            String propertyName) {
        return new SimpleDateFormat(DATE_FORMAT).format(element.getProperty(propertyName));
    }

}
