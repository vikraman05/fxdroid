/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.chunkstore;

import junit.framework.TestCase;
import org.fejoa.chunkstore.BPlusTree;
import org.fejoa.chunkstore.BaseBPlusTree;
import org.fejoa.chunkstore.HashValue;
import org.fejoa.library.crypto.CryptoHelper;
import org.fejoa.library.support.StorageLib;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;


public class BPlusTreeTest extends TestCase {
    final List<String> cleanUpFiles = new ArrayList<String>();

    class TestTree {
        final BPlusTree tree;
        final Map<String, Long> entries = new HashMap<>();

        public TestTree(BPlusTree tree) {
            this.tree = tree;
        }

        public void put(String key, Long value) throws IOException {
            tree.put(HashValue.fromHex(key), value);
            entries.put(key, value);
        }

        public boolean remove(String key) throws IOException {
            boolean result = tree.remove(key);
            if (!result)
                return result;
            entries.remove(key);
            return result;
        }

        public void validate() throws IOException {
            assertEquals(entries.size(), tree.size());

            for (Map.Entry<String, Long> entry : entries.entrySet())
                assertEquals(entry.getValue(), tree.get(entry.getKey()));

            System.out.println("Deleted tiles: " + tree.countDeletedTiles());
        }

        public void print() throws IOException {
            tree.print();
        }
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        for (String dir : cleanUpFiles)
            StorageLib.recursiveDeleteFile(new File(dir));
    }

    private int tileSize(int nKeys, int hashSize, BPlusTree tree) {
        int indexPointerSize = tree.getIndexType().size();

        return 2 * indexPointerSize + nKeys * (indexPointerSize + hashSize);
    }

    public void testSimple() throws Exception {
        String fileName = "test.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0005", 5l);
        tree.put("0003", 3l);
        tree.print();
        tree.put("0004", 4l);
        tree.print();
        tree.put("0006", 6l);
        tree.print();
        tree.put("0009", 90l);
        tree.print();
        tree.put("0002", 2l);
        tree.print();
        tree.put("0001", 1l);
        tree.print();

        tree.validate();
    }

    public void testRemove() throws Exception {
        String fileName = "remove.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0005", 5l);
        tree.put("0003", 3l);
        tree.put("0002", 2l);
        tree.print();

        assertFalse(tree.remove("0008"));
        tree.remove("0003");

        tree.validate();
        tree.print();
    }

    public void testRemove2() throws Exception {
        String fileName = "remove2.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0005", 5l);
        tree.put("0003", 3l);
        tree.put("0002", 2l);

        tree.remove("0005");

        tree.validate();
        tree.print();
    }

    public void testRemove3() throws Exception {
        String fileName = "remove3.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0006", 6l);
        tree.put("0007", 7l);

        tree.remove("0003");
        tree.validate();

        tree.print();
    }

    public void testRemove4() throws Exception {
        String fileName = "remove4.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0006", 6l);
        tree.put("0007", 7l);

        tree.remove("0007");
        tree.validate();

        tree.print();
    }

    public void testRemove5() throws Exception {
        String fileName = "remove5.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0006", 6l);
        tree.put("0007", 7l);

        tree.print();
        tree.remove("0001");
        tree.validate();

        tree.print();
    }

    public void testRemove6() throws Exception {
        String fileName = "remove6.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0006", 6l);
        tree.put("0007", 7l);

        tree.remove("0004");
        tree.validate();

        tree.print();
    }

    public void testRemove7() throws Exception {
        String fileName = "remove7.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);

        assertTrue(tree.remove("0001"));
        tree.validate();
        tree.print();
        tree.put("0002", 2l);
        tree.print();
        tree.validate();
    }

    public void testRemove8() throws Exception {
        String fileName = "remove8.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0006", 6l);
        tree.put("0007", 7l);

        tree.print();
        tree.remove("0005");
        tree.validate();
        tree.print();
    }

    public void testRemove9() throws Exception {
        String fileName = "remove9.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create((short)2, tileSize(2, 2, bTree));
        TestTree tree = new TestTree(bTree);
        tree.put("0001", 1l);
        tree.put("0002", 2l);
        tree.put("0003", 3l);
        tree.put("0004", 4l);
        tree.put("0005", 5l);
        tree.put("0007", 7l);
        tree.put("0008", 8l);
        tree.put("0006", 6l);

        tree.remove("0008");
        tree.print();
        tree.remove("0007");
        tree.validate();
        tree.print();
    }


    private void add(TestTree tree, Random generator, int items, List<String> added) throws IOException {
        for (int i = 0; i < items; i++) {
            Long value = (long) (Long.MAX_VALUE * generator.nextDouble());
            String hash = CryptoHelper.sha256HashHex(value.toString());
            tree.put(hash, value);
            if (added != null)
                added.add(hash);
        }
    }

    private void remove(TestTree tree, Random generator, int items, List<String> toRemove) throws IOException {
        for (int i = 0; i < items; i++) {
            int value = (int) (toRemove.size() * generator.nextDouble());
            String hash = toRemove.remove(value);
            assertTrue(tree.remove(hash));
        }
    }

    /*
    public void testAddingLarge() throws IOException {
        String fileName = "addLarge.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create(32, 1024);
        TestTree tree = new TestTree(bTree);
        assertEquals(0, bTree.size());
        Random generator = new Random(1);
        List<String> added = new ArrayList<>();
        add(tree, generator, 5000, added);
        tree.validate();

        remove(tree, generator, 2000, added);
        tree.validate();

        add(tree, generator, 1000, added);
        tree.validate();
    }
    */

    private void validateIterator(BPlusTree tree, List<String> expected) throws IOException {
        Iterator<BPlusTree.Entry<Long>> iter = tree.iterator();
        int count = 0;
        while (iter.hasNext()) {
            count++;
            BaseBPlusTree.Entry<Long> next = iter.next();
            assertTrue(expected.contains(new HashValue(next.key).toHex()));
        }
        assertEquals(expected.size(), count);
        assertEquals(expected.size(), tree.size());
    }

    public void testIterator() throws IOException {
        String fileName = "testIterator.idx";
        cleanUpFiles.add(fileName);

        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        BPlusTree bTree = new BPlusTree(file);
        bTree.create(32, 1024);

        assertFalse(bTree.iterator().hasNext());

        TestTree tree = new TestTree(bTree);
        Random generator = new Random(1);
        List<String> added = new ArrayList<>();
        add(tree, generator, 1, added);
        validateIterator(bTree, added);

        add(tree, generator, 20, added);
        validateIterator(bTree, added);

        add(tree, generator, 50, added);
        validateIterator(bTree, added);
    }
}
