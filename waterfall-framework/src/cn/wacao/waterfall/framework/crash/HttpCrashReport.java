package cn.wacao.waterfall.framework.crash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import cn.wacao.waterfall.framework.log.WLog;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.zip.CRC32;

/**
 * Created by wacao on 14-9-22.
 */
public class HttpCrashReport extends AbstractCrashReport {

    public static final String APP_ID = "5001";

    public static final String SIGN_KEY = "MCITvktFZZFzZ4XT483I";

    private static HttpCrashReport report;

    private long userId;

    private long imId;

    public static HttpCrashReport getInstance() {
        if (report == null) {
            report = new HttpCrashReport();
        }
        return report;
    }

    private HttpCrashReport() {

    }

    @Override
    public void onCrash(String key, String data) {
        data = appendCrashData(key, data);
        // CrashPersistence.writeLog(ctx, crashInfo,fileName);
        super.onCrash(key, data);
    }

    @Override
    protected void sendReport(final String key, String data, boolean onStartSubmit) {
        JSONTokener jsonTokener = new JSONTokener(data);
        try {
            if (!(jsonTokener.nextValue() instanceof JSONObject)) {
                data = appendCrashData(key, data);
            }
        } catch (Exception ex) {
            data = appendCrashData(key, data);
            WLog.error(this, "crashdata to JSONObject error", ex);
        }

//        RequestParam param = newPostContent(data);
//        //{"appId":"5001","sign":"58bad46c68f962ee00362f07565c4ed126080e4a4a7b71cf5e5838d2fa5f6456","data":{"code":"OK","msg":""}}
//        RequestManager.instance().submitCrashReportRequest(
//                UriProvider.CRASH_POST_URL,
//                param,
//                new ResponseListener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        MLog.debug(this, "on response =" + response);
//                        reportFinish(response, key);
//                    }
//                },
//                new ResponseErrorListener() {
//                    @Override
//                    public void onErrorResponse(RequestError error) {
//                        MLog.error(this, "on error =" + error);
//                    }
//                },
//                onStartSubmit
//        );

    }

    private void reportFinish(String response, String key) {
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                JSONObject resData = json.getJSONObject("data");
                if (resData.getString("code").equals("OK")) {
                    // 删除文件
                    WLog.info("deleteFile", "fileName=%s", key);
                    // CrashPersistence.deleteFile(ctx, key);
                    CrashPref.instance().remove(key);
                }
            } catch (JSONException e) {
                WLog.error(this, "sendReport error!", e);
            }
        }
    }

//    /**
//     * 创建崩溃信息
//     *
//     * @param crashdata 包括设备信息和崩溃日志
//     * @return
//     */
//    private RequestParam newPostContent(String crashdata) {
//        RequestParam param = new DefaultRequestParam();
//        try {
//            JSONObject json = new JSONObject(crashdata);
//
//            String strData = json.getString(CrashConfig.STACK_TRACE);
//            String data = json.getString(ReportConstant.DATA);
//
//            param.put(ReportConstant.APP_ID, APP_ID);
//            param.put(ReportConstant.SIGN, genSign(data, SIGN_KEY));
//            param.put(ReportConstant.DATA, data);
//
//            byte[] files = strData.getBytes("UTF-8");
//            param.put(ReportConstant.FILE_FIELD_NAME, new RequestParam.FileData(files, "crash.txt"));
//        } catch (Exception e) {
//            MLog.error(this, "newPostContent strData to bytes error!", e);
//        }
//
//        return param;
//    }

    public String appendCrashData(String key, String strData) {
        JSONObject result = new JSONObject();
        String reportID = null;
        // report id
        if (reportID == null) {
            CRC32 crc32 = new CRC32();
            crc32.update(strData.getBytes());
            long value = crc32.getValue();
            String strCrc32 = Long.toString(value);
            reportID = strCrc32;
        }
//        if(CoreManager.getAuthCore()!=null){
//            userId = CoreManager.getAuthCore().getUserId();
//        }
//        if(CoreManager.getUserCore()!=null){
//            UserInfo userInfo = CoreManager.getUserCore().getCacheLoginUserInfo();
//            if(userInfo != null){
//                imId = userInfo.yyId;
//            }
//        }

        try {
            String customProperty = createCustomProperty(userId, imId);
            String excepDiscription = getExcepDiscription(strData);
            JSONObject extInfo = createExtInfo(ctx, customProperty,
                    excepDiscription, reportID, userId);

            JSONObject userMap = new JSONObject();
            result.put(CrashConfig.STACK_TRACE, strData);
        } catch (Exception e) {
            WLog.error(this, "collectCrashData error!", e);
        }
        String jsonData = result.toString();
        CrashPref.instance().put(key, jsonData);
        return jsonData;
    }

    private JSONObject createExtInfo(Context mContext,
                                     String customProperty, String excepDiscription, String reportID,
                                     long uid) throws JSONException {

//        Ver curr = VersionUtil.getLocalVer(mContext);
//        String strVer = curr.getOriginalVersion();//curr.toString();
//
//        String strOSVer = android.os.Build.MANUFACTURER + ", "
//                + android.os.Build.BRAND + ", " + android.os.Build.MODEL + ", "
//                + android.os.Build.VERSION.RELEASE + ", "
//                + android.os.Build.DEVICE;

        JSONObject extInfo = new JSONObject();
        // crash extInfo
//        extInfo.put(ReportConstant.CRASH_COUNT_24HOUR, "1"); // save local file,
//        extInfo.put(ReportConstant.CRASH_CUSTOM_PROPERTY, customProperty);
//        // extMap.put(CrashKeys.CRASH_EXCEP_ADDR, "0x6000ffee");
//        // extMap.put(CrashKeys.CRASH_EXCEP_CODE, "0xc0000005");
//        extInfo.put(ReportConstant.CRASH_EXCEP_DISCRIPTION, excepDiscription);
//        extInfo.put(ReportConstant.CRASH_EXCEP_MODULE, "Android");
//        extInfo.put(ReportConstant.CRASH_OS_VER, strOSVer);
//        extInfo.put(ReportConstant.CRASH_PRODUCT_ID, "AndroidYY");// VersionUtil.getPackageName(mContext));
//        // extMap.put(CrashKeys.CRASH_PRODUCT_LANG, "zh-CN");
//        extInfo.put(ReportConstant.CRASH_PRODUCT_VER, strVer);
//        extInfo.put(ReportConstant.CRASH_PRODUCT_VER_DETAIL, strVer);
//        extInfo.put(ReportConstant.CRASH_REPORT_ID, reportID);
//        extInfo.put(ReportConstant.CRASH_RUN_ENV, "0");//崩溃环境，0表示默认崩溃，1表示网吧崩溃
//        // extMap.put(CrashKeys.CRASH_USER_CONTACT, "");
//        // extMap.put(CrashKeys.CRASH_USER_DISCRIPTION, "");
//        //版本类型，1为正式版，2为开发版，默认为1
//        if(Env.instance().isUriDev()){
//            extInfo.put(ReportConstant.PRODUCT_VER_TYPE, "2");
//        }else{
//            extInfo.put(ReportConstant.PRODUCT_VER_TYPE, "1");
//        }
//        extInfo.put(ReportConstant.REPORT_UNIQUE_KEY, uid + "_" + System.currentTimeMillis());
        return extInfo;

    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private String createCustomProperty(long uid, long imid) {
        String eq = "=", cma = ",", verson = "";

        if (Build.VERSION.SDK_INT >= 14) {
            verson = android.os.Build.getRadioVersion();
        } else {
            verson = Build.RADIO;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("IMID").append(eq).append(imid)
                .append(cma).append("UID").append(eq).append(uid)
                .append(cma).append("DISPLAY").append(eq).append(Build.DISPLAY)
                .append(cma).append("Manufacturer").append(eq).append(Build.MANUFACTURER)
                .append(cma).append("Brand").append(eq).append(Build.BRAND)
                .append(cma).append("Model").append(eq).append(Build.MODEL)
                .append(cma).append("Version").append(eq).append(verson)
                .append(cma).append("Device").append(eq).append(Build.DEVICE)
                .append(cma).append("SDKLevel").append(eq).append(Build.VERSION.SDK_INT);

        return sb.toString();
    }

    private String getExcepDiscription(String strData) {
        int maxCount = 500;
        String excepDiscription = "安卓exception";
        int start = strData.lastIndexOf("Caused by");
        if (start > 0) {
            int end = strData.length();
            int count = strData.length() - start;
            if (count > maxCount) {
                end = start + maxCount;
            }
            excepDiscription = strData.substring(start, end);
        } else {
            int end = strData.length() > maxCount ? maxCount : strData.length();
            excepDiscription = strData.substring(0, end);
        }
        return excepDiscription;
    }


//    /**
//     * 根据nyy协议约定,生成sha256哈希的sign
//     *
//     * @param data 没有经过urlEncode的data,为json格式
//     * @param key  业务的key
//     * @return
//     */
//    public String genSign(String data, String key) {
//        String str = "data=" + data + "&key=" + key;
//        return SHAUtils.getSHA256(str);
//    }
}
