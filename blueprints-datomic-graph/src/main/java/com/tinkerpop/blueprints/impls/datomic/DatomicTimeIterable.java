package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.*;
import java.util.*;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicTimeIterable implements CloseableIterable {

    private TimeAwareElement timeAwareElement;
    private boolean forward;
    private TimeAwareFilter timeAwareFilter;

    public DatomicTimeIterable(TimeAwareElement timeAwareElement, boolean forward) {
        this.timeAwareElement = timeAwareElement;
        this.forward = forward;
    }

    public DatomicTimeIterable(TimeAwareElement timeAwareElement, boolean forward, TimeAwareFilter timeAwareFilter) {
        this.timeAwareElement = timeAwareElement;
        this.forward = forward;
        this.timeAwareFilter = timeAwareFilter;
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator iterator() {
        if (forward) {
            return new ForwardTimeIterator();
        }
        else {
            return new BackwardTimeIterator();
        }
    }

    private class BackwardTimeIterator extends TimeIterator {
        @Override
        protected DatomicElement getNext(TimeAwareElement element) {
            DatomicElement found = (DatomicElement)element.getPreviousVersion();
            if (found != null && timeAwareFilter != null) {
                if (timeAwareFilter.filter(found) != null) {
                    return found;
                }
                else {
                    return getNext(found);
                }
            }
            return found;
        }
    }

    private class ForwardTimeIterator extends TimeIterator {
        @Override
        protected DatomicElement getNext(TimeAwareElement element) {
            DatomicElement found = (DatomicElement)element.getNextVersion();
            if (found != null && timeAwareFilter != null) {
                if (timeAwareFilter.filter(found) != null) {
                    return found;
                }
                else {
                    return getNext(found);
                }
            }
            return found;
        }
    }

    // Iterator for time aware iteration
    private abstract class TimeIterator implements Iterator {

        @Override
        public boolean hasNext() {
            return hasNext(getNext(timeAwareElement));
        }

        protected abstract DatomicElement getNext(TimeAwareElement element);

        @Override
        public TimeAwareElement next() {
            TimeAwareElement next = getNext(timeAwareElement);
            if (next != null) {
                timeAwareElement = next;
                return next;
            }
            else {
                return null;
            }
        }

        private boolean hasNext(DatomicElement element) {
            // We do have a next version
            return element != null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
