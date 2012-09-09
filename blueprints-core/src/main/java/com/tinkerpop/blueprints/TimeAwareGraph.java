package com.tinkerpop.blueprints;

import java.util.Date;

/**
 * A time aware graph supports an explicit notion of time for all graph elements (vertices and edges). Each element has a specific scope in time and allows for time-scoped iteration
 * where a user can retrieve previous or next versions of each element
 *
 * @author Davy Suvee (http://datablend.be)
 */
public interface TimeAwareGraph extends Graph {

    /**
     * Sets the time scope of the graph on a specific date
     * @param time the time at which the scope should be placed
     */
    public void setCheckpointTime(Date time);

    /**
     * Sets the time scope at which new elements (vertices and edges) need to be added
     * @param time the time at which the transaction scope should be placed
     */
    public void setTransactionTime(Date time);

}
