package cn.wacao.waterfall.framework.crash;


/**
 * 崩溃发生监听
 */
public interface CrashListener {
    /**
     * 异常的日志内容
     *
     * @param key  崩溃信息统一存储后返回的文件名
     * @param data
     */
    public void onCrash(String key, String data);

}