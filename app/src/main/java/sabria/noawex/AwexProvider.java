package sabria.noawex;

import sabria.noawex.library.NoAwex;
import sabria.noawex.library.core.thread.AndroidThreadHelper;
import sabria.noawex.library.core.thread.pool.PoolPolicyImp;
import sabria.noawex.library.util.AndroidLogger;

/**
 * Created by xiong,An android project Engineer,on 1/6/2016.
 * Data:1/6/2016  上午 12:28
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class AwexProvider {



    private static NoAwex sInstance = null;

    public static synchronized NoAwex get() {
        if (sInstance == null) {
            sInstance = new NoAwex(new AndroidThreadHelper(),
                    new AndroidLogger(),
                    new PoolPolicyImp(0, 1));
        }
        return sInstance;
    }

}
