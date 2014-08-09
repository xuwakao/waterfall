package cn.wacao.waterfall.framework.task;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by wacao on 2014/8/10.
 */
public class Worker<V> extends FutureTask<V> implements IStateTask, ITimeTask{
    private State state;
    private long startTime;
    private long endTime;

    public Worker(Runnable runnable, V result) {
        super(runnable, result);
        setState(State.CREATED);
    }

    public Worker(Callable<V> callable) {
        super(callable);
        setState(State.CREATED);
    }

    @Override
    protected void done() {
        super.done();
        setState(State.COMPLETED);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void setState(State state) {
        this.state = state;
    }

    @Override
    public void markStart(long timing) {
        startTime = timing;
    }

    @Override
    public void markEnd(long timing) {
        endTime = timing;
    }
}
