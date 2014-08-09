package cn.wacao.waterfall.framework.task;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.SystemClock;

import java.util.concurrent.*;

/**
 * Created by wacao on 2014/8/10.
 */
public class WorkerExecutor extends ThreadPoolExecutor {
    public WorkerExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public WorkerExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public WorkerExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public WorkerExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if(r instanceof Worker){
            ((Worker) r).markStart(SystemClock.elapsedRealtime());
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if(r instanceof Worker){
            ((Worker) r).markEnd(SystemClock.elapsedRealtime());
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
    }

    @Override
    public Future<?> submit(Runnable task) {
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return super.submit(task);
        else
            return newCompatibleTaskFor(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return super.submit(task, result);
        else
            return newCompatibleTaskFor(task, result);
    }

    @TargetApi(9)
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new Worker<T>(callable);
    }

    @TargetApi(9)
    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new Worker<T>(runnable, value);
    }

    protected <T> RunnableFuture<T> newCompatibleTaskFor(Callable<T> callable) {
        return new Worker<T>(callable);
    }

    protected <T> RunnableFuture<T> newCompatibleTaskFor(Runnable runnable, T value) {
        return new Worker<T>(runnable, value);
    }
}
