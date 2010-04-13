package com.tinkerpop.blueprints.pgm.pipex;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Process extends Runnable {
    public void onStart();
    public void onStop();
    public boolean step();
}
