package sabria.noawex.library.core.thread;

import android.os.*;
import android.os.Process;


/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 12:39
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class AndroidThreadHelper implements ThreadHelper {

    private final Handler mMainHandler;

    public AndroidThreadHelper(){
        this.mMainHandler=new Handler(Looper.getMainLooper());
    }

    @Override
    public boolean isCurrentThread() {
        //6.0
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return Looper.getMainLooper().isCurrentThread();
        }else{
            return Thread.currentThread()==Looper.getMainLooper().getThread();
        }
    }

    @Override
    public void post(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    @Override
    public void setUpPriorityToCurrentThread(int priority) {
        android.os.Process.setThreadPriority(priority);
    }

    @Override
    public void setUpPriorityToRealTimeThread() {
        setUpPriorityToCurrentThread(Process.THREAD_PRIORITY_DEFAULT+ Process.THREAD_PRIORITY_LESS_FAVORABLE);
    }
}
