package sabria.noawex.library.core.thread.work;

import sabria.noawex.library.core.thread.ThreadHelper;
import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.util.Logger;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:12
 * Base on clever-m.com(JAVA Service)
 * Describe: 一个最简单的Thread线程类
 * Version:1.0
 * Open source
 */
public class RealTimeWorker implements Runnable{

    private final long mId;
    private final Task mTask;
    private final ThreadHelper mThreadHelper;
    private final Logger mLogger;

    public RealTimeWorker(long id,Task task,ThreadHelper helper,Logger logger){
        this.mId=id;
        Thread thread = new Thread(this, " real-time worker " + id);
        this.mTask=task;
        this.mThreadHelper=helper;
        this.mLogger=logger;
        thread.start();
    }



    @Override
    public void run() {
        mThreadHelper.setUpPriorityToRealTimeThread();
        if (mLogger.isEnabled()) {
            mLogger.i("Worker " + mId + " starting...");
        }
        try {
            if (mLogger.isEnabled()) {
                mLogger.i("Worker " + mId + " start executing task " + mTask.getId());
            }
            mTask.execute();
            if (mLogger.isEnabled()) {
                mLogger.i("Worker " + mId + " ends executing task " + mTask.getId());
            }
        } catch (InterruptedException ignored) {
        } finally {
            if (mLogger.isEnabled()) {
                mLogger.i("Worker " + mId + " dies");
            }
        }
    }
}
