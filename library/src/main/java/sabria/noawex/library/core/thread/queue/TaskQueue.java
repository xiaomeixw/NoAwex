package sabria.noawex.library.core.thread.queue;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.core.thread.work.Worker;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:13
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class TaskQueue {

    private static final int INITIAL_CAPACITY = 4;
    private final PriorityBlockingQueue<Task> mTaskQueue;
    //等待的count
    private final AtomicInteger mWaitersCount = new AtomicInteger();
    private final AtomicInteger mSize = new AtomicInteger();
    private final int mId;
    private boolean mDie=false;

    public TaskQueue(int id){
        this.mId=id;
        mTaskQueue=new PriorityBlockingQueue<>(INITIAL_CAPACITY,new TaskPriorityComparator());

    }


    public Task take(Worker worker) throws InterruptedException{
        try{
            if(mDie){
                throw new IllegalStateException("Queue is die!");
            }
            mWaitersCount.incrementAndGet();
            Task task = mTaskQueue.take();
            mSize.decrementAndGet();
            task.setWorker(worker);
            return task;
        }finally {
            mWaitersCount.decrementAndGet();
        }

    }

    public synchronized  void insert(Task task){
        if(mDie){
            throw new IllegalStateException("Queue is die!");
        }
        mTaskQueue.offer(task);
        mSize.incrementAndGet();
    }




    public synchronized <Result, Progress> boolean remove(Task<Result, Progress> task) {
        if(mDie){
            throw new IllegalStateException("Queue is die!");
        }

        boolean removed = mTaskQueue.remove(task);
        if(removed){
            mSize.decrementAndGet();
        }
        return removed;
    }

    public synchronized void destory(){
        synchronized (this){
            mDie=true;
        }
        for(Task task:mTaskQueue){
            task.getPromise().cancelTask();
        }
    }


    public int waiters(){
        return mWaitersCount.get();
    }

    public int size(){
        return mSize.get();
    }

    public int getId(){
        return mId;
    }




}
