package cn.wacao.waterfall.framework.notification;

import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import cn.wacao.waterfall.framework.log.WLog;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by wacao on 14-9-22.
 */

public class CallbackWrapper implements ICallback {

    private static final Handler gHandler = new Handler(Looper.getMainLooper());

    private final Object mListener;
    private final SparseArray<Method> mHandlerMap = new SparseArray<Method>();

    public CallbackWrapper(Object listener) {
        mListener = listener;
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventNotifyCenter.MessageHandler an = method.getAnnotation(EventNotifyCenter.MessageHandler.class);
            if (an != null) {
                mHandlerMap.put(an.message(), method);
            }
        }
    }

    public boolean isValid() {
        return mListener != null && mHandlerMap.size() > 0;
    }

    @Override
    public void callback(int msg, final Object... params) {
        if (isValid()) {
            final Method method = mHandlerMap.get(msg);
            if (method != null) {
                gHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(mListener, params);
                        }
                        catch (Throwable e) {
                            WLog.error(this,
                                    "error happened on invoking %s, params = %s, listener = %s, error = %s",
                                    method, Arrays.toString(params), mListener, e.toString());
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof CallbackWrapper) {
            CallbackWrapper dst = (CallbackWrapper) o;
            return (mListener == dst.mListener || (mListener != null && mListener.equals(dst.mListener)));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (mListener == null ? 0 : mListener.hashCode());
    }

}
