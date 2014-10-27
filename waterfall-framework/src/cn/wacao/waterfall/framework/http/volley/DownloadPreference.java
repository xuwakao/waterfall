package cn.wacao.waterfall.framework.http.volley;

import android.content.Context;
import android.content.SharedPreferences;
import cn.wacao.waterfall.framework.AppConfig;
import cn.wacao.waterfall.framework.utils.SharedPref;

/**
 * Created by huluxia-wc on 14-9-30.
 */
public class DownloadPreference extends SharedPref {
    private static final String DOWNLOAD_PREF = "huluxia-download-pref";
    private static DownloadPreference instance;

    private DownloadPreference(SharedPreferences preferences) {
        super(preferences);
    }

    public synchronized static DownloadPreference getInstance() {
        if (instance == null) {
            SharedPreferences pref = AppConfig.getInstance().getAppContext().getSharedPreferences(DOWNLOAD_PREF, Context.MODE_PRIVATE);
            instance = new DownloadPreference(pref);
        }
        return instance;
    }
}
