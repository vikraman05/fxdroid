/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library.command;

import org.fejoa.library.Remote;
import org.fejoa.library.database.StorageDir;
import org.fejoa.library.database.DatabaseDiff;
import org.fejoa.library.remote.*;
import org.fejoa.library.support.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OutgoingQueueManager {
    final private String TASK_NAME = "Send Commands";
    final private ConnectionManager manager;
    final private List<OutgoingCommandQueue> queues = new ArrayList<>();
    private Task.IObserver<TaskUpdate, Void> observer;
    // command hash to job callback
    final private Map<String, Task.ICancelFunction> runningSendJobs = new HashMap<>();

    final private StorageDir.IListener storageListener = new StorageDir.IListener() {
        @Override
        public void onTipChanged(DatabaseDiff diff) {
            sendCommands();
        }
    };

    public OutgoingQueueManager(OutgoingCommandQueue queue, ConnectionManager manager) {
        this.queues.add(queue);
        this.manager = manager;
    }

    public void start(Task.IObserver<TaskUpdate, Void> observer) {
        this.observer = observer;

        for (OutgoingCommandQueue queue : queues) {
            StorageDir dir = queue.getStorageDir();
            dir.addListener(storageListener);
        }
        sendCommands();
    }

    public void stop() {
        for (OutgoingCommandQueue queue : queues)
            queue.getStorageDir().removeListener(storageListener);
    }

    private void sendCommands() {
        for (OutgoingCommandQueue queue : queues) {
            try {
                List<OutgoingCommandQueue.Entry> commands = queue.getCommands();
                for (int i = 0; i < commands.size(); i++) {
                    OutgoingCommandQueue.Entry command = commands.get(i);
                    send(queue, command, observer, commands.size(), i);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void jobReturned(OutgoingCommandQueue.Entry entry) {
        runningSendJobs.remove(entry.hash());
        if (runningSendJobs.size() == 0)
            observer.onResult(null);
    }

    private void send(final OutgoingCommandQueue queue, final OutgoingCommandQueue.Entry entry,
                      final Task.IObserver<TaskUpdate, Void> observer,
                      final int totalCommands, final int currentCommand) {
        if (runningSendJobs.containsKey(entry.hash()))
            return;

        // if run synchronously job may finish before we are able to put it into the map, thus add a preliminary null
        // item
        runningSendJobs.put(entry.hash(), null);
        Task.ICancelFunction job = manager.submit(new SendCommandJob(entry.getData(), entry.getUser()),
                new Remote(entry.getUser(), entry.getServer()), new AuthInfo.Plain(),
                new Task.IObserver<Void, RemoteJob.Result>() {
                    @Override
                    public void onProgress(Void aVoid) {
                    }

                    @Override
                    public void onResult(RemoteJob.Result result) {
                        if (result.status == Errors.DONE) {
                            observer.onProgress(new TaskUpdate(TASK_NAME, totalCommands, currentCommand + 1,
                                    "command sent"));
                            try {
                                queue.removeCommand(entry);
                                queue.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        jobReturned(entry);
                    }

                    @Override
                    public void onException(Exception exception) {
                        jobReturned(entry);
                        observer.onProgress(new TaskUpdate(TASK_NAME, 1, 1, "exception while sending"));
                        observer.onException(exception);
                    }
        }).getCancelFunction();
        // add the job if not already finished
        if (runningSendJobs.containsKey(entry.hash()))
            runningSendJobs.put(entry.hash(), job);
    }
}
