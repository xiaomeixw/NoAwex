package sabria.noawex.library.state;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 04:56
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface PoolState {

    QueueState getQueue(int queueId);

    Task getEqualTaskInQueue(Task task);

}
