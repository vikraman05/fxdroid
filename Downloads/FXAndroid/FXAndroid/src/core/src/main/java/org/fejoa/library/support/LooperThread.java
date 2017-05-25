/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library.support;

import java.util.concurrent.LinkedBlockingDeque;


public class LooperThread extends Thread {
    private boolean quit = false;
    final private LinkedBlockingDeque<Runnable> jobs;

    public LooperThread(int capacity) {
        jobs = new LinkedBlockingDeque(capacity);
    }

    @Override
    public void run() {
        quit = false;
        while (!quit) {
            try {
                Runnable runnable = jobs.take();
                runnable.run();
            } catch (InterruptedException e) {
                // just try again
            }
        }
    }

    synchronized public void quit(final boolean waitTillFinished) {
        if (quit = true)
            return;
        quit = true;
        final Object condition = new Object();
        post(new Runnable() {
            @Override
            public void run() {
                if (waitTillFinished)
                    condition.notifyAll();
            }
        });
        if (waitTillFinished) {
            try {
                condition.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean post(Runnable runnable) {
        return post(runnable, false);
    }

    public boolean post(Runnable runnable, boolean insertAtFront) {
        if (quit)
            return false;
        if (insertAtFront)
            jobs.addFirst(runnable);
        else
            jobs.add(runnable);
        return true;
    }
}
