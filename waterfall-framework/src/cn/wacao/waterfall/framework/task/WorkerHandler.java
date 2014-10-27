package cn.wacao.waterfall.framework.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.*;

/**
 * Created by wacao on 14-9-24.
 */
public class WorkerHandler implements IWorkerHandler {
    protected WorkerExecutor mExecutor;
    private Handler mHandler;
    private Looper mLooper;

    public WorkerHandler(Looper looper) {
        if (looper == null)
            looper = Looper.getMainLooper();
        mLooper = looper;
        mHandler = new Handler(looper);
        mExecutor = new WorkerExecutor(8, 8, 10L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>(8), new WorkerThreadFactory(), null);
    }

    public Looper getLoopper() {
        return mLooper;
    }


    @Override
    public <T> Worker<T> submit(Worker<T> worker) {
        T result = null;
        if (mExecutor != null) {
            Worker<T> future = mExecutor.submit(worker, null);
            try {
                result = future.get(future.getTimeout(), TimeUnit.SECONDS);
            } catch (CancellationException ce) {
                future.setState(IStateTask.State.Canceled);
            } catch (ExecutionException ee) {
                future.setState(IStateTask.State.Error);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
                future.setState(IStateTask.State.Interupted);
            } catch (TimeoutException te) {
                future.setState(IStateTask.State.Timeout);
            } finally {
                return future;
            }
        } else {
            return null;
        }

    }
}
