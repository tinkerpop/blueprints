package com.tinkerpop.blueprints.transaction;

import com.tinkerpop.blueprints.Element;

public interface PropertyEntry<T extends Element>
{
    T entity();
    
    String key();
    
    Object previouslyCommitedValue();

    Object value();
}
