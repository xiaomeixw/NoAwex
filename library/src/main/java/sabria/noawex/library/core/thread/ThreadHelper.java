package sabria.noawex.library.core.thread;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 12:38
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface ThreadHelper {

    boolean isCurrentThread();

    void post(Runnable runnable);

    void setUpPriorityToCurrentThread(int priority);

    void setUpPriorityToRealTimeThread();


}
