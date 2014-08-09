package cn.wacao.waterfall.framework.task;

import java.util.concurrent.ThreadFactory;

/**
 * Created by wacao on 2014/8/10.
 */
public class WorkerThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
