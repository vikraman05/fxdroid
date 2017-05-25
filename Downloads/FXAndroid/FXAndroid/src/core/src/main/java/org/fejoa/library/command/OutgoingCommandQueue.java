/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library.command;

import org.fejoa.library.Remote;
import org.fejoa.library.crypto.CryptoException;
import org.fejoa.library.database.IOStorageDir;
import org.fejoa.library.database.StorageDir;

import java.io.IOException;
import java.util.List;


public class OutgoingCommandQueue extends CommandQueue<OutgoingCommandQueue.Entry> {
    static public class Entry extends CommandQueue.Entry {
        final static private String USER_KEY = "user";
        final static private String SERVER_KEY = "server";

        private String user;
        private String server;

        public Entry() {
            super();
        }

        public Entry(byte[] data, String user, String server) {
            super(data);

            this.user = user;
            this.server = server;
        }

        @Override
        public void write(IOStorageDir dir) throws IOException, CryptoException {
            super.write(dir);

            dir.writeString(USER_KEY, user);
            dir.writeString(SERVER_KEY, server);
        }

        @Override
        public void read(IOStorageDir dir) throws IOException, CryptoException {
            super.read(dir);

            user = dir.readString(USER_KEY);
            server = dir.readString(SERVER_KEY);
        }

        public String getUser() {
            return user;
        }

        public String getServer() {
            return server;
        }
    }

    public OutgoingCommandQueue(StorageDir dir) throws IOException {
        super(dir);
    }

    public void updateReceiver(String oldUser, String oldServer, String newUser, String newServer) throws IOException,
            CryptoException {
        List<Entry> commands = getCommands();
        for (Entry entry : commands) {
            if (!entry.getUser().equals(oldUser) && !entry.getServer().equals(oldServer))
                continue;
            removeCommand(entry);
            addCommand(new Entry(entry.getData(), newUser, newServer));
        }
    }

    public void post(ICommand command, Remote receiver, boolean commit) throws IOException, CryptoException {
        post(command, receiver.getUser(), receiver.getServer(), commit);
    }

    public void post(ICommand command, String user, String server, boolean commit)
            throws IOException, CryptoException {

        OutgoingCommandQueue.Entry entry = new OutgoingCommandQueue.Entry(command.getCommand(), user, server);
        addCommand(entry);

        if (commit)
            commit();
    }

    public void post(ICommand command, String user, String server)
            throws IOException, CryptoException {
        post(command, user, server, true);
    }

    @Override
    protected OutgoingCommandQueue.Entry instantiate() {
        return new Entry();
    }
}
