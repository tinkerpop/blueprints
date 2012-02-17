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
package com.tinkerpop.blueprints.pgm.util.io.graphml.extra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Salvatore Piccione (salvo.picci@gmail.com)
 *
 */
public class ExtraTypeHandlerHelper {

    @SuppressWarnings("rawtypes")
	public static Map<String,ExtraTypeHandler> buildExtraTypeHadlerMap () {
    	ServiceLoader<ExtraTypeHandler> serviceLoader = ServiceLoader.load(ExtraTypeHandler.class,ClassLoader.getSystemClassLoader());
    	Iterator<ExtraTypeHandler> iterator = serviceLoader.iterator();
    	ExtraTypeHandler handler;
    	HashMap<String, ExtraTypeHandler> extraTypeHandlerMap = new HashMap<String, ExtraTypeHandler>();
    	while (iterator.hasNext()) {
    		handler = iterator.next();
    		extraTypeHandlerMap.put(handler.getTypeName(), handler);
    	}
    	return extraTypeHandlerMap;
    }
}
