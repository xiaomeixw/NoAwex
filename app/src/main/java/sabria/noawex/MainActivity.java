package sabria.noawex;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import sabria.noawex.library.NoAwex;
import sabria.noawex.library.callback.background.DoneCallback;
import sabria.noawex.library.callback.ui.UIDoneCallback;
import sabria.noawex.library.core.promises.Promise;
import sabria.noawex.library.core.thread.task.Task;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        findViewById(R.id.btn_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });
    }

    /**
     * the log：
     * 06-01 11:36:55.202      457-502/sabria.noawex E/State1﹕ STATE_PENDING
     * 06-01 11:36:55.202      457-457/sabria.noawex E/State4﹕ STATE_PENDING
     * 06-01 11:36:55.202      457-502/sabria.noawex I/Awex﹕ run
     * 06-01 11:36:55.202      457-502/sabria.noawex E/State2﹕ STATE_PENDING
     * 06-01 11:36:55.203      457-502/sabria.noawex E/State3﹕ STATE_RESOLVED
     * 06-01 11:36:55.203      457-457/sabria.noawex E/State5﹕ STATE_RESOLVED
     * 06-01 11:36:55.204      457-457/sabria.noawex I/Awex﹕ Result to the task execution is: 42
     */
    private void initData() {
        NoAwex awex = AwexProvider.get();

        Promise<Integer, Float> submit = awex.submit(new Task<Integer, Float>() {
            @Override
            protected Integer run() throws InterruptedException {
                Log.i("Awex", "run");
                //Do some heavy task here
                return 42; //Return some result
            }
        });
        Log.e("State4",""+submit.getState());
        submit.done(new UIDoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                Log.i("Awex", "Result to the task execution is: " + result);
            }
        });

    }


}
