/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library;

import org.fejoa.library.database.IOStorageDir;
import org.fejoa.library.crypto.Crypto;
import org.fejoa.library.crypto.CryptoHelper;

import java.io.IOException;


public class Remote implements IStorageDirBundle {
    private String id;
    private String user;
    private String server;

    Remote() {

    }

    public Remote(String id, String user, String server) {
        this.id = id;
        this.user = user;
        this.server = server;
    }

    public Remote(String user, String server) {
        this.id = null;
        this.user = user;
        this.server = server;
    }

    public String getId() {
        if (id == null)
            id = CryptoHelper.generateSha1Id(Crypto.get());
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getServer() {
        return server;
    }

    public String toAddress() {
        return getUser() + "@" + getServer();
    }

    @Override
    public void write(IOStorageDir dir) throws IOException {
        dir.writeString(Constants.ID_KEY, getId());
        dir.writeString(Constants.USER_KEY, user);
        dir.writeString(Constants.SERVER_KEY, server);
    }

    @Override
    public void read(IOStorageDir dir) throws IOException {
        id = dir.readString(Constants.ID_KEY);
        user = dir.readString(Constants.USER_KEY);
        server = dir.readString(Constants.SERVER_KEY);
    }
}
