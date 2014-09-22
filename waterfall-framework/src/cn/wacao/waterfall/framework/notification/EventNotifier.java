package cn.wacao.waterfall.framework.notification;

import cn.wacao.waterfall.framework.log.WLog;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wacao on 14-9-22.
 */

class EventNotifier {

    private static final ExecutorService mThread = Executors.newCachedThreadPool();

    private final ConcurrentHashMap<Object, CopyOnWriteArraySet<ICallback>> mCallbacks =
            new ConcurrentHashMap<Object, CopyOnWriteArraySet<ICallback>>();

    public void add(Object key, ICallback callback) {
        CopyOnWriteArraySet<ICallback> set = getSet(key, true);
        removeCallbackFromSet(set,callback);
        set.add(callback);
        WLog.debug(this, "registCallback for %s, callback size = %d", key, set.size());
    }

    private CopyOnWriteArraySet<ICallback> getSet(Object key, boolean create) {
        CopyOnWriteArraySet<ICallback> set = mCallbacks.get(key);
        if (set == null) {
            synchronized (this) {
                if (mCallbacks.get(key) == null && create) {
                    set = new CopyOnWriteArraySet<ICallback>();
                    mCallbacks.put(key, set);
                }
                set = mCallbacks.get(key);
            }
        }
        return set;
    }

    private void removeCallbackFromSet(CopyOnWriteArraySet<ICallback> set,
                                       ICallback callback) {
        if (set == null || callback == null) {
            return;
        }
        set.remove(callback);
    }

    public void add(ICallback callback) {
        add(this, callback);
    }

    public void remove(ICallback callback) {
        for (CopyOnWriteArraySet<ICallback> set : mCallbacks.values()) {
            removeCallbackFromSet(set, callback);
        }
    }

    public boolean notifyCallbacks(int message) {
        return notifyCallbacks(this, message, (Object[]) null);
    }

    public boolean notifyCallbacks(Object key, int message) {
        return notifyCallbacks(key, message, (Object[]) null);
    }

    public boolean notifyCallbacks(int message, Object... params) {
        return notifyCallbacks(this, message, params);
    }

    public boolean notifyCallbacks(Object key, final int message, final Object... params) {
        CopyOnWriteArraySet<ICallback> set = getSet(key, false);
        if (set != null) {
            for (final ICallback callback : set) {
                mThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        callback.callback(message, params);
                    }
                });
            }
        }
        return true;
    }

}

