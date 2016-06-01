package sabria.noawex.library.state;

import java.util.concurrent.locks.LockSupport;

import sabria.noawex.library.core.thread.task.Task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:48
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface WorkerState {

    int getId();

    State getState();

    Task getCurrentTask();

    long getLastTimeActive();

    enum State{

        /**
         * State for a worker which has not yet started.
         */
        NEW,

        /**
         * State for a runnable worker.
         */
        RUNNABLE,

        /**
         * State for a worker blocked waiting for a monitor lock.
         */
        BLOCKED,

        /**
         * State for a waiting worker.
         */
        WAITING,

        /**
         * State for a waiting worker with a specified waiting time.
         */
        TIMED_WAITING,

        /**
         * State for terminated worker. No more tasks will be executed by this worker.
         */
        TERMINATED,

        /**
         * State for a worker waiting for the next task.
         */
        WAITING_FOR_NEXT_TASK




    }



}
