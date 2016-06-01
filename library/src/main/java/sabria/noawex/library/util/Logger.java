package sabria.noawex.library.util;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 12:46
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface Logger {

    boolean isEnabled();

    void i(String message);

    void e(String message,Exception ex);

}
