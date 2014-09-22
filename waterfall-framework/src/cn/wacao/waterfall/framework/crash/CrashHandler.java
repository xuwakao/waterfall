package cn.wacao.waterfall.framework.crash;

import cn.wacao.waterfall.framework.log.LogToES;
import cn.wacao.waterfall.framework.log.WLog;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * 崩溃捕获处理，
 *
 * @author chengaochang
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private UncaughtExceptionHandler sDefaultHandler;
    private Map<CrashListener, CrashListener> mListeners;

    public CrashHandler(UncaughtExceptionHandler sDefaultHandler) {
        this.sDefaultHandler = sDefaultHandler;
        Thread.setDefaultUncaughtExceptionHandler(this);
        mListeners = new HashMap<CrashListener, CrashListener>();
    }

    public void registListener(CrashListener listener) {
        mListeners.put(listener, listener);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        try {
            String crashData = CrashCollector.collectStackTrace(ex);
            WLog.info("Konka.crash", crashData);
            writeTraceToLog(crashData, ex);
            String key = CrashPref.instance().saveCrash(crashData);
            /*if (mListeners != null && key!=null) {
				for(CrashListener listener : mListeners.keySet()){
					listener.onCrash(key, crashData);
				}
			}
            Thread.sleep(3000);*/
        } catch (Exception e) {
            WLog.error(this, ex);
        }
        if (sDefaultHandler != null) {
            sDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private void writeTraceToLog(String traces, Throwable ex) {
        try {
            LogToES.writeLogToFile(LogToES.getLogPath(), CrashConfig.UNCAUGHT_EXCEPTIONS_LOGNAME, traces,
                    true, System.currentTimeMillis());
            WLog.error(this, ex);
        } catch (Exception e) {
            WLog.error(this, e);
        }
    }
}
