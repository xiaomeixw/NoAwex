package sabria.noawex.library.core.thread.pool;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 04:44
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface PoolManager {

    void createQueue(int queueId);
    void removeQueue(int queueId);

    void executeImmediately(Task task);
    void queueTask(int queueId,Task task);
    void mergeTask(Task taskInQueue,Task taskToMerge);

    int createWorker(int queueId,int priority);
    void removeWorker(int queueId,int workId,boolean shouldInterrupt);


}
