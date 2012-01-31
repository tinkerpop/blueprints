package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.WrappingCloseableSequence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerIndex<T extends Element> implements Index<T>, Serializable {

    private Map<String, Map<Object, ElementSet>> index = new HashMap<String, Map<Object,ElementSet>>();
    private final String indexName;
    private final Class<T> indexClass;

    public TinkerIndex(final String indexName, final Class<T> indexClass) {
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public Class<T> getIndexClass() {
        return this.indexClass;
    }

    public Type getIndexType() {
        return Type.MANUAL;
    }

    public void put(final String key, final Object value, final T element) {
        Map<Object,ElementSet> keyMap = this.index.get(key);
        if (keyMap == null) {
            keyMap = new HashMap<Object, ElementSet>();
            this.index.put(key, keyMap);
        }
        ElementSet objects = keyMap.get(value);
        if (null == objects) {
            objects = new ElementSet();
            keyMap.put(value, objects);
        }
        objects.add(element);

    }

    public CloseableSequence<T> get(final String key, final Object value) {
        Map<Object, ElementSet> keyMap = this.index.get(key);
        if (null == keyMap) {
            return new WrappingCloseableSequence<T>(new HashSet<T>());
        } else {
        	ElementSet set = keyMap.get(value);
            if (null == set)
                return new WrappingCloseableSequence<T>(new HashSet<T>());
            else
                return set.getSequence();
        }
    }

    public long count(final String key, final Object value) {
        Map<Object, ElementSet> keyMap = this.index.get(key);
        if (null == keyMap) {
            return 0;
        } else {
        	ElementSet set = keyMap.get(value);
            if (null == set)
                return 0;
            else
                return set.size();
        }
    }

    public void remove(final String key, final Object value, final T element) {
        Map<Object, ElementSet> keyMap = this.index.get(key);
        if (null != keyMap) {
        	ElementSet objects = keyMap.get(value);
            if (null != objects) {
                objects.remove(element);
                if (objects.size() == 0) {
                    keyMap.remove(value);
                }
            }
        }
    }

    public void removeElement(final T element) {
        if (this.indexClass.isAssignableFrom(element.getClass())) {
            for (Map<Object,ElementSet> map : index.values()) {
                for (ElementSet set : map.values()) {
                    set.remove(element);
                }
            }
        }
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
    
    private class ElementSet {
    	private Set<T> set = null;
    	private ArrayList<T> iterList = null;
    	
    	public void add(T element) {
    		if(set == null)
    			set = new HashSet<T>();
    		set.add(element);
    		iterList = null;
    	}
    	
    	public void remove(T element) {
    		if(set != null) {
    			set.remove(element);
    			iterList = null;
    		}
    	}
    	
    	public int size() {
    		if(set != null)
    			return set.size();
    		else
    			return 0;
    	}

    	public CloseableSequence<T> getSequence() {
    		if(iterList == null) {
				if(set == null)
					return new WrappingCloseableSequence<T>(new ArrayList<T>(0));
				iterList = new ArrayList<T>(set);
    		}
    		return new WrappingCloseableSequence<T>(iterList);
    	}
    }
}
