package sabria.noawex.library.core.thread.pool;

import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.state.PoolState;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 04:43
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public abstract class PoolPolicy {

    private PoolManager mPoolManager;

    public void initialize(PoolManager poolManager){
        this.mPoolManager=poolManager;
        onStartUp();
    }

    /**
     * The pool is starting-up, its time to create the basic work queues and workers
     */
    public abstract void onStartUp();


    public void createQueue(int queueId){
        mPoolManager.createQueue(queueId);
    }

    public void removeQueue(int queueId){
        mPoolManager.removeQueue(queueId);
    }

    public void queueTask(int queueId,Task task){
        mPoolManager.queueTask(queueId, task);
    }

    public void mergeTask(Task taskInQueue,Task taskToMerge){
        mPoolManager.mergeTask(taskInQueue, taskToMerge);
    }

    public void executeImmediately(Task task){
        mPoolManager.executeImmediately(task);
    }

    public int createWorker(int queueId,int priority){
        return mPoolManager.createWorker(queueId,priority);
    }

    public void removeWorker(int queueId, int workerId){
        removeWorker(queueId,workerId,false);
    }

    public void removeWorker(int queueId, int workerId, boolean interrupt){
        mPoolManager.removeWorker(queueId, workerId, interrupt);
    }

    public abstract void onTaskAdded(PoolState poolState,Task task);
    public abstract void onTaskFinished(PoolState poolState,Task task);
    public abstract void onTaskQueueTimeout(PoolState poolState,Task task);
    public abstract void onTaskExecutionTimeout(PoolState poolState,Task task);

}
