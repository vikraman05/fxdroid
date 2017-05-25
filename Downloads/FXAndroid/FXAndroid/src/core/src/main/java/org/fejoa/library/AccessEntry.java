/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library;

import org.fejoa.library.database.IOStorageDir;

import java.io.IOException;


/**
 * Granted access from a contact.
 */
public class AccessEntry implements IStorageDirBundle {
    static final private String ACCESS_RIGHT_KEY = "accessRights";

    private String branch;
    private int accessRight;

    public AccessEntry() {

    }

    public AccessEntry(String branch, int accessRight) {
        this.branch = branch;
        this.accessRight = accessRight;
    }

    public String getId() {
        return branch;
    }

    @Override
    public void write(IOStorageDir dir) throws IOException {
        dir.writeString(Constants.BRANCH_KEY, branch);
        dir.writeInt(ACCESS_RIGHT_KEY, accessRight);
    }

    @Override
    public void read(IOStorageDir dir) throws IOException {
        branch = dir.readString(Constants.BRANCH_KEY);
        accessRight = dir.readInt(ACCESS_RIGHT_KEY);
    }
}
