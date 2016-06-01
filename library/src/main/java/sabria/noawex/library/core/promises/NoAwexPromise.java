package sabria.noawex.library.core.promises;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import sabria.noawex.library.NoAwex;
import sabria.noawex.library.R;
import sabria.noawex.library.callback.ThenCallback;
import sabria.noawex.library.callback.background.AlwaysCallback;
import sabria.noawex.library.callback.background.CancelCallback;
import sabria.noawex.library.callback.background.DoneCallback;
import sabria.noawex.library.callback.background.FailCallback;
import sabria.noawex.library.callback.background.ProgressCallback;
import sabria.noawex.library.callback.ui.UIAlwaysCallback;
import sabria.noawex.library.callback.ui.UICancelCallback;
import sabria.noawex.library.callback.ui.UIDoneCallback;
import sabria.noawex.library.callback.ui.UIFailCallback;
import sabria.noawex.library.core.thread.ThreadHelper;
import sabria.noawex.library.core.thread.task.Task;
import sabria.noawex.library.util.Logger;
import sabria.noawex.library.util.ObjectPool;
import sabria.noawex.library.util.Utils;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:06
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class NoAwexPromise<Result, Progress> implements ResolvablePromise<Result, Progress> {

    protected final NoAwex mNoAwex;
    private final Task mTask;

    private final long mId;
    private final ThreadHelper mThreadHelper;
    private final Logger mLogger;

    private State mState;
    private Callbacks<Result, Progress> mCallbacks;

    private Result mResult;
    private Exception mException;

    private final Object mProgressInOrderSyncObject = new Object();
    private final Object mBlockingObject = new Object();

    public NoAwexPromise(NoAwex noAwex) {
        this(noAwex, null);
    }

    public NoAwexPromise(NoAwex noAwex, Task task) {
        this.mNoAwex = noAwex;
        this.mTask = task;
        mId = mTask != null ? mTask.getId() : -1;
        mThreadHelper = noAwex.provideUIThread();
        mLogger = noAwex.provideLogger();
        mState = State.STATE_PENDING;
        mCallbacks = Callbacks.get();
        Utils.printStateChanged(mLogger, mId, "PENDING");
    }

    @Override
    public Promise<Result, Progress> done(final DoneCallback<Result> callback) {
        Log.e("State5", "" + getState());
        State state;
        synchronized (this) {
            state = mState;
            if (state == State.STATE_PENDING) {
                mCallbacks.mDoneCallbacks.add(callback);
            }
        }
        if (state == State.STATE_RESOLVED) {
            if (shouldExecuteInBackground(callback)) {
                mNoAwex.submit(new Runnable() {
                    @Override
                    public void run() {
                        tryTrigger(callback,mResult);
                    }
                });
            }else{
                triggerDone(callback);
            }
        }
        return this;
    }


    /**
     * 改回调起作用会出现在task.execute()中发生异常时,会先调用Promise.reject(ex);
     * @param callback
     * @return
     */
    @Override
    public Promise<Result, Progress> fail(final FailCallback callback) {
        State state;
        synchronized (this) {
            state = mState;
            if (state == State.STATE_PENDING) {
                mCallbacks.mFailCallbacks.add(callback);
            }
        }

        if (state == State.STATE_REJECTED) {
            if (shouldExecuteInBackground(callback)) {
                mNoAwex.submit(new Runnable() {
                    @Override
                    public void run() {
                        //fail回调
                        tryTrigger(callback, mException);
                    }
                });
            } else {
                triggerFail(callback);
            }
        }
        return this;
    }

    @Override
    public Promise<Result, Progress> progress(ProgressCallback<Progress> callback) {
        synchronized (this) {
            switch (mState) {
                case STATE_PENDING:
                    mCallbacks.mProgressCallbacks.add(callback);
                    break;
            }
        }
        return this;
    }

    @Override
    public Promise<Result, Progress> always(final AlwaysCallback callback) {
        State state;
        synchronized (this) {
            state = mState;
            if (state == State.STATE_PENDING) {
                mCallbacks.mAlwaysCallbacks.add(callback);
            }
        }
        switch (state) {
            case STATE_RESOLVED:
            case STATE_REJECTED:
                if (shouldExecuteInBackground(callback)) {
                    mNoAwex.submit(new Runnable() {

                        @Override
                        public void run() {
                            tryTrigger(callback);
                        }
                    });
                } else {
                    triggerAlways(callback);
                }
                break;
        }
        return this;
    }

    /**
     * 它与cancelTask关联
     * @param callback
     * @return
     */
    @Override
    public Promise<Result, Progress> cancel(final CancelCallback callback) {
        State state;
        synchronized (this) {
            state = mState;
            if (mState == State.STATE_PENDING) {
                mCallbacks.mCancelCallbacks.add(callback);
            }
        }
        if (state == State.STATE_CANCELLED) {
            if (shouldExecuteInBackground(callback)) {
                mNoAwex.submit(new Runnable() {

                    @Override
                    public void run() {
                        tryTrigger(callback);
                    }
                });
            } else {
                triggerCancel(callback);
            }
        }
        return this;
    }


    @Override
    public <R, P> Promise<R, P> then(ThenCallback<Result, R, P> callback) {
        return null;
    }

    @Override
    public Promise<Result, Progress> pipe(Promise<Result, Progress> promise) {
        return null;
    }


    /**
     * task执行完后会调到这里,这里就是修改状态的关键步骤
     * @param result
     * @return
     */
    @Override
    public Promise<Result, Progress> resolve(Result result) {
        List<DoneCallback<Result>> doneCallbacks;
        List<AlwaysCallback> alwaysCallbacks;
        synchronized (this) {
            validateInPendingState();

            mState = State.STATE_RESOLVED;
            Utils.printStateChanged(mLogger, mId, "RESOLVED");
            mResult = result;

            doneCallbacks = mCallbacks.cloneDoneCallbacks();
            alwaysCallbacks = mCallbacks.cloneAlwaysCallbacks();
            clearCallbacks();
        }

        if (doneCallbacks.size() > 0 || alwaysCallbacks.size() > 0) {
            triggerAllDones(doneCallbacks);
            triggerAllAlways(alwaysCallbacks);
        }

        return this;
    }

    private void validateInPendingState() {
        if (mState != State.STATE_PENDING) {
            throw new IllegalStateException("Illegal promise state for this operation");
        }
    }

    /**
     * 这里和fail搭配，它会优先于fail执行，目的就是让fail的状态正常
     * @param ex
     * @return
     */
    @Override
    public Promise<Result, Progress> reject(Exception ex) {
        List<FailCallback> failCallbacks;
        List<AlwaysCallback> alwaysCallbacks;
        synchronized (this){
            validateInPendingState();
            mState=State.STATE_REJECTED;//重置状态
            Utils.printStateChanged(mLogger,mId,"REJECTED");
            mException=ex;//从task执行发生异常出返回
            failCallbacks=mCallbacks.cloneFailCallbacks();
            alwaysCallbacks=mCallbacks.cloneAlwaysCallbacks();
            clearCallbacks();
        }
        if(failCallbacks.size()>0||alwaysCallbacks.size()>0){
            triggerAllFails(failCallbacks);
            triggerAllAlways(alwaysCallbacks);
        }
        return this;
    }

    @Override
    public void notifyProgress(Progress progress) {

    }

    @Override
    public State getState() {
        return mState;
    }

    @Override
    public boolean isPending() {
        return getState() == State.STATE_PENDING;
    }

    @Override
    public boolean isResolved() {
        return getState() == State.STATE_RESOLVED;
    }

    @Override
    public boolean isRejected() {
        return getState() == State.STATE_REJECTED;
    }

    @Override
    public boolean isCancelled() {
        return getState() == State.STATE_CANCELLED;
    }

    @Override
    public boolean isCompleted() {
        State state = getState();
        return state == State.STATE_RESOLVED || state == State.STATE_REJECTED || state == State.STATE_CANCELLED;
    }

    @Override
    public Result getResult() throws Exception {
        blockWhilePending();

        switch (mState) {
            case STATE_CANCELLED:
                throw new IllegalStateException("Couldn't get result from a cancelled promise");
            case STATE_REJECTED:
                throw mException;
            default: //Promise.STATE_RESOLVED:
                return mResult;
        }
    }

    private void blockWhilePending() throws InterruptedException {
        synchronized (mBlockingObject) {
            while (isPending()) {
                try {
                    mBlockingObject.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }

    @Override
    public Result getResultDefault() throws InterruptedException {
        return null;
    }


    private void triggerDone(final DoneCallback<Result> callback) {
        if(callback instanceof UIDoneCallback && !mThreadHelper.isCurrentThread()){
            mThreadHelper.post(new CancellableRunnable(){
                @Override
                public void execute() {
                    tryTrigger(callback,mResult);
                }
            });
        }else{
            tryTrigger(callback,mResult);
        }
    }

    private void tryTrigger(DoneCallback<Result> callback, Result result) {
        try{
            callback.onDone(result);
        }catch (Exception ex){
            mLogger.e("Error when trigger done callback", ex);
        }
    }

    private void tryTrigger(FailCallback callback, Exception exception) {
        try {
            callback.onFail(exception);
        } catch (Exception ex) {
            mLogger.e("Error when trigger fail callback", ex);
        }
    }

    private void triggerAllFails(Collection<FailCallback> failCallbacks) {
        for (final FailCallback callback : failCallbacks) {
            triggerFail(callback);
        }
    }

    private void triggerFail(final FailCallback callback) {
        if (callback instanceof UIFailCallback && !mThreadHelper.isCurrentThread()) {
            //主线程
            mThreadHelper.post(new CancellableRunnable() {
                @Override
                public void execute() {
                    tryTrigger(callback, mException);
                }
            });
        } else {
            tryTrigger(callback, mException);
        }
    }


    private boolean shouldExecuteInBackground(DoneCallback<Result> callback) {
        return mThreadHelper.isCurrentThread() && !(callback instanceof UIDoneCallback);
    }

    private boolean shouldExecuteInBackground(FailCallback callback) {
        return mThreadHelper.isCurrentThread() && !(callback instanceof UIFailCallback);
    }

    private boolean shouldExecuteInBackground(CancelCallback callback) {
        return mThreadHelper.isCurrentThread() && !(callback instanceof UICancelCallback);
    }

    private boolean shouldExecuteInBackground(AlwaysCallback callback) {
        return mThreadHelper.isCurrentThread() && !(callback instanceof UIAlwaysCallback);
    }



    @Override
    public void cancelTask() {
        cancelTask(false);
    }

    @Override
    public void cancelTask(final boolean mayInterrupt) {
        final State state;
        final List<CancelCallback> cancelCallbacks;
        synchronized (this) {
            state = mState;
            if (state == State.STATE_PENDING) {
                mState = State.STATE_CANCELLED;
                Utils.printStateChanged(mLogger, mId, "CANCELLED");
            }
            cancelCallbacks = mCallbacks.cloneCancelCallbacks();
            clearCallbacks();
        }
        if (state == State.STATE_PENDING) {
            if (mThreadHelper.isCurrentThread() && cancelCallbacks.size() > 0) {
                mNoAwex.submit(new Runnable() {
                    @Override
                    public void run() {
                        doCancel(mayInterrupt, cancelCallbacks);
                    }

                });
            } else {
                doCancel(mayInterrupt, cancelCallbacks);
            }
        }
    }

    private abstract class CancellableRunnable implements Runnable{
        @Override
        public void run() {
            execute();
        }

        public abstract void execute();

    }

    private static class Callbacks<Result, Progress>{
        public static final Callbacks EMPTY=new Callbacks(true);

        private final List<DoneCallback<Result>> mDoneCallbacks;
        private final List<FailCallback> mFailCallbacks;
        private final List<ProgressCallback<Progress>> mProgressCallbacks;
        private final List<CancelCallback> mCancelCallbacks;
        private final List<AlwaysCallback> mAlwaysCallbacks;

        private static final ObjectPool<Callbacks> sObjectPool=new ObjectPool<>(30);

        public static <R,P> Callbacks<R,P> get(){
            Callbacks<R, P> callbacks = (Callbacks<R, P>) sObjectPool.acquire();
            if (callbacks == null) {
                callbacks = new Callbacks<>(false);
            }
            return callbacks;
        }


        private Callbacks(boolean empty) {
            if(empty){
                mDoneCallbacks= Collections.emptyList();
                mFailCallbacks = Collections.emptyList();
                mProgressCallbacks = Collections.emptyList();
                mCancelCallbacks = Collections.emptyList();
                mAlwaysCallbacks = Collections.emptyList();
            }else{
                mDoneCallbacks = new ArrayList<>();
                mFailCallbacks = new ArrayList<>();
                mProgressCallbacks = new ArrayList<>();
                mCancelCallbacks = new ArrayList<>();
                mAlwaysCallbacks = new ArrayList<>();
            }
        }

        public List<DoneCallback<Result>> cloneDoneCallbacks(){
            return clone(mDoneCallbacks);
        }

        public List<FailCallback> cloneFailCallbacks(){
            return clone(mFailCallbacks);
        }

        public List<ProgressCallback<Progress>> cloneProgressCallbacks() {
            return clone(mProgressCallbacks);
        }

        public List<AlwaysCallback> cloneAlwaysCallbacks() {
            return clone(mAlwaysCallbacks);
        }

        public List<CancelCallback> cloneCancelCallbacks() {
            return clone(mCancelCallbacks);
        }

        private <T> List<T> clone(List<T> items) {
            return items.size()==0?Collections.<T>emptyList(): (List<T>) ((ArrayList) items).clone();
        }

        public void recycle(){
            if(this.equals(EMPTY)){
                return;
            }

            mDoneCallbacks.clear();
            mFailCallbacks.clear();
            mProgressCallbacks.clear();
            mCancelCallbacks.clear();
            mAlwaysCallbacks.clear();

            sObjectPool.release(this);
        }



    }


    @Override
    public String toString() {
        return "NoAwexPromise{" +
                "mState=" + mState +
                '}';
    }

    private void clearCallbacks() {
        mCallbacks.recycle();
        mCallbacks = Callbacks.EMPTY;

        synchronized (mBlockingObject) {
            mBlockingObject.notifyAll();
        }
    }

    private void triggerAllDones(Collection<DoneCallback<Result>> doneCallbacks) {
        for (final DoneCallback<Result> callback : doneCallbacks) {
            triggerDone(callback);
        }
    }

    private void triggerAllAlways(List<AlwaysCallback> alwaysCallbacks) {
        for (final AlwaysCallback callback : alwaysCallbacks) {
            triggerAlways(callback);
        }
    }

    private void triggerAlways(final AlwaysCallback callback) {
        if (callback instanceof UIAlwaysCallback && !mThreadHelper.isCurrentThread()) {
            mThreadHelper.post(new CancellableRunnable() {
                @Override
                public void execute() {
                    tryTrigger(callback);
                }
            });
        } else {
            tryTrigger(callback);
        }
    }

    private void tryTrigger(AlwaysCallback callback) {
        try {
            callback.onAlways();
        } catch (Exception ex) {
            mLogger.e("Error when trigger always callback", ex);
        }
    }

    private void doCancel(boolean mayInterrupt, Collection<CancelCallback> cancelCallbacks) {
        if (mTask != null) {
            mNoAwex.cancel(mTask, mayInterrupt);
        }
        triggerAllCancel(cancelCallbacks);
    }

    private void triggerAllCancel(Collection<CancelCallback> cancelCallbacks) {
        for (final CancelCallback callback : cancelCallbacks) {
            triggerCancel(callback);
        }
    }


    private void tryTrigger(CancelCallback callback) {
        try {
            callback.onCancel();
        } catch (Exception ex) {
            mLogger.e("Error when trigger always callback", ex);
        }
    }


    private void triggerCancel(final CancelCallback callback) {
        if (callback instanceof UICancelCallback && !mThreadHelper.isCurrentThread()) {
            mThreadHelper.post(new Runnable() {
                @Override
                public void run() {
                    tryTrigger(callback);
                }
            });
        } else {
            tryTrigger(callback);
        }
    }


}

