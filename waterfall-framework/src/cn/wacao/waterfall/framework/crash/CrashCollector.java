package cn.wacao.waterfall.framework.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import cn.wacao.waterfall.framework.log.WLog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Properties;

public class CrashCollector {
    public static final int level = 2;

    public static String collectStackTrace(Throwable th) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            break;
        }
        String stackTrace = result.toString();

        printWriter.close();

        return stackTrace.trim();
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx
     */
    public static Properties collectCrashDeviceInfo(Context ctx) {
        Properties crashInfo = new Properties();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                crashInfo.put("versionName", pi.versionName == null ? "not set"
                        : pi.versionName);
                crashInfo.put(CrashConfig.VERSION_CODE,
                        String.valueOf(pi.versionCode));
            }
        } catch (NameNotFoundException e) {
            WLog.error(CrashConfig.TAG, "Error while collectCrashDeviceInfo", e);
        }
        // 使用反射来收集设备信息.在Build类中包含各种设备信息,
        // 例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                crashInfo.put(field.getName(), field.get(null) + "");
                Log.d(CrashConfig.TAG,
                        field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                WLog.error(CrashConfig.TAG, "Error while collect crash info", e);
            }

        }
        Log.d(CrashConfig.TAG, "crashInfo:" + crashInfo);
        return crashInfo;
    }

}
