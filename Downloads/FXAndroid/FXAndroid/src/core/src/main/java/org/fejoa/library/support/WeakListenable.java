/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package org.fejoa.library.support;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class WeakListenable<Listener> {
    private List<WeakReference<Listener>> listeners = new ArrayList<>();

    public void addListener(Listener listener) {
        if (hasListener(listener))
            return;
        listeners.add(new WeakReference<>(listener));
    }

    public boolean hasListener(Listener listener) {
        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            if (listenerWeak.get() == listener)
                return true;
        }
        return false;
    }

    public void removeListener(Listener listener) {
        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            if (listenerStrong == listener) {
                it.remove();
                return;
            }
        }
    }

    public List<Listener> getListeners() {
        List<Listener> outList = new ArrayList<>();

        Iterator<WeakReference<Listener>> it = listeners.iterator();
        while (it.hasNext()) {
            WeakReference<Listener> listenerWeak = it.next();
            Listener listenerStrong = listenerWeak.get();
            if (listenerStrong == null) {
                it.remove();
                continue;
            }
            outList.add(listenerStrong);
        }

        return outList;
    }

}
