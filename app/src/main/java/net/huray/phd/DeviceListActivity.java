package net.huray.phd;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class DeviceListActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        requestPermissionIfNeeded();

        initViews();
    }

    private void initViews() {
        Button btnScan = findViewById(R.id.btn_scan);
        Button btnStopScan = findViewById(R.id.btn_stop_scan);

    }

    private void requestPermissionIfNeeded() {
        if (!isPermissionGranted()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE
            );
        }
    }

    private boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
