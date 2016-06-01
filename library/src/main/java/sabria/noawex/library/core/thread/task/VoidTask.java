package sabria.noawex.library.core.thread.task;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:09
 * Base on clever-m.com(JAVA Service)
 * Describe: Base class for tasks that doesn't returns any result.
 * Version:1.0
 * Open source
 */
public abstract class VoidTask extends Task<Void,Void> {

    public VoidTask(){
        super();
    }

    public VoidTask(int priority){
        super(priority);
    }


    @Override
    protected Void run() throws InterruptedException{
        runWithoutResult();
        return null;
    }

    protected abstract  void runWithoutResult()throws InterruptedException;
}
