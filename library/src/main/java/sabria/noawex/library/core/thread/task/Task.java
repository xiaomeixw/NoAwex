package sabria.noawex.library.core.thread.task;

import android.util.Log;

import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import sabria.noawex.library.NoAwex;
import sabria.noawex.library.core.promises.NoAwexPromise;
import sabria.noawex.library.core.promises.Promise;
import sabria.noawex.library.core.thread.queue.TaskQueue;
import sabria.noawex.library.core.thread.work.Worker;
import sabria.noawex.library.util.Logger;
import sabria.noawex.library.util.Utils;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:05
 * Base on clever-m.com(JAVA Service)
 * Describe:
 *
 * 对于一个Task对象而言，它所承载的只有一些个体逻辑方法(包含自我状态):run()
 *
 * 它归属于Work对象，work对象是一个Runnable对象,它才执行run(task.execute)方法
 *
 * 而work对象又归属于TaskQueue中,归属于TaskQueue管理
 *
 * Version:1.0
 * Open source
 */
public abstract class Task<Result,Progress> {

    public static final int STATE_NOT_INITIALIZED = -1;
    public static final int STATE_NOT_QUEUE = 0;
    public static final int STATE_QUEUE = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_FINISHED = 3;
    public static final int STATE_CANCELLING = 4;
    public static final int STATE_CANCELLED = 5;

    public static final int PRIORITY_LOWEST = 1;
    public static final int PRIORITY_LOW = 2;
    public static final int PRIORITY_NORMAL = 3;
    public static final int PRIORITY_HIGH = 4;
    public static final int PRIORITY_REAL_TIME = Integer.MAX_VALUE;
    private int mCurrentState = STATE_NOT_INITIALIZED;

    private final int mPriority;
    private final int mQueueTimeout;
    private final int mExecutionTimeout;

    private long mId;
    private Worker mWorker;
    private NoAwex mNoAwex;
    private Logger mLogger;
    private NoAwexPromise mPromise;
    private TaskQueue mTaskQueue;
    private TimerTask mQueueTimeoutTimerTask;
    private TimerTask mExecutionTimeoutTimerTask;


    public Task() {
        this(PRIORITY_NORMAL, -1, -1);
    }

    public Task(int priority) {
        this(priority, -1, -1);
    }

    public Task(int priority, int queueTimeout, int executionTimeout) {
        this.mPriority = priority;
        this.mQueueTimeout = queueTimeout;
        this.mExecutionTimeout = executionTimeout;
    }

    public void initialize(NoAwex noAwex) {
        if (mCurrentState != STATE_NOT_INITIALIZED) {
            throw new IllegalStateException("Trying to reuse an already submitted task");
        }
        this.mNoAwex = noAwex;
        mId = noAwex.provideWorkId();
        mLogger = noAwex.provideLogger();
        mCurrentState = STATE_NOT_QUEUE;
        Utils.printStateChanged(mLogger, mId, "NOT_QUEUE");
        mPromise = new NoAwexPromise<>(mNoAwex, this);
        mQueueTimeoutTimerTask = getTimeoutTimerTask();
        mExecutionTimeoutTimerTask=getExecutionTimerTask();
    }

    /**
     * 去拿Timeout-TimerTask对象
     * @return
     */
    private TimerTask getTimeoutTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (mTaskQueue != null && mTaskQueue.remove(Task.this)) {
                    mNoAwex.onTaskQueueTimeout(Task.this);
                }
            }
        };
    }

    /**
     * 去拿Execution-TimerTask对象
     * @return
     */
    private TimerTask getExecutionTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                mNoAwex.onTaskExecutionTimeout(Task.this);
            }
        };
    }




    public long getId() {
        Utils.checkInitialized(mCurrentState);
        return mId;
    }

    public int getPriority() {
        return mPriority;
    }

    public int getState(){
        return mCurrentState;
    }

    public boolean isCancelled(){
        return mCurrentState==STATE_CANCELLING||mCurrentState==STATE_CANCELLED;
    }

    public Promise<Result,Progress> getPromise(){
        Utils.checkInitialized(mCurrentState);
        return mPromise;
    }

    public final void setWorker(Worker worker){
       this.mWorker=worker;
    }

    public Worker getWorker() {
        return mWorker;
    }

    public final void markQueue(TaskQueue taskQueue){
        Utils.checkInitialized(mCurrentState);
        this.mTaskQueue=taskQueue;
        mCurrentState=STATE_QUEUE;
        Utils.printStateChanged(mLogger,mId,"QUEUE");
        mNoAwex.schedule(mQueueTimeoutTimerTask,mQueueTimeout);
    }

    public TaskQueue getTaskQueue() {
        return mTaskQueue;
    }

    public final void execute() throws InterruptedException {
        Utils.checkInitialized(mCurrentState);

        mQueueTimeoutTimerTask.cancel();
        mCurrentState=STATE_RUNNING;
        Utils.printStateChanged(mLogger,mId,"RUNNING");
        mNoAwex.schedule(mExecutionTimeoutTimerTask, mExecutionTimeout);

        Log.e("State1",mPromise.getState()+"");

        //
        Result result=null;
        try {
            result=run();
        }catch (InterruptedException ex){
            if(mPromise.isCancelled()){
                mCurrentState=STATE_CANCELLED;
                Utils.printStateChanged(mLogger,mId,"CANCELLED");
            }
            Thread.currentThread().interrupt();
            throw ex;
        }catch (Exception ex){
            mPromise.reject(ex);
        }finally {
            mExecutionTimeoutTimerTask.cancel();
        }

        Log.e("State2",mPromise.getState()+"");

        //方法完成后，必须制空状态为1
        resolveWithResult(result);

        Log.e("State3", mPromise.getState() + "");

    }

    private ReentrantLock lock=new ReentrantLock();
    private void resolveWithResult(Result result) {
        try {
            lock.lock();
            //在task执行任务后，将promise的状态由pending转成resolve
            if(mPromise.isPending()){
                mPromise.resolve(result);
            }

            if (mCurrentState == STATE_CANCELLING) {
                mCurrentState = STATE_CANCELLED;
                Utils.printStateChanged(mLogger, mId, "CANCELLED");
                return;
            }

            mCurrentState = STATE_FINISHED;
            Utils.printStateChanged(mLogger, mId, "FINISHED");

        }finally {
            mWorker = null;
            lock.unlock();
        }
    }


    protected abstract Result run() throws InterruptedException;

    public final void softCancel(){
        try {
            lock.lock();
            Utils.checkInitialized(mCurrentState);
            mCurrentState=STATE_CANCELLING;
            Utils.printStateChanged(mLogger,mId,"CANCELLING");
        }finally {
            lock.unlock();
        }
    }

    public void reset(){
        try {
            lock.lock();
            if(mCurrentState!=STATE_FINISHED){
                throw new IllegalStateException("Trying to reuse an already submitted task");
            }
            mCurrentState=STATE_NOT_INITIALIZED;
            onReset();

        }finally {
            lock.unlock();
        }
    }

    /**
     * Override this method to reset any state of the task prior to reuse it
     */
    protected void onReset() {
    }


    @Override
    public String toString() {
        return "Task{" +
                "mCurrentState=" + mCurrentState +
                ", mPriority=" + mPriority +
                ", mQueueTimeout=" + mQueueTimeout +
                ", mExecutionTimeout=" + mExecutionTimeout +
                ", mId=" + mId +
                ", mWorker=" + mWorker +
                ", mNoAwex=" + mNoAwex +
                ", mLogger=" + mLogger +
                ", mPromise=" + mPromise +
                ", mTaskQueue=" + mTaskQueue +
                ", mQueueTimeoutTimerTask=" + mQueueTimeoutTimerTask +
                ", mExecutionTimeoutTimerTask=" + mExecutionTimeoutTimerTask +
                ", lock=" + lock +
                '}';
    }
}
