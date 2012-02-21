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
 * Authors:
 *      Salvatore Piccione (TXT e-solutions SpA)
 **************************************************************************/
package com.tinkerpop.blueprints.pgm.util.io.graphml.extra;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.util.io.graphml.GraphMLWriter;

/**
 * This class handles the parsing both in reading (unmarshalling) and writing (marshalling) of the values that 
 * have a type different from the ones defined in the GraphML XML Schema. Such information
 * is typically stored as an extra attribute of the GraphML element <code>key</code>.
 * The parsing MUST be <i>consistent</i> i.e. given that:<br/><ul><li><code>unmarshalledObject = handler.unmarshal(graph,originalMarshalledValue)</code></li>
 * <li><code>marshalledValue = handler.marshal(graph,element,propertyName)</code></li></ul>the following statements are <code>true</code>:<br/>
 * <ol><li><code>originalMarshalledValue.equals(marshalledValue)</code></li>
 * <li><code>unmarshalledObject.equals(element.getProperty(propertyName))</code></li></ol>
 * 
 * @author Salvatore Piccione (TXT e-solutions SpA)
 *
 */
public interface ExtraTypeHandler<T> {
	
	/**
	 * Returns the name of the key attribute analyzed by this handler. This should be used
	 * to determine the actual type of a graph element property. 
	 * 
	 * @return the name of the key attribute analyzed by this handler.
	 */
	public String getAttributeName ();
	
	/**
	 * Returns the name of the type managed by this handler.
	 * 
	 * @return the name of the managed type.
	 */
	public String getTypeName ();
	
	/**
	 * Returns the value of the key attribute named {@link #getAttributeName()} that, passed as second argument of
	 * {@link #getType(String, String)}, verifies the following equation:
	 * {@link #getType(String, String)} <code>==</code> {@link #getTypeName()}.<br/>
	 * This value will be assigned to the key attribute named {@link #getAttributeName()}
	 * when creating a GraphML file by using {@link GraphMLWriter}.
	 * 
	 * @return the default value of the key attribute managed by this handler.
	 */
	public String getAttributeValue ();
	
	/**
	 * Returns the actual type of a <code>key</code>.
	 * 
	 * @param currentGraphMLType the canonical GraphML type
	 * @param extraTypeValue the value of the key attribute named {@link #getAttributeName()}
	 * @return the actual type that can be: the same string returned by {@link #getTypeName()}, if all
	 * constraints are fulfilled, or <code>currentGraphMLType</code> if at least one constraint is not
	 * fulfilled
	 * @throws IllegalArgumentException if <code>currentGraphMLType</code> is empty.
	 * @throws NullPointerException if <code>currentGraphMLType</code> is null.
	 */
	public String getType (String currentGraphMLType, String extraTypeValue) throws IllegalArgumentException, NullPointerException;
	
	/**
	 * Converts the value of a data element in a GraphML file into an instance of the class
	 * managed by this handler.
	 * 
	 * @param targetGraph the graph where the value should be stored
	 * @param propertyValue the value to be converted
	 * @return an instance of <code>T</code> if {{@link #canHandle(Graph, Object)} <code>== true</code> and
	 * no error occurs during the conversion of the value or <code>null</code> in case 
	 * {{@link #canHandle(Graph, Object)} <code>== false</code> or an error occurs during the conversion of the value
	 */
	public T unmarshal (Graph targetGraph, Object propertyValue);
	
	/**
	 * Checks if this handler can managed the given property value.
	 * 
	 * @param targetGraph the graph where the value is/should be stored
	 * @param propertyValue the value to be checked
	 * @return <code>true</code> if this handler can manage the given value (i.e. the given value is an instance of
	 * <code>T</code> and such instance fulfills all validation constraints - if any)
	 */
	public boolean canHandle (Graph targetGraph, Object propertyValue);
	
	/**
	 * Converts the value of the given graph element property into a string.
	 * 
	 * @param targetGraph the graph where the value has been stored.
	 * @param element the graph element holding the value to be converted into a string.
	 * @param propertyName the name of the property storing the value to be converted
	 * @return the string representation of the value of the given property.
	 */
	public String marshal (Graph targetGraph, Element element, String propertyName);

}
