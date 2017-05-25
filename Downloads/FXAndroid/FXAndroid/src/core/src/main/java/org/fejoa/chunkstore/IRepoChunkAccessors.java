/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.chunkstore;

import java.io.IOException;


public interface IRepoChunkAccessors {
    interface ITransaction {
        /**
         * Accessor to raw data chunks.
         */
        ChunkStore.Transaction getRawAccessor();
        /**
         * Accessor to access commit chunks.
         */
        IChunkAccessor getCommitAccessor(ChunkContainerRef ref);
        /**
         * Accessor to access the directory structure chunks.
         */
        IChunkAccessor getTreeAccessor(ChunkContainerRef ref);
        /**
         * Accessor to access the files structure chunks.
         */
        IChunkAccessor getFileAccessor(ChunkContainerRef ref, String filePath);

        void finishTransaction() throws IOException;
        void cancel();
    }

    ITransaction startTransaction() throws IOException;
}
