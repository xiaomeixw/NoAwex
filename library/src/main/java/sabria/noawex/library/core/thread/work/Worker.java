package sabria.noawex.library.core.thread.work;

import sabria.noawex.library.core.thread.ThreadHelper;
import sabria.noawex.library.core.thread.queue.TaskQueue;
import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.state.WorkerState;
import sabria.noawex.library.state.WorkerStateImpl;
import sabria.noawex.library.util.Logger;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:13
 * Base on clever-m.com(JAVA Service)
 * Describe:归属于Queue管理的Work
 * Version:1.0
 * Open source
 */
public class Worker implements Runnable {

    private final int mId;
    private final Thread mThread;
    private final ThreadHelper mThreadHelper;
    private final TaskQueue mWorkQueue;
    private final Logger mLogger;
    private final WorkerListener mListener;
    private final int mPriority;

    private boolean mDie = false;
    private Task mCurrentTask;
    private boolean mExecutingTask;
    private long mLastTimeActive;

    public Worker(int id, int priority, TaskQueue workQueue, ThreadHelper threadHelper, Logger logger, WorkerListener listener) {
        mId = id;
        mPriority = priority;
        mThreadHelper = threadHelper;
        mThread = new Thread(this, " worker " + id);
        mWorkQueue = workQueue;
        mLogger = logger;
        mListener = listener;
        mThread.start();
    }

    @Override
    public void run() {
        mThreadHelper.setUpPriorityToCurrentThread(mPriority);
        if (mLogger.isEnabled()) {
            mLogger.i("Worker " + mId + " starting...");
        }
        try {
            while (!mDie) {

                try {

                    Task newTask = mWorkQueue.take(this);
                    synchronized (this) {
                        mCurrentTask = newTask;
                        mExecutingTask = true;
                        mLastTimeActive = mCurrentTask != null ? System.nanoTime() / 10000000 : mLastTimeActive;
                    }
                    if (mCurrentTask != null) {
                        long taskId = mCurrentTask.getId();
                        if (mLogger.isEnabled()) {
                            mLogger.i("Worker " + mId + " start executing task " + taskId);
                        }
                        mCurrentTask.execute();
                        if (mLogger.isEnabled()) {
                            mLogger.i("Worker " + mId + " ends executing task " + taskId);
                        }
                    }

                } catch (InterruptedException ex) {
                    return;
                } finally {
                    Task executedTask = this.mCurrentTask;
                    synchronized (this){
                        mCurrentTask = null;
                        mExecutingTask = false;
                    }
                    if(executedTask!=null){
                        mListener.onTaskFinished(executedTask);
                    }
                }
            }
        } finally {
            if (mLogger.isEnabled()) {
                mLogger.i("Worker " + mId + " dies");
            }
        }


    }

    public synchronized WorkerStateImpl takeState(){
        return WorkerStateImpl.get(mId,getState(),mCurrentTask,mLastTimeActive);
    }

    private WorkerState.State getState() {
        if (!mExecutingTask) {
            return WorkerState.State.WAITING_FOR_NEXT_TASK;
        }

        Thread.State state = mThread.getState();

        switch (state){
            case RUNNABLE:
                return WorkerState.State.RUNNABLE;
            case BLOCKED:
                return WorkerState.State.BLOCKED;
            case WAITING:
                return WorkerState.State.WAITING;
            case TERMINATED:
                return WorkerState.State.TERMINATED;
            case TIMED_WAITING:
                return WorkerState.State.TIMED_WAITING;
        }
        throw new IllegalStateException("Worker in an illegal state");

    }


    public int getId() {
        return mId;
    }

    public void interrupt(){
        die();
        mThread.interrupt();
    }

    public void die() {
        synchronized (this){
            mDie=true;
            if(!mExecutingTask){
                mThread.interrupt();
            }
        }
    }

}
