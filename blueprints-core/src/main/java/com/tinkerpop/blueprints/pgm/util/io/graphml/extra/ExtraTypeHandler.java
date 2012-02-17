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
 **************************************************************************/
package com.tinkerpop.blueprints.pgm.util.io.graphml.extra;

import com.tinkerpop.blueprints.pgm.Graph;

/**
 * This class handles the parsing of the values that have a type different from the ones
 * defined in the GraphML XML Schema. Such information is typically stored as an extra
 * attribute of the element <code>key</code>.
 * Implementations of this interface should compare the GraphML type (if necessary) against the value
 * of the extra attribute to identify the actual type of <code>key</code>.
 * 
 * @author Salvatore Piccione (salvo.picci@gmail.com)
 *
 */
public interface ExtraTypeHandler<T> {
	
	/**
	 * Returns the name of the managed attribute. This should be used
	 * to determine the actual type of a <code>key</code>. 
	 * 
	 * @return
	 */
	public String getAttributeName ();
	
	/**
	 * Returns the name of the managed type.
	 * @return
	 */
	public String getTypeName ();
	
	/**
	 * Returns the value of the managed attribute. This will be assigned
	 * to the <code>key</code> attribute named {@link #getAttributeName()}
	 * when creating the GraphML file from an existing graph.
	 * 
	 * @return
	 */
	public String getAttributeValue ();
	
	/**
	 * Returns the actual type of the <code>key</code>.
	 * 
	 * @param currentGraphMLType the canonical GraphML type
	 * @param extraTypeValue the value of the attribute {@link #getAttributeName()}
	 * @return the actual type that can be: the same string returned by {@link #getTypeName()}, if all
	 * constraints are fulfilled, or <code>currentGraphMLType</code> if at least one constraint is not
	 * fulfilled
	 * @throws IllegalArgumentException if either <code>currentGraphMLType</code> or <code>extraTypeValue</code>
	 * are <code>null</code>
	 */
	public String getType (String currentGraphMLType, String extraTypeValue) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param targetGraph the graph where the value should be stored
	 * @param propertyValue
	 * @return
	 */
	public T unmarshal (Graph targetGraph, Object propertyValue);
	
	public boolean canHandle (Graph targetGraph, Object propertyValue);
	
	public String marshal (Graph targetGraph, Object elementId, String propertyName, Object propertyValue);

}
