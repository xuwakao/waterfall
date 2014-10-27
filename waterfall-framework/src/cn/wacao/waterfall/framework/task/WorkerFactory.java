package cn.wacao.waterfall.framework.task;

import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wacao on 14-9-24.
 */
public class WorkerFactory {
    private static WorkerFactory mFactory;
    private Map<Looper, WorkerHandler> mHandlers = new HashMap<Looper, WorkerHandler>();

    public synchronized static WorkerFactory getInstance() {
        if (mFactory == null) {
            mFactory = new WorkerFactory();
        }
        return mFactory;
    }

    public synchronized WorkerHandler getWokerHandler() {
        WorkerHandler handler = mHandlers.get(Looper.myLooper());
        if (handler == null) {
            handler = new WorkerHandler(Looper.myLooper());
            mHandlers.put(handler.getLoopper(), handler);
        }
        return handler;
    }
}
