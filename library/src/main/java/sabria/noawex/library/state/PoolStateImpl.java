package sabria.noawex.library.state;


import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.util.Map;
import sabria.noawex.library.util.ObjectPool;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 05:25
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class PoolStateImpl implements PoolState{

    private final Map<Integer,QueueStateImpl> mQueueStateMap= Map.Provider.get();
    private Map<Task,Task> mTasks;

    private PoolStateImpl(){
    }

    private final static ObjectPool<PoolStateImpl> sObjectPool = new ObjectPool<>(30);

    public static PoolStateImpl get(){
        PoolStateImpl poolState;
        synchronized (sObjectPool){
            poolState = sObjectPool.acquire();
            if(poolState==null){
                poolState=new PoolStateImpl();
            }
        }
        return poolState;
    }

    @Override
    public QueueState getQueue(int queueId) {
        return mQueueStateMap.get(queueId);
    }

    public void addQueue(int queueId, QueueStateImpl queueState){
        mQueueStateMap.put(queueId,queueState);
    }

    public void setTasks(Map<Task,Task> tasks){
        mTasks=tasks;
    }

    public void recycle() {
        synchronized (sObjectPool) {
            for (QueueStateImpl queueState : mQueueStateMap.values()) {
                queueState.recycle();
            }
            mQueueStateMap.clear();
            sObjectPool.release(this);
        }
    }

    /**
     * Search for a task that has the same type and hash code in the queue. You should override
     */
    @Override
    public Task getEqualTaskInQueue(Task task) {
        return mTasks.get(task);
    }

    @Override
    public String toString() {
        return "PoolStateImpl{" +
                "mQueueStateMap=" + mQueueStateMap +
                ", mTasks=" + mTasks +
                '}';
    }
}
