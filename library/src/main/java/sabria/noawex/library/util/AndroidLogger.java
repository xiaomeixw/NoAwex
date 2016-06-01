package sabria.noawex.library.util;

import android.util.Log;

import sabria.noawex.library.BuildConfig;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 12:48
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class AndroidLogger implements Logger {

    private static final String TAG="noawex";

    @Override
    public boolean isEnabled() {
        return BuildConfig.DEBUG;
    }

    @Override
    public void i(String message) {
        Log.i(TAG,message);
    }

    @Override
    public void e(String message, Exception ex) {
        Log.e(TAG,message,ex);
    }
}
