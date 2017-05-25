/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.chunkstore.sync;

import org.fejoa.chunkstore.ChunkStore;
import org.fejoa.chunkstore.Config;
import org.fejoa.chunkstore.HashValue;
import org.fejoa.library.remote.IRemotePipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.fejoa.chunkstore.sync.Request.HAS_CHUNKS;
import static org.fejoa.chunkstore.sync.Request.OK;


public class HasChunksHandler {
    public static void handleHasChunks(ChunkStore.Transaction transaction, IRemotePipe pipe,
                                       DataInputStream inputStream)
            throws IOException {
        final List<HashValue> haveChunks = new ArrayList<>();
        final int nChunks = inputStream.readInt();
        for (int i = 0; i < nChunks; i++) {
            HashValue hashValue = Config.newBoxHash();
            inputStream.readFully(hashValue.getBytes());
            if (transaction.contains(hashValue))
                haveChunks.add(hashValue);
        }

        DataOutputStream outputStream = new DataOutputStream(pipe.getOutputStream());
        Request.writeResponseHeader(outputStream, HAS_CHUNKS, OK);
        outputStream.writeInt(haveChunks.size());
        for (HashValue hashValue : haveChunks)
            outputStream.write(hashValue.getBytes());
    }
}
