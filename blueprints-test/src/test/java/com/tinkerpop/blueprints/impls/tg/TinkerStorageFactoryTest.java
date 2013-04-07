package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * @author Victor Su
 */
public class TinkerStorageFactoryTest extends BaseTest {

    @Test
    public void storageFactoryIsSingleton() {
        TinkerStorageFactory factory = TinkerStorageFactory.getInstance();
        Assert.assertSame(factory, TinkerStorageFactory.getInstance());
    }

    @Test
    public void testGMLStorage() throws IOException {
        final String path = getDirectory() + "/" + "storage-test-gml";
        createDirectory(new File(path));

        TinkerStorage storage = TinkerStorageFactory.getInstance().getTinkerStorage(TinkerGraph.FileType.GML);
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        storage.save(graph, path);

        Assert.assertEquals(1, findFilesByExt(path, "gml").length);
        Assert.assertEquals(1, findFilesByExt(path, "dat").length);
    }

    @Test
    public void testGraphMLStorage() throws IOException {
        final String path = getDirectory() + "/" + "storage-test-graphml";
        createDirectory(new File(path));

        TinkerStorage storage = TinkerStorageFactory.getInstance().getTinkerStorage(TinkerGraph.FileType.GRAPHML);
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        storage.save(graph, path);

        Assert.assertEquals(1, findFilesByExt(path, "xml").length);
        Assert.assertEquals(1, findFilesByExt(path, "dat").length);
    }

    @Test
    public void testGraphSONStorageFactory() throws IOException {
        final String path = getDirectory() + "/" + "storage-test-graphson";
        createDirectory(new File(path));

        TinkerStorage storage = TinkerStorageFactory.getInstance().getTinkerStorage(TinkerGraph.FileType.GRAPHSON);
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        storage.save(graph, path);

        Assert.assertEquals(1, findFilesByExt(path, "json").length);
        Assert.assertEquals(1, findFilesByExt(path, "dat").length);
    }

    @Test
    public void testJavaStorageFactory() throws IOException {
        final String path = getDirectory() + "/" + "storage-test-java";
        createDirectory(new File(path));

        TinkerStorage storage = TinkerStorageFactory.getInstance().getTinkerStorage(TinkerGraph.FileType.JAVA);
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        storage.save(graph, path);

        Assert.assertEquals(1, findFilesByExt(path, "dat").length);
    }

    private void createDirectory(File dir) {
        if (dir.exists()) {
            deleteDirectory(dir);
        }

        if (!dir.mkdirs()) {
            throw new RuntimeException("Could not create directory");
        }
    }

    private String getDirectory() {
        String directory = System.getProperty("tinkerGraphDirectory");
        if (directory == null) {
            directory = this.getWorkingDirectory();
        }
        return directory;
    }

    private String getWorkingDirectory() {
        return this.computeTestDataRoot().getAbsolutePath();
    }

    private static File[] findFilesByExt(final String path, final String ext) {
        final File dir = new File(path);

        return dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(ext);
            }
        });
    }
}
