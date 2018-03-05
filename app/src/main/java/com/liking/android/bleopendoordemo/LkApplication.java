package com.liking.android.bleopendoordemo;

import android.app.Application;

import com.liking.android.bleopendoordemo.ble.LkBleManager;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created on 2018/3/2
 * Created by sanfen
 *
 * @version 1.0.0
 */

public class LkApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LkBleManager.getInstance().initialize(this);
        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
