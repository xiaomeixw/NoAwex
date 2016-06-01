package sabria.noawex.library.util;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 03:56
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public class ObjectPool<T> {

    private final Object mLock = new Object();
    private final Object[] mPool;

    private int mPoolSize;

    public ObjectPool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        mPool = new Object[maxPoolSize];
    }

    /**
     * 取
     *
     * @return
     */
    public T acquire() {
        synchronized (mLock) {
            if (mPoolSize > 0) {
                //取出来后，赋值给新对象，然后销毁原对象
                final int lastPooledIndex = mPoolSize - 1;
                T instance = (T) mPool[lastPooledIndex];
                mPool[lastPooledIndex] = null;
                mPoolSize--;
                return instance;
            }
            return null;
        }
    }

    /**
     * 存-不可重复
     * @param element
     * @return
     */
    public boolean release(T element) {
        synchronized (mLock) {
            if (isInPool(element)) {
                throw new IllegalStateException("Already in the pool!");
            }

            if (mPoolSize < mPool.length) {
                mPool[mPoolSize] = element;
                mPoolSize++;
                return true;
            }
            return false;
        }
    }

    private boolean isInPool(T element) {
        for (int i = 0; i < mPoolSize; i++) {
            if (mPool[i] == element) {
                return true;
            }
        }
        return false;
    }


}
