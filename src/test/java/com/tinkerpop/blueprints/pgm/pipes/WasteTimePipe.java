package com.tinkerpop.blueprints.pgm.pipes;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WasteTimePipe extends AbstractPipe<String, String> {

    private final int sleepTime;

    public WasteTimePipe(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public WasteTimePipe() {
        this(3);
    }

    protected String processNextStart() {
        try {
            Thread.sleep(this.sleepTime);
        } catch (InterruptedException e) {
        }
        //System.out.println(Thread.currentThread().getName());
        return this.starts.next();
    }
}
