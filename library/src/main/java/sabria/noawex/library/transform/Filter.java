package sabria.noawex.library.transform;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  上午 11:06
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface Filter<T> {
    boolean filter(T value);
}
