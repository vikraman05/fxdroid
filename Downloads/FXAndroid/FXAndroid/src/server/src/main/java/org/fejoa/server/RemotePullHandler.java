/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.server;

import org.fejoa.chunkstore.Repository;
import org.fejoa.library.AccessTokenContact;
import org.fejoa.library.Constants;
import org.fejoa.library.FejoaContext;
import org.fejoa.library.Remote;
import org.fejoa.library.database.StorageDir;
import org.fejoa.library.remote.*;
import org.fejoa.library.support.Task;
import org.json.JSONObject;

import java.io.InputStream;


public class RemotePullHandler extends JsonRequestHandler {
    public RemotePullHandler() {
        super(RemotePullJob.METHOD);
    }

    @Override
    public void handle(final Portal.ResponseHandler responseHandler, final JsonRPCHandler jsonRPCHandler,
                       InputStream data, Session session) throws Exception {
        JSONObject params = jsonRPCHandler.getParams();
        String serverUser = params.getString(Constants.SERVER_USER_KEY);
        AccessControl accessControl = new AccessControl(session, serverUser);
        if (!accessControl.isRootUser()) {
            responseHandler.setResponseHeader(jsonRPCHandler.makeResult(Errors.ACCESS_DENIED,
                    "Only root users can do a remote pull."));
            return;
        }

        FejoaContext context = session.getContext(serverUser);
        AccessTokenContact accessTokenContact = new AccessTokenContact(context, params.getJSONObject(
                RemotePullJob.ACCESS_TOKEN_KEY));
        String branch = params.getString(Constants.BRANCH_KEY);
        String sourceUser = params.getString(RemotePullJob.SOURCE_USER_KEY);
        String sourceServer = params.getString(RemotePullJob.SOURCE_SERVER_KEY);
        Remote sourceRemote = new Remote(sourceUser, sourceServer);

        // TODO make sure that the it not exists, otherwise it fails to open encrypted StorageDirs
        StorageDir targetDir = context.getStorage(branch, null, null);
        ChunkStorePullRepoJob pullJob = new ChunkStorePullRepoJob((Repository)targetDir.getDatabase(), sourceUser,
                branch);

        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.setStartScheduler(new Task.CurrentThreadScheduler());
        connectionManager.setObserverScheduler(new Task.CurrentThreadScheduler());
        connectionManager.submit(pullJob, sourceRemote,
                new AuthInfo.Token(accessTokenContact),
                new Task.IObserver<Void, RemoteJob.Result>() {
                    @Override
                    public void onProgress(Void aVoid) {

                    }

                    @Override
                    public void onResult(RemoteJob.Result result) {
                        responseHandler.setResponseHeader(jsonRPCHandler.makeResult(result.status,
                                    result.message));
                    }

                    @Override
                    public void onException(Exception exception) {
                        responseHandler.setResponseHeader(jsonRPCHandler.makeResult(Errors.EXCEPTION,
                                exception.getMessage()));
                    }
                });
    }
}
