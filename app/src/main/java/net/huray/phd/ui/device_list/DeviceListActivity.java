package net.huray.phd.ui.device_list;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.huray.phd.R;
import net.huray.phd.ui.scanning.DeviceScanActivity;
import net.huray.phd.utils.Const;

public class DeviceListActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 11;
    private final String permission = Manifest.permission.ACCESS_FINE_LOCATION;

    private DeviceListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        requestPermissionIfNotGranted();
        initViews();
    }

    private void initViews() {
        adapter = new DeviceListAdapter(this);
        ListView listView = findViewById(R.id.lv_device_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
                moveToScanActivity(position));
    }

    private void moveToScanActivity(int position) {
        if (isPermissionGranted()) {
            Intent intent = new Intent(this, DeviceScanActivity.class);
            intent.putExtra(Const.EXTRA_DEVICE_TYPE, adapter.getDeviceTypeNumber(position));
            startActivity(intent);
            return;
        }

        requestPermission();
    }

    private void requestPermissionIfNotGranted() {
        if (isPermissionGranted()) return;

        requestPermission();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED;
    }
}
