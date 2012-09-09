package com.tinkerpop.blueprints;

import org.joda.time.Interval;

/**
 * A TimeAwareElement is the base interface for time-aware elements (i.e. time-aware vertices and edges).
 * It extends the base blueprints Element interface with time-based operations
 *
 * @author Davy Suvee (http://datablend.be)
 */
public interface TimeAwareElement extends Element {

    /**
     * An identifier that specifies the time-scope in which it exists
     *
     * @return the time identifier of the element
     */
    public Object getTimeId();

    /**
     * Returns true if this element instance is the current version
     *
     * @return true if this element instance is the current version
     */
    public boolean isCurrentVersion();

    /**
     * Returns true if this element no longer exists (it is still the current version, but no longer an active element in the graph)
     *
     * @return true if this element instance is deleted
     */
    public boolean isDeleted();

    /**
     * Returns the previous version of this element
     *
     * @return the previous (time-aware) version of this element
     */
    public TimeAwareElement getPreviousVersion();

    /**
     * Returns the previous versions of this element
     *
     * @return an iterable of previous versions
     */
    public Iterable<? extends TimeAwareElement> getPreviousVersions();

    /**
     * Returns the previous versions of this element that satisfy a particular condition implemented as a filter
     *
     * @param timeAwareFilter the particular time aware filter
     * @return an iterable of previous versions
     */
    public Iterable<? extends TimeAwareElement> getPreviousVersions(TimeAwareFilter timeAwareFilter);

    /**
     * Returns the next version of this element
     *
     * @return the next (time-aware) version of this element
     */
    public TimeAwareElement getNextVersion();

    /**
     * Returns the next versions of this element
     *
     * @return an iterable of previous versions
     */
    public Iterable<? extends TimeAwareElement> getNextVersions();

    /**
     * Returns the next versions of this element that satisfy a particular condition implemented as a filter
     *
     * @param timeAwareFilter the particular time aware filter
     * @return an iterable of previous versions
     */
    public Iterable<? extends TimeAwareElement> getNextVersions(TimeAwareFilter timeAwareFilter);

    /**
     * Returns the time interval in which this version of this node is scoped
     *
     * @return the joda time interval
     */
    public Interval getTimeInterval();

}
