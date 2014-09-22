package cn.wacao.waterfall.framework.notification;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import cn.wacao.waterfall.framework.log.WLog;

import java.lang.reflect.Method;

/**
 * Created by wacao on 14-9-22.
 */

public abstract class CallbackHandler extends Handler implements ICallback {

    public CallbackHandler(Looper looper) {
        super(looper);
        init();
    }

    public CallbackHandler() {
        super();
        init();
    }

    private SparseArray<Method> mHandlerMap;

    private void init() {
        initHandlers();
    }

    private synchronized void initHandlers() {
        if (mHandlerMap == null) {
            mHandlerMap = new SparseArray<Method>();
        }
        for (Method method : ((Object)this).getClass().getDeclaredMethods()) {
            EventNotifyCenter.MessageHandler an = method.getAnnotation(EventNotifyCenter.MessageHandler.class);
            if (an != null) {
                mHandlerMap.put(an.message(), method);
            }
        }
    }

    private synchronized Method getMessageHandler(int message) {
        if (mHandlerMap == null) {
            initHandlers();
        }
        return mHandlerMap.get(message);
    }

    public boolean canHandleMessage(int message) {
        return getMessageHandler(message) != null;
    }

    @Override
    public void handleMessage(Message msg) {
        Object[] params = null;
        Method handler = null;
        try {
            handler = getMessageHandler(msg.what);
            params = (Object[]) msg.obj;
            if (params != null) {
                handler.invoke(this, params);
            } else {
                handler.invoke(this, (Object[]) null);
            }
        } catch (Exception e) {
            WLog.error(this, getLog(msg, params, handler));
            WLog.error(this, e.getMessage());
        }
    }

    private String getLog(Message msg, Object[] params, Method handler) {
        try {
            StringBuilder log = new StringBuilder("handle msg ");
            log.append(msg.what);
            log.append(" error, params = [");
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        log.append(", ");
                    }
                    log.append(params[i]);
                }
            }
            log.append("], ");
            log.append("handler = ");
            log.append(handler);
            return log.toString();
        } catch (Exception e) {
            WLog.error(this, "generate error log failed");
            return "generate error log failed";
        }
    }

    @Override
    public void callback(int msg, Object... params) {
        if (canHandleMessage(msg)) {
            Message message = obtainMessage();
            message.what = msg;
            message.obj = params;
            sendMessage(message);
        }
    }

}

