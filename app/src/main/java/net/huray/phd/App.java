package net.huray.phd;

import android.app.Application;

import jp.co.ohq.ble.OHQDeviceManager;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OHQDeviceManager.init(getApplicationContext());
    }
}
