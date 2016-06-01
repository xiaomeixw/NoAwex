package sabria.noawex.library.state;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 05:13
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface QueueState {

    int getId();

    int getEnqueue();

    int getWaiters();

    int numberOfWorkers();


}
