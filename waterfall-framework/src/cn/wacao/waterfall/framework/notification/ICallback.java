package cn.wacao.waterfall.framework.notification;

/**
 * Created by wacao on 14-9-22.
 */
public interface ICallback {
    public void callback(int msg, Object... params);
}
