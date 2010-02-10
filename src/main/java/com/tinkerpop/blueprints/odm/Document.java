package com.tinkerpop.blueprints.odm;

import java.util.Map;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Document {

    public Number getNumber(String key);

    public Boolean getBoolean(String key);

    public String getString(String key);

    public Map getMap(String key);

    public List getList(String key);

    public void put(String key, Object value);

    public void remove(String key);

}
