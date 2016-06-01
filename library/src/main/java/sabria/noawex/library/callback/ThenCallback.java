package sabria.noawex.library.callback;

import sabria.noawex.library.core.promises.Promise;

/**
 * Created by xiong,An android project Engineer,on 31/5/2016.
 * Data:31/5/2016  下午 12:34
 * Base on clever-m.com(JAVA Service)
 * Describe:
 * Version:1.0
 * Open source
 */
public interface ThenCallback<Parameter,Result,Progress> {

    Promise<Result,Progress> then(Parameter parameter);
}
