package sabria.noawex.library.core.thread.pool;

import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.state.PoolState;
import sabria.noawex.library.state.QueueState;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 05:05
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class PoolPolicyImp extends PoolPolicy {

    /**
     * queue的id的默认值是1
     */
    private static final int QUEUE_ID = 1;

    /**
     * 执行级别
     */
    private final int mDefaultPriority;

    /**
     * 最大的Thread数
     */
    private final int mMaxThreads;


    /**
     * 如果外部没有指定几个Thread,那么我们就按照CPU核心数来处理
     * @param defaultPriority
     */
    public PoolPolicyImp(int defaultPriority){
        this(defaultPriority,Runtime.getRuntime().availableProcessors());
    }

    public PoolPolicyImp(int defaultPriority,int maxThreads){
        this.mDefaultPriority=defaultPriority;
        this.mMaxThreads=maxThreads;
    }

    /**
     * 创建 Queue 和 Worker
     */
    @Override
    public void onStartUp() {
        createQueue(QUEUE_ID);
        createWorker(QUEUE_ID,mDefaultPriority);
    }

    /**
     *
     * @param poolState
     * @param task
     */
    @Override
    public void onTaskAdded(PoolState poolState, Task task) {
        QueueState queueState = poolState.getQueue(QUEUE_ID);

        //是否是RealTimeTask(必须马上执行的Task)
        boolean isRealTimeTask = task.getPriority() == Task.PRIORITY_REAL_TIME;
        if(isRealTimeTask && (queueState.getEnqueue()!=0 || queueState.getWaiters()==0)){
            executeImmediately(task);
        }else{
            //走 queue  --- worker  --- task
            if(queueState.getWaiters() == 0 && queueState.numberOfWorkers() < mMaxThreads){
                createWorker(QUEUE_ID,mDefaultPriority);
            }
            queueTask(QUEUE_ID,task);
        }

    }

    @Override
    public void onTaskFinished(PoolState poolState, Task task) {

    }

    @Override
    public void onTaskQueueTimeout(PoolState poolState, Task task) {
        task.getPromise().cancelTask();
    }

    @Override
    public void onTaskExecutionTimeout(PoolState poolState, Task task) {
        task.getPromise().cancelTask();
    }
}
