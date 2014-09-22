package cn.wacao.waterfall.framework.crash;

import android.content.Context;
import android.content.SharedPreferences;
import cn.wacao.waterfall.framework.AppConfig;
import cn.wacao.waterfall.framework.log.WLog;
import cn.wacao.waterfall.framework.utils.SharedPref;

public class CrashPref extends SharedPref {

    private static CrashPref sInst;
    public static final String LAST_CRASH_KEY = "lastCrash";
    public static final String LAST_CRASH_TIME_KEY = "lastCrashTime";

    private CrashPref(SharedPreferences pref) {
        super(pref);
    }

    public synchronized static CrashPref instance() {
        if (sInst == null) {
            SharedPreferences pref = AppConfig.getInstance().getAppContext().getSharedPreferences("CrashPref", Context.MODE_PRIVATE);
            sInst = new CrashPref(pref);
        }
        return sInst;
    }

    public String saveCrash(String data) {
        if (isRepeat(data)) {
            return null;
        }
        long timestamp = System.currentTimeMillis();
        String fileName = "crash-" + timestamp;
        this.put(fileName, data);
        WLog.debug(CrashConfig.TAG, "save crash, key:%s,vlue:%s!", fileName,
                data);
        return fileName;
    }

    private Boolean isRepeat(String data) {
        if (data.equals(getLastCrash()) && isInOneHour(getLastCrashTime())) {
            return true;
        } else {
            saveLastCrash(data);
            saveLastCrashTime(System.currentTimeMillis());
            return false;
        }
    }

    private boolean isInOneHour(long lastTime) {
        long curTime = System.currentTimeMillis();
        if ((curTime - lastTime) < (60 * 60 * 1000))
            return true;
        return false;
    }

    private long getLastCrashTime() {
        return this.getLong(LAST_CRASH_TIME_KEY);
    }

    private void saveLastCrashTime(long time) {
        this.putLong(LAST_CRASH_TIME_KEY, time);
    }

    private void saveLastCrash(String data) {
        this.put(LAST_CRASH_KEY, data);
    }

    private String getLastCrash() {
        return this.get(LAST_CRASH_KEY);
    }
}
