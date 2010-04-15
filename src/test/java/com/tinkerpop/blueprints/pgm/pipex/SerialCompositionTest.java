package com.tinkerpop.blueprints.pgm.pipex;

import com.tinkerpop.blueprints.pgm.pipex.util.ChannelReader;
import com.tinkerpop.blueprints.pgm.pipex.util.IdempotentProcess;
import junit.framework.TestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class SerialCompositionTest extends TestCase {

    public void testSimpleComposition() {
        ExecutorService executor = Executors.newScheduledThreadPool(10);
        Channel<String> inChannel = new BlockingChannel<String>(10);
        Channel<String> outChannel = new BlockingChannel<String>(10);
        SerialProcess<String,String> id1 = new IdempotentProcess<String>();
        SerialProcess<String,String> id2 = new IdempotentProcess<String>();
        SerialProcess<String,String> id3 = new IdempotentProcess<String>();
        SerialComposition<String,String> composition = new SerialComposition<String,String>(executor, 10, inChannel, outChannel, id1, id2, id3);
        ChannelReader<String> reader = new ChannelReader<String>(outChannel);
        executor.execute(composition);
        executor.execute(reader);
        inChannel.write("marko");
        inChannel.write("antonio");
        inChannel.write("rodriguez");
        inChannel.close();
        assertEquals(reader.getCounter(), 3);
        executor.shutdown();

    }
}
