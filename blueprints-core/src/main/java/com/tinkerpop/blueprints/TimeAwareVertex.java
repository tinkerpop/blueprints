package com.tinkerpop.blueprints;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public interface TimeAwareVertex extends Vertex, TimeAwareElement {

    @Override
    public TimeAwareVertex getPreviousVersion();

    @Override
    public Iterable<TimeAwareVertex> getPreviousVersions();

    @Override
    public Iterable<TimeAwareVertex> getPreviousVersions(TimeAwareFilter timeAwareFilter);

    @Override
    public TimeAwareVertex getNextVersion();

    @Override
    public Iterable<TimeAwareVertex> getNextVersions();

    @Override
    public Iterable<TimeAwareVertex> getNextVersions(TimeAwareFilter timeAwareFilter);

}
