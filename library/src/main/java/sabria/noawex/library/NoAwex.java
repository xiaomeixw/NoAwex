package sabria.noawex.library;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import sabria.noawex.library.core.promises.NoAwexPromise;
import sabria.noawex.library.core.promises.Promise;
import sabria.noawex.library.core.thread.ThreadHelper;
import sabria.noawex.library.core.thread.pool.PoolManager;
import sabria.noawex.library.core.thread.pool.PoolPolicy;
import sabria.noawex.library.core.thread.queue.TaskQueue;
import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.core.thread.work.RealTimeWorker;
import sabria.noawex.library.core.thread.work.Worker;
import sabria.noawex.library.core.thread.work.WorkerListener;
import sabria.noawex.library.exception.AbsentValueException;
import sabria.noawex.library.state.PoolStateImpl;
import sabria.noawex.library.state.QueueStateImpl;
import sabria.noawex.library.util.ArrayMap;
import sabria.noawex.library.util.Logger;
import sabria.noawex.library.util.Map;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  上午 09:33
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class NoAwex {

    private final ThreadHelper mThreadHelper;
    private final Logger mLogger;
    private final AtomicLong mWorkIdProvider =new AtomicLong();
    private final Map<Integer,TaskQueue> mTaskQueueMap;
    private final Map<Integer,Map<Integer,Worker>> mWorkers;
    private final PoolPolicy mPoolPolicy;
    private final AtomicInteger mThreadIdProvider = new AtomicInteger();
    private final ExecutorService mCallbackExecutor = Executors.newSingleThreadExecutor();
    private Timer mTimer;
    private final Map<Task,Task> mTasks = Map.Provider.getSync();
    private NoAwexPromise mPromise;


    public NoAwex(ThreadHelper helper,Logger logger,PoolPolicy poolPolicy){
        this.mThreadHelper=helper;
        this.mLogger=logger;
        this.mPoolPolicy=poolPolicy;
        this.mTaskQueueMap=Map.Provider.getSync();
        this.mWorkers=Map.Provider.getSync();
        mTimer=new Timer();
        initializerPromise();
        mPoolPolicy.initialize(new PoolManagerImpl());

    }

    private void initializerPromise() {
        mPromise=new NoAwexPromise(this);
        mPromise.reject(new AbsentValueException());
    }


    public <Result,Progress> Promise<Result,Progress> submit(Task<Result,Progress> task){
        task.initialize(this);
        PoolStateImpl poolState = extractPoolState();
        mPoolPolicy.onTaskAdded(poolState,task);
        poolState.recycle();
        return task.getPromise();
    }

    public void submit(Runnable runnable){
        mCallbackExecutor.submit(runnable);
    }

    private PoolStateImpl extractPoolState() {
        PoolStateImpl poolState = PoolStateImpl.get();
        extractQueueState(poolState);
        poolState.setTasks(mTasks);
        return poolState;

    }

    private void extractQueueState(PoolStateImpl poolState) {
        ArrayMap<Integer, TaskQueue> queues = mTaskQueueMap.clone();
        for(TaskQueue queue : queues.values()){
            QueueStateImpl queueState = QueueStateImpl.get(queue.getId(), queue.size(), queue.waiters());
            extractWorkersInfo(queueState);
            poolState.addQueue(queue.getId(),queueState);
        }
    }

    private void extractWorkersInfo(QueueStateImpl queueState) {
        ArrayMap<Integer, Worker> workers = mWorkers.get(queueState.getId()).clone();
        for(Worker worker : workers.values()){
            queueState.addWorker(worker.getId(),worker.takeState());
        }
    }


    public long provideWorkId() {
        return mWorkIdProvider.incrementAndGet();
    }

    public Logger provideLogger() {
        return mLogger;
    }

    public ThreadHelper provideUIThread(){
        return mThreadHelper;
    }

    public <Result,Progress> void onTaskQueueTimeout(Task<Result,Progress> task) {
        PoolStateImpl poolState = extractPoolState();
        mPoolPolicy.onTaskQueueTimeout(poolState,task);
        poolState.recycle();
    }

    public <Result,Progress> void onTaskExecutionTimeout(Task<Result,Progress> task) {
        PoolStateImpl poolState = extractPoolState();
        mPoolPolicy.onTaskExecutionTimeout(poolState,task);
        poolState.recycle();
    }

    public void schedule(TimerTask timerTask, int timeout) {
        if(timeout >0){
            mTimer.schedule(timerTask,timeout);
        }
    }


    /**
     * PoolManager实现类
     */
    private class PoolManagerImpl implements PoolManager {
        /**
         * 创建一个QueueTask
         * @param queueId
         */
        @Override
        public void createQueue(int queueId) {
            if(mTaskQueueMap.containsKey(queueId)){
                throw new IllegalStateException("Trying to create a queue with an id that already exists");
            }
            mTaskQueueMap.put(queueId,new TaskQueue(queueId));
        }

        /**
         * 级联remove所有相关work-thread和 Task
         * @param queueId
         */
        @Override
        public void removeQueue(int queueId) {
            TaskQueue taskQueue = mTaskQueueMap.remove(queueId);
            Map<Integer, Worker> workersOfQueue = mWorkers.remove(queueId);
            for(Worker worker : workersOfQueue.values()){
                worker.die();
            }
            taskQueue.destory();
            for(Worker worker : workersOfQueue.values()){
                worker.interrupt();
            }
        }

        @Override
        public void executeImmediately(Task task) {
            task.markQueue(null);
            new RealTimeWorker(mThreadIdProvider.incrementAndGet(),task,mThreadHelper,mLogger);
        }

        @Override
        public void queueTask(int queueId, Task task) {
            TaskQueue taskQueue = mTaskQueueMap.get(queueId);
            task.markQueue(taskQueue);
            taskQueue.insert(task);
            mTasks.put(task,task);
        }

        @Override
        public void mergeTask(Task taskInQueue, Task taskToMerge) {
            if (taskInQueue.getState() <= Task.STATE_NOT_QUEUE) {
                throw new IllegalStateException("Task not queued");
            }
            if (taskToMerge.getState() != Task.STATE_NOT_QUEUE) {
                throw new IllegalStateException("Task already queue");
            }

            taskToMerge.markQueue(null);
            taskInQueue.getPromise().pipe(taskToMerge.getPromise());
        }

        @Override
        public int createWorker(int queueId, int priority) {
            TaskQueue taskQueue = mTaskQueueMap.get(queueId);
            Map<Integer, Worker> workersOfQueue = mWorkers.get(queueId);
            if(workersOfQueue == null){
                workersOfQueue = Map.Provider.get();
                mWorkers.put(queueId,workersOfQueue);
            }
            int id = mThreadIdProvider.incrementAndGet();
            workersOfQueue.put(id,new Worker(id,priority,taskQueue,mThreadHelper,mLogger,mWorkerListener));
            return id;
        }

        @Override
        public void removeWorker(int queueId, int workId, boolean shouldInterrupt) {
            Worker worker = mWorkers.get(queueId).remove(workId);
            if(worker!=null){
                if(worker !=null){
                    worker.interrupt();
                }else{
                    worker.die();
                }
            }
        }
    }

    private final WorkerListener mWorkerListener = new WorkerListener() {
        @Override
        public void onTaskFinished(Task task) {
            PoolStateImpl poolState = extractPoolState();
            mPoolPolicy.onTaskFinished(poolState,task);
            poolState.recycle();
            mTasks.remove(task);
        }
    };
}
