package cn.wacao.waterfall.framework.crash;

import android.content.Context;
import cn.wacao.waterfall.framework.log.WLog;

import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractCrashReport implements CrashListener {
    protected Context ctx;

    public void init(Context context) {
        WLog.debug(this, "AbstractCrashReport init.");
        ctx = context;
        CrashHandler handler = new CrashHandler(
                Thread.getDefaultUncaughtExceptionHandler());
        handler.registListener(this);
        try {
            checkReport();
        } catch (Exception e) {
            WLog.error(this, e);
        }
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     */
    private void checkReport() {
        @SuppressWarnings("unchecked")
        Map<String, String> crashMap = (Map<String, String>) CrashPref.instance().getAll();
        if (crashMap == null || crashMap.isEmpty()) {
            return;
        }

        for (Entry<String, String> entry : crashMap.entrySet()) {
            WLog.debug(this,
                    "checkReport() send crash key:%s, data:%s",
                    entry.getKey(), entry.getValue());
            //作为最近一次Bug日志缓存的不上报，只做匹配
            if (!entry.getKey().equals(CrashPref.LAST_CRASH_KEY) && !entry.getKey().equals(CrashPref.LAST_CRASH_TIME_KEY))
                sendReport(entry.getKey(), entry.getValue(), true);
        }
    }

    @Override
    public void onCrash(String key, String data) {
        try {
            sendReport(key, data, false);
        } catch (Exception e) {
            WLog.error(this, e);
        }
    }

    protected abstract void sendReport(String key, String data, boolean onStartSubmit);
}
