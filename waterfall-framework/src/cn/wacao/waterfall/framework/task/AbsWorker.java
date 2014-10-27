package cn.wacao.waterfall.framework.task;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by wacao on 2014/8/10.
 */
public class AbsWorker<V> extends FutureTask<V> implements IStateTask, ITimeTask{
    private State state;
    private long startTime;
    private long endTime;
    private int timeout = Integer.MAX_VALUE;

    public AbsWorker(Runnable runnable, V result) {
        super(runnable, result);
        setState(State.Created);
    }

    public AbsWorker(Callable<V> callable) {
        super(callable);
        setState(State.Created);
    }

    @Override
    protected void done() {
        super.done();
        setState(State.Succsess);
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
    public boolean isDone() {
        return this.state.ordinal() >= State.Succsess.ordinal();
    }

    @Override
    public void markStart(long timing) {
        startTime = timing;
    }

    @Override
    public void markEnd(long timing) {
        endTime = timing;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}
