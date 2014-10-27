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
        if (r instanceof AbsWorker) {
            ((AbsWorker) r).markStart(SystemClock.elapsedRealtime());
            ((AbsWorker) r).setState(IStateTask.State.Running);
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (r instanceof AbsWorker) {
            ((AbsWorker) r).markEnd(SystemClock.elapsedRealtime());
/*            try {
                Object result = ((AbsWorker) r).get();
            } catch (CancellationException ce) {
                t = ce;
                ((AbsWorker) r).setState(IStateTask.State.Canceled);
            } catch (ExecutionException ee) {
                t = ee.getCause();
                ((AbsWorker) r).setState(IStateTask.State.Error);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
                t = ie;
                ((AbsWorker) r).setState(IStateTask.State.Interupted);
            }*/
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
    }

    @Override
    public Worker<?> submit(Runnable task) {
        if (task == null)
            throw new NullPointerException();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return (Worker<?>) super.submit(task);
        else
            return newCompatibleTaskFor(task, null);
    }

    @Override
    public <T> Worker<T> submit(Callable<T> task) {
        if (task == null)
            throw new NullPointerException();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return (Worker<T>) super.submit(task);
        else
            return newCompatibleTaskFor(task);
    }

    @Override
    public <T> Worker<T> submit(Runnable task, T result) {
        if (task == null)
            throw new NullPointerException();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
            return (Worker<T>) super.submit(task, result);
        else
            return newCompatibleTaskFor(task, result);
    }

    @TargetApi(9)
    @Override
    protected <T> Worker<T> newTaskFor(Callable<T> callable) {
        return new Worker<T>(callable);
    }

    @TargetApi(9)
    @Override
    protected <T> Worker<T> newTaskFor(Runnable runnable, T value) {
        return new Worker<T>(runnable, value);
    }

    protected <T> Worker<T> newCompatibleTaskFor(Callable<T> callable) {
        return new Worker<T>(callable);
    }

    protected <T> Worker<T> newCompatibleTaskFor(Runnable runnable, T value) {
        return new Worker<T>(runnable, value);
    }
}
