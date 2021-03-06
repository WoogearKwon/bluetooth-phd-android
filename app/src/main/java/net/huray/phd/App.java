package net.huray.phd;

import android.app.Application;
import android.content.SharedPreferences;

import net.huray.phd.utils.Const;

import jp.co.ohq.ble.OHQDeviceManager;

public class App extends Application {
    private static App appInstance;
    private SharedPreferences prefs;

    public App() {
        super();
        appInstance = this;
    }

    public static synchronized App getInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        OHQDeviceManager.init(getApplicationContext());
    }

    public SharedPreferences getSecurePreferences() {
        if (prefs == null) {
            prefs = getSharedPreferences(Const.PREF_NAME, MODE_PRIVATE);
        }

        return prefs;
    }
}
