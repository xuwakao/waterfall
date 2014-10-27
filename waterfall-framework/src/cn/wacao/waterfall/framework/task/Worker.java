package cn.wacao.waterfall.framework.task;

import java.util.concurrent.Callable;

/**
 * Created by wacao on 14-9-24.
 */
public class Worker<T> extends AbsWorker<T> {
    public Worker(Runnable runnable, T result) {
        super(runnable, result);
    }

    public Worker(Callable<T> callable) {
        super(callable);
    }

    @Override
    public void perform() {
        WorkerFactory.getInstance().getWokerHandler().submit(this);
    }
}
