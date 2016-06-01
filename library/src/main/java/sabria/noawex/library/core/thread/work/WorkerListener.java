package sabria.noawex.library.core.thread.work;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:27
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface WorkerListener {

    void onTaskFinished(Task task);

}
