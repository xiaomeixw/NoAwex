package sabria.noawex.library.exception;

import android.os.Message;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 06:26
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class AbsentValueException extends Exception {

    public AbsentValueException(){
        super("Promise rejected without any value");
    }

}
