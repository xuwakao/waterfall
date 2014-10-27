package cn.wacao.waterfall.framework.task;

/**
 * Created by wacao on 14-9-24.
 */
public interface IWorkerHandler {
    public <T> Worker<T> submit(Worker<T> worker);
}
