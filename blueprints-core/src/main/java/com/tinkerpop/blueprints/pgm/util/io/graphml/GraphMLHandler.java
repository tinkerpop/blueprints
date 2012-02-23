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
package com.tinkerpop.blueprints.pgm.util.io.graphml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.tinkerpop.blueprints.pgm.util.io.graphml.extra.ExtraTypeHandler;

/**
 * A superclass that collects the common functionalities and fields of both {@link GraphMLReader} and {@link GraphMLWriter}.
 * 
 * @author Salvatore Piccione (TXT e-solutions SpA)
 *
 */
public class GraphMLHandler {

    protected String edgeLabelKey = null;
    @SuppressWarnings("rawtypes")
    protected Map<String,ExtraTypeHandler> extraTypeHandlerMap = null;
    protected String automaticIndexKey = null;
    protected String manualIndexKey = null;

    /**
     * Constructs a new instance initializing the list of {@linkplain ExtraTypeHandler} implementations.
     */
    @SuppressWarnings("rawtypes")
    protected GraphMLHandler() {
        this.extraTypeHandlerMap = buildExtraTypeHadlerMap();
    }

    /**
     * Sets the name of the GraphML <code>&lt;key&gt;</code> attribute whose value is the name of the automatic index this
     * property is associated to.
     * 
     * @param automaticIndexKey the name of the GraphML <code>&lt;key&gt;</code> attribute holding the name of the
     * automatic index to be associated to every graph element property represented by this <code>&lt;key&gt;</code>
     * element.
     */
    public void setAutomaticIndexKey(String automaticIndexKey) {
        this.automaticIndexKey = automaticIndexKey;
    }

    /**
     * Sets the name of the GraphML <code>&lt;data&gt;</code> attribute whose value is the name of the manual index that
     * specific <code>&lt;data&gt;</code> element is associated to.
     * 
     * @param manualIndexKey the name of the GraphML <code>&lt;data&gt;</code> attribute holding the name of the
     * manual index to be associated to the specific graph element property represented
     * by this <code>&lt;>data&gt;</code> element.
     */
    public void setManualIndexKey(String manualIndexKey) {
        this.manualIndexKey = manualIndexKey;
    }


    /**
     * @param edgeLabelKey if the label of an edge is a &lt;data/&gt; property, fetch it from the data property.
     */
    public void setEdgeLabelKey(String edgeLabelKey) {
        this.edgeLabelKey = edgeLabelKey;
    }

    @SuppressWarnings("rawtypes")
    protected static Map<String, ExtraTypeHandler> buildExtraTypeHadlerMap() {
        ServiceLoader<ExtraTypeHandler> serviceLoader = ServiceLoader.load(ExtraTypeHandler.class,ClassLoader.getSystemClassLoader());
        Iterator<ExtraTypeHandler> iterator = serviceLoader.iterator();
        ExtraTypeHandler handler;
        Map<String,ExtraTypeHandler> extraTypeHandlerMap = new HashMap<String, ExtraTypeHandler>();
        while (iterator.hasNext()) {
            handler = iterator.next();
            extraTypeHandlerMap.put(handler.getTypeName(), handler);
        }
        return extraTypeHandlerMap;
    }

}