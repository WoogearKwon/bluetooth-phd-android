package net.huray.phd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import net.huray.phd.bluetooth.OmronBleDeviceManager;
import net.huray.phd.bluetooth.controller.ScanController;
import net.huray.phd.bluetooth.controller.SessionController;
import net.huray.phd.bluetooth.listener.OmronDeviceListener;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.SessionData;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;

public class DeviceScanActivity extends AppCompatActivity implements ScanController.Listener, SessionController.Listener, OmronDeviceListener {
    private OmronBleDeviceManager omronManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        initOmronManager();
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

    private void startScan() {

        if (!omronManager.isScanning()) {
            omronManager.startScan();
        }
    }

    // ScanListener
    @Override
    public void onScan(@NonNull @NotNull List<DiscoveredDevice> discoveredDevices) {

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