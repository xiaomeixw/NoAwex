package sabria.noawex.library.core.promises;

import sabria.noawex.library.R;
import sabria.noawex.library.callback.ThenCallback;
import sabria.noawex.library.callback.background.AlwaysCallback;
import sabria.noawex.library.callback.background.CancelCallback;
import sabria.noawex.library.callback.background.DoneCallback;
import sabria.noawex.library.callback.background.FailCallback;
import sabria.noawex.library.callback.background.ProgressCallback;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  上午 10:09
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface Promise<Result,Progress> {

    /**
     * 状态
     */
    public enum State{

        /**
         * 执行中且未完成
         */
        STATE_PENDING,


        /**
         * 执行完成且成功
         */
        STATE_RESOLVED,

        /**
         * 执行拒绝
         */
        STATE_REJECTED,


        /**
         * 执行取消
         */
        STATE_CANCELLED
    }

    /**
     * 获取状态
     * @return
     */
    public State getState();

    /**
     * 是否在pending状态
     * @return
     */
    boolean isPending();

    /**
     * 是否在resolved状态
     * @return
     */
    boolean isResolved();

    /**
     * 是否在rejected状态
     * @return
     */
    boolean isRejected();

    /**
     * 是否在cancelled状态
     * @return
     */
    boolean isCancelled();

    boolean isCompleted();

    /**
     * Waiting for results
     * @return
     * @throws Exception
     */
    Result getResult() throws Exception;

    /**
     * you can get a default value in case of error
     * @return
     * @throws InterruptedException
     */
    Result getResultDefault() throws InterruptedException;


    Promise<Result,Progress> done(DoneCallback<Result> callback);

    Promise<Result,Progress> fail(FailCallback callback);

    Promise<Result,Progress> progress(ProgressCallback<Progress> callback);

    Promise<Result,Progress> always(AlwaysCallback callback);

    Promise<Result,Progress> cancel(CancelCallback callback);

    <R,P> Promise<R,P> then(ThenCallback<Result,R,P> callback);//old Promise result will be new Promise's args

    Promise<Result, Progress> pipe(Promise<Result, Progress> promise);


    void cancelTask();

    void cancelTask(boolean mayInterrupt);
}
