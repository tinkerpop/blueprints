package com.tinkerpop.blueprints.impls.tg.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class PropertyArray implements PropertyContainer, Serializable {
    
    private static final float GROWTH_FACTOR = 2.0f;
    private static final int MAX_CAPACITY = 10;
    private static final int DEFAULT_INITIAL_CAPACITY = 5;

    private int size;
    private String[] keys;
    private Object[] values;
    
    public PropertyArray() {
        this(DEFAULT_INITIAL_CAPACITY);
    }
    
    public PropertyArray(int initialCapacity) {
        if (initialCapacity<=0) throw new IllegalArgumentException("Initial capacity must be positive");
        this.keys = new String[initialCapacity];
        this.values = new Object[initialCapacity];
        this.size = 0;
    }
    
    @Override
    public Object get(final String key) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        int position=getPosition(key);
        if (position>=0) return values[position];
        else return null;
    }

    @Override
    public PropertyContainer setProperty(final String key, final Object value) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        //if (value==null) throw new IllegalArgumentException("Value may not be null");
        //1. Find existing position
        int position = getPosition(key);
        if (position>=0) values[position]=value;
        else {
            if (size<keys.length) {
                keys[size]=key.intern();
                values[size]=value;
                size++;
            } else {
                if (size<MAX_CAPACITY) {
                    //Grow arrays
                    int newcapacity = Math.round(GROWTH_FACTOR*size);
                    assert newcapacity>size;
                    String[] newkeys = new String[newcapacity];
                    Object[] newvalues = new Object[newcapacity];
                    System.arraycopy(this.keys,0,newkeys,0,size);
                    System.arraycopy(this.values,0,newvalues,0,size);
                    this.keys=newkeys;
                    this.values=newvalues;
                    return this.setProperty(key,value);
                } else {
                    //It grew to large, upgrade to PropertyMap
                    PropertyMap map = new PropertyMap(Math.round(GROWTH_FACTOR*size));
                    for (int i=0;i<size;i++) {
                        map.setProperty(keys[i],values[i]);
                    }
                    map.setProperty(key,value);
                    return map;
                }
            }
        }
        return this;
    }
    
    private final int getPosition(final String key) {
        for (int i=0;i<size;i++) {
            if (keys[i].equals(key)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public PropertyContainer removeProperty(final String key) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        removeProperty(getPosition(key));
        return this;
    }

    public void removeProperty(final int position) {
        if (position>=0) {
            for (int i=position+1;i<size;i++) {
                keys[i-1]=keys[i];
                values[i-1]=values[i];
            }
            size--;
        }
    }

    @Override
    public void clear() {
        size=0;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return new Set<String>() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return size == 0;
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof String)) return false;
                return getPosition((String) o)>=0;
            }

            @Override
            public boolean containsAll(Collection<?> objects) {
                for (Object a : objects) {
                    if (!contains(a)) return false;
                }
                return true;
            }

            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    int position = -1;

                    @Override
                    public boolean hasNext() {
                        return (position+1)<size;
                    }

                    @Override
                    public String next() {
                        position++;
                        if (position>=size) throw new NoSuchElementException();
                        return keys[position];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            @Override
            public String[] toArray() {
                String[] k = new String[size];
                System.arraycopy(keys,0,k,0,size);
                return k;
            }

            @Override
            public <T> T[] toArray(T[] ts) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean add(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean addAll(Collection<? extends String> strings) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> objects) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> objects) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Iterable<Property> getProperties() {
        return new Iterable<Property>() {
            @Override
            public Iterator<Property> iterator() {
                return new Iterator<Property>() {

                    int position = -1;

                    @Override
                    public boolean hasNext() {
                        return (position+1)<size;
                    }

                    @Override
                    public Property next() {
                        position++;
                        if (position>=size) throw new NoSuchElementException();
                        return new Property(keys[position],values[position]);
                    }

                    @Override
                    public void remove() {
                        removeProperty(position);
                        position--;
                    }
                };
            }
        };
    }
}
