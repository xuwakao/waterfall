package cn.wacao.waterfall.framework.notification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by duowan on 14-9-22.
 */

public class EventNotifyCenter {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)

    public @interface MessageHandler {
        public int message();
    }

    private static final EventNotifier mNotifier = new EventNotifier();

    public static void add(Class<?> callerCls, Object receiver) {
        ICallback callback = getCallback(receiver);
        if (callback != null) {
            mNotifier.add(callerCls, callback);
        }
    }

    private static ICallback getCallback(Object receiver) {
        ICallback callback = null;
        if (receiver instanceof ICallback) {
            callback = (ICallback) receiver;
        }
        else {
            CallbackWrapper wrapper = new CallbackWrapper(receiver);
            if (wrapper.isValid()) {
                callback = wrapper;
            }
        }
        return callback;
    }

    public static void remove(Object receiver) {
        ICallback callback = getCallback(receiver);
        if (callback != null) {
            mNotifier.remove(callback);
        }
    }

    public static void notifyEvent(Object caller, int message, Object... params) {
        if (caller != null) {
            if (caller instanceof java.lang.Class) {
                mNotifier.notifyCallbacks(caller, message, params);
            }
            else {
                mNotifier.notifyCallbacks(caller.getClass(), message, params);
            }
        }
    }
}

