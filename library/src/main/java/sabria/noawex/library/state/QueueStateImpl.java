package sabria.noawex.library.state;

import sabria.noawex.library.util.Map;
import sabria.noawex.library.util.ObjectPool;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 05:28
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class QueueStateImpl implements QueueState {


    private int mId;
    private int mEnqueue;
    private int mWaiters;
    private final Map<Integer, WorkerStateImpl> mWorkers = Map.Provider.get();

    private final static ObjectPool<QueueStateImpl> sObjectPool = new ObjectPool<>(30);

    private QueueStateImpl(){

    }

    public static QueueStateImpl get(int id, int enqueue, int waiters) {
        QueueStateImpl queueState;
        synchronized (sObjectPool) {
            queueState = sObjectPool.acquire();
            if (queueState == null) {
                queueState = new QueueStateImpl();
            }
            queueState.mId = id;
            queueState.mEnqueue = enqueue;
            queueState.mWaiters = waiters;
        }
        return queueState;
    }

    /**
     * Id of the queue
     */
    @Override
    public int getId() {
        return mId;
    }

    /**
     * Total number of tasks in the queue
     */
    @Override
    public int getEnqueue() {
        return mEnqueue;
    }

    /**
     * Total number of mWorkers waiting for work
     */
    @Override
    public int getWaiters() {
        return mWaiters;
    }

    @Override
    public int numberOfWorkers() {
        return mWorkers.size();
    }

    public void addWorker(int id,WorkerStateImpl workerState){
        mWorkers.put(id,workerState);
    }

    public void recycle() {
        for (WorkerStateImpl workerState : mWorkers.values()) {
            workerState.recycle();
        }
        mWorkers.clear();
        synchronized (sObjectPool) {
            sObjectPool.release(this);
        }
    }

    @Override
    public String toString() {
        return "QueueStateImpl{" +
                "mId=" + mId +
                ", mEnqueue=" + mEnqueue +
                ", mWaiters=" + mWaiters +
                ", mWorkers=" + mWorkers +
                '}';
    }
}
