package cn.xtu.lhj.timermanager.thread;

import android.os.Looper;
import android.util.Log;

public class UIRefresh extends Thread{

    private static final String TAG = "MainActivity";

    private static UIRefresh uiRefresh;
    private Runnable runnable;

    private UIRefresh(Runnable runnable) {
        this.runnable = runnable;
    }

    public static UIRefresh getInstance(Runnable runnable) {
        if (uiRefresh == null) {
            uiRefresh = new UIRefresh(runnable);
            Log.i(TAG, "getInstance: new a UIRefreshThread");
        }
        return uiRefresh;
    }

    @Override
    public void run() {
        Looper.prepare();
        runnable.run();
    }
}
