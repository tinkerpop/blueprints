package com.tinkerpop.blueprints;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public interface TimeAwareEdge extends Edge, TimeAwareElement {

    @Override
    public TimeAwareEdge getPreviousVersion();

    @Override
    public Iterable<TimeAwareEdge> getPreviousVersions();

    @Override
    public Iterable<TimeAwareEdge> getPreviousVersions(TimeAwareFilter timeAwareFilter);

    @Override
    public TimeAwareEdge getNextVersion();

    @Override
    public Iterable<TimeAwareEdge> getNextVersions();

    @Override
    public Iterable<TimeAwareEdge> getNextVersions(TimeAwareFilter timeAwareFilter);


}
