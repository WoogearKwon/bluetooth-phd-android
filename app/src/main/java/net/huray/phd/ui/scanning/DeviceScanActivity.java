package net.huray.phd.ui.scanning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import net.huray.phd.R;
import net.huray.phd.bluetooth.OmronBleDeviceManager;
import net.huray.phd.bluetooth.controller.ScanController;
import net.huray.phd.bluetooth.controller.SessionController;
import net.huray.phd.bluetooth.listener.OmronDeviceListener;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.SessionData;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.utils.Const;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;

public class DeviceScanActivity extends AppCompatActivity
        implements ScanController.Listener, SessionController.Listener, OmronDeviceListener {

    private DeviceScanAdapter adapter;

    private DeviceType deviceType;
    private OmronBleDeviceManager omronManager;

    private Button btnScan;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        setDeviceType();
        initViews();
        initDeviceManager();
    }

    private void setDeviceType() {
        int deviceTypeNumber = getIntent().getIntExtra(Const.EXTRA_DEVICE_TYPE, 0);
        deviceType = DeviceType.getDeviceType(deviceTypeNumber);
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_scan_title);
        tvTitle.setText(getString(deviceType.getName()));

        adapter = new DeviceScanAdapter(this, deviceType);
        ListView listView = findViewById(R.id.lv_scanned_device_list);
        listView.setAdapter(adapter);

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> startScanOmron());

        tvDescription = findViewById(R.id.tv_scan_description);
    }

    private void initDeviceManager() {
        if (deviceType == DeviceType.OMRON_BP || deviceType == DeviceType.OMRON_WEIGHT) {
            initOmronManager();
            return;
        }

        if (deviceType == DeviceType.I_SENS_BS) {
            return;
        }
    }

    private void initOmronManager() {
        omronManager = new OmronBleDeviceManager(
                OHQDeviceCategory.BodyCompositionMonitor,
                OHQSessionType.REGISTER,
                this,
                this,
                this
        );
    }

    private void startScanOmron() {
        if (omronManager.isScanning()) {
            stopScanOmron();
            return;
        }
        omronManager.startScan();
        btnScan.setText(getString(R.string.stop_scan_device));
        tvDescription.setText(getString(R.string.scanning_device));
    }

    private void stopScanOmron() {
        omronManager.stopScan();
        btnScan.setText(getString(R.string.start_scan_device));
        tvDescription.setText(getString(R.string.click_device_scan_button));
    }

    // ScanListener
    @Override
    public void onScan(@NonNull @NotNull List<DiscoveredDevice> discoveredDevices) {
        adapter.updateOmronDevices(discoveredDevices);
//        discoveredDevices.stream()
//                .map(device -> devices.add(new Device(deviceType.getName(), device.getAddress())));
//        adapter.addDevices(devices);
    }

    @Override
    public void onScanCompletion(@NonNull @NotNull OHQCompletionReason reason) {

    }

    // SessionListener
    @Override
    public void onConnectionStateChanged(@NonNull @NotNull OHQConnectionState connectionState) {

    }

    @Override
    public void onSessionComplete(@NonNull @NotNull SessionData sessionData) {

    }

    // OmronListener
    @Override
    public void onScanned(List<DiscoveredDevice> discoveredDevice) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onFailed() {

    }

    @Override
    public void onReceiveData(boolean isSuccess) {

    }

    @Override
    public void onCanceled() {

    }
}