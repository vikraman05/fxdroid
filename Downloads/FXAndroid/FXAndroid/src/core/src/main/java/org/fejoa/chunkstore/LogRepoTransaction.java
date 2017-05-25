/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.chunkstore;

import org.fejoa.library.crypto.CryptoException;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LogRepoTransaction implements IRepoChunkAccessors.ITransaction {
    final private IRepoChunkAccessors.ITransaction childTransaction;
    final private List<HashValue> objectsWritten = new ArrayList<>();

    public LogRepoTransaction(IRepoChunkAccessors.ITransaction childTransaction) {
        this.childTransaction = childTransaction;
    }

    @Override
    public ChunkStore.Transaction getRawAccessor() {
        // TODO: create a wrapper for this as well
        return childTransaction.getRawAccessor();
    }

    @Override
    public IChunkAccessor getCommitAccessor(ChunkContainerRef ref) {
        return createWrapper(childTransaction.getCommitAccessor(ref));
    }

    @Override
    public IChunkAccessor getTreeAccessor(ChunkContainerRef ref) {
        return createWrapper(childTransaction.getTreeAccessor(ref));
    }

    @Override
    public IChunkAccessor getFileAccessor(ChunkContainerRef ref, String filePath) {
        return createWrapper(childTransaction.getFileAccessor(ref, filePath));
    }

    @Override
    public void finishTransaction() throws IOException {
        childTransaction.finishTransaction();
    }

    @Override
    public void cancel() {
        childTransaction.cancel();
    }

    private IChunkAccessor createWrapper(final IChunkAccessor chunkAccessor) {
        return new IChunkAccessor() {
            @Override
            public DataInputStream getChunk(ChunkPointer hash) throws IOException, CryptoException {
                return chunkAccessor.getChunk(hash);
            }

            @Override
            public PutResult<HashValue> putChunk(byte[] data, HashValue ivHash) throws IOException, CryptoException {
                PutResult<HashValue> result = chunkAccessor.putChunk(data, ivHash);
                if (!result.wasInDatabase)
                    objectsWritten.add(result.key);

                return result;
            }

            @Override
            public void releaseChunk(HashValue data) {
                for (HashValue written : objectsWritten) {
                    if (!written.equals(data))
                        continue;
                    objectsWritten.remove(written);
                    break;
                }
            }
        };
    }

    public List<HashValue> getObjectsWritten() {
        return objectsWritten;
    }
}
