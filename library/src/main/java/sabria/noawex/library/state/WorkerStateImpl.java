package sabria.noawex.library.state;

import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.util.ObjectPool;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:54
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class WorkerStateImpl implements WorkerState {

    private int mId;
    private State mState;
    private Task mCurrentTask;
    private long mLastTimeActive;

    private final static ObjectPool<WorkerStateImpl> sObjectPool = new ObjectPool<>(30);

    private WorkerStateImpl(){

    }

    public static WorkerStateImpl get(int id,State state, Task currentTask, long lastTimeActive){
        WorkerStateImpl workerState;
        synchronized (sObjectPool){
            workerState = sObjectPool.acquire();
            if(workerState==null){
                workerState=new WorkerStateImpl();
            }
            workerState.mId=id;
            workerState.mState=state;
            workerState.mCurrentTask=currentTask;
            workerState.mLastTimeActive=lastTimeActive;
        }
        return workerState;
    }

    @Override
    public int getId() {
        return mId;
    }

    @Override
    public State getState() {
        return mState;
    }

    @Override
    public Task getCurrentTask() {
        return mCurrentTask;
    }

    @Override
    public long getLastTimeActive() {
        return mLastTimeActive;
    }

    public void recycle(){
        mCurrentTask=null;
        synchronized (sObjectPool){
            sObjectPool.release(this);
        }
    }

    @Override
    public String toString() {
        return "WorkerStateImpl{" +
                "mId=" + mId +
                ", mState=" + mState +
                ", mCurrentTask=" + mCurrentTask +
                ", mLastTimeActive=" + mLastTimeActive +
                '}';
    }
}
