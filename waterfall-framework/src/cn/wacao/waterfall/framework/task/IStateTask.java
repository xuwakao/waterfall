package cn.wacao.waterfall.framework.task;

/**
 * Created by xujiexing on 2014/8/10.
 */
public interface IStateTask {
    public enum State{
        Created,
        Running,
        Succsess,
        Error,
        Timeout,
        Canceled,
        Interupted
    }
    public State getState();
    public void setState(State state);
    public boolean isDone();
}
