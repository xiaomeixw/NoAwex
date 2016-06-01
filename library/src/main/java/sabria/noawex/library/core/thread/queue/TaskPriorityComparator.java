package sabria.noawex.library.core.thread.queue;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 04:28
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class TaskPriorityComparator implements java.util.Comparator<Task> {
    @Override
    public int compare(Task lhs, Task rhs) {
        return lhs.getPriority() > rhs.getPriority() ? -1 : (lhs.getPriority() == rhs.getPriority() ? 0 : 1);
    }
}
