package cn.wacao.waterfall.framework.task;

/**
 * Created by wacao on 2014/8/10.
 */
public interface ITimeTask {
    /**
     * mark the timing when task is starting
     */
    public void markStart(long timing);

    /**
     * mark the timeing when task is ending
     */
    public void markEnd(long timing);
}
