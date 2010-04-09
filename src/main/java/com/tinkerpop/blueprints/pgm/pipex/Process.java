package com.tinkerpop.blueprints.pgm.pipex;

import java.util.concurrent.BlockingQueue;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Process extends Runnable {
    public void onStart();
    public void onStop();
    public void step();
}
