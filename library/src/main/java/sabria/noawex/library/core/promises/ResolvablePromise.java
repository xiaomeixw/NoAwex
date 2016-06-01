package sabria.noawex.library.core.promises;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 01:07
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface ResolvablePromise<Result,Progress> extends Promise<Result,Progress> {

    Promise<Result,Progress> resolve(Result result);

    Promise<Result,Progress> reject(Exception ex);

    void notifyProgress(Progress progress);

}
