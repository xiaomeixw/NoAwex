package sabria.noawex.library.util;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:22
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class Utils {

    public static void printStateChanged(Logger mLogger,Long mId,String newState) {
        if (mLogger.isEnabled()) {
            StringBuilder stringBuilder = new StringBuilder();
            mLogger.i(stringBuilder.append("Task ")
                    .append(mId)
                    .append(" state changed to ")
                    .append(newState)
                    .toString());
        }
    }

    /**
     * 检查是否初始化
     */
    public static  void checkInitialized(int currentState) {
        if (currentState == Task.STATE_NOT_INITIALIZED) {
            throw new IllegalStateException("Task not already initialized, before calling this method ensure this task is submitted");
        }
    }
}
