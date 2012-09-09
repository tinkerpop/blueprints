package com.tinkerpop.blueprints;

/**
 * Filters for time aware elements
 *
 * @author Davy Suvee (http://datablend.be)
 */
public interface TimeAwareFilter {

    /**
     * Returns the time aware element if a particular condition, implemented by the filter holds.
     *
     * @param timeAwareElement the time aware element to filter
     * @return the original input or null in case the element does not satisfy the particular condition
     */
    public TimeAwareElement filter(TimeAwareElement timeAwareElement);

}
