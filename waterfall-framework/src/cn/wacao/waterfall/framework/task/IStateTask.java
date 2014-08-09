package cn.wacao.waterfall.framework.task;

/**
 * Created by xujiexing on 2014/8/10.
 */
public interface IStateTask {
    public enum State{
        CREATED,
        STARTING,
        COMPLETED
    }
    public State getState();
    public void setState(State state);
}
