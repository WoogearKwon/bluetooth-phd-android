package net.huray.phd.ui.scanning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import net.huray.phd.utils.PrefUtils;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;

import static jp.co.ohq.ble.enumerate.OHQCompletionReason.Canceled;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.ConnectionTimedOut;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.FailedToConnect;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.FailedToRegisterUser;

public class OmronDeviceScanActivity extends AppCompatActivity
        implements ScanController.Listener, SessionController.Listener, OmronDeviceListener {

    private DeviceScanAdapter adapter;

    private DeviceType deviceType;
    private OmronBleDeviceManager omronManager;

    private Button btnScan;
    private TextView tvDescription;
    private ProgressBar progressBar;
    private List<Integer> radioButtons;

    private int userIndex = 0;
    private String deviceAddress;

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

    private void initDeviceManager() {
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

    private void connectOmronWeightDevice(int position) {
        if (userIndex == 0) {
            Toast.makeText(this, getString(R.string.select_user_index), Toast.LENGTH_SHORT).show();
            return;
        }

        Map<OHQUserDataKey, Object> userData = new HashMap<>();
        userData.put(OHQUserDataKey.DateOfBirthKey,  "2001-01-01");
        userData.put(OHQUserDataKey.HeightKey, new BigDecimal("170.5"));
        userData.put(OHQUserDataKey.GenderKey, OHQGender.Male);
        omronManager.connectWeightDevice(adapter.getDeviceAddress(position), userIndex, userData);
        showLoadingView();
    }

    // ScanListener
    @Override
    public void onScan(@NonNull @NotNull List<DiscoveredDevice> discoveredDevices) {
        adapter.updateOmronDevices(discoveredDevices);
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_scan_title);
        tvTitle.setText(getString(deviceType.getName()));

        adapter = new DeviceScanAdapter(this, deviceType);
        ListView listView = findViewById(R.id.lv_scanned_device_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> connectOmronWeightDevice(position));

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> startScanOmron());

        tvDescription = findViewById(R.id.tv_scan_description);
        progressBar = findViewById(R.id.progress_bar);

        initRadioButtons();
    }

    private void initRadioButtons() {
        radioButtons = new ArrayList<>();
        radioButtons.add(R.id.rb_one);
        radioButtons.add(R.id.rb_two);
        radioButtons.add(R.id.rb_three);
        radioButtons.add(R.id.rb_four);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        if (deviceType == DeviceType.OMRON_WEIGHT) {
            radioGroup.setVisibility(View.VISIBLE);
            return;
        }

        radioGroup.setVisibility(View.INVISIBLE);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        for (int i = 0; i < radioButtons.size(); i++) {
            if (view.getId() == radioButtons.get(i)) {
                if (checked) userIndex = i + 1;
                return;
            }
        }
    }

    private void showLoadingView() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingView() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onScanCompletion(@NonNull @NotNull OHQCompletionReason reason) {
        Log.d("woogear", "");
    }

    // SessionListener
    @Override
    public void onConnectionStateChanged(@NonNull @NotNull OHQConnectionState connectionState) {
        Log.d("woogear", "onConnectionStateChanged");
    }

    @Override
    public void onSessionComplete(@NonNull @NotNull SessionData sessionData) {
        Log.d("woogear", "onSessionComplete");
        hideLoadingView();
        Toast.makeText(this, "기기 연결 완료", Toast.LENGTH_SHORT).show();

        // TODO: 오므론 연결 완료 // 전송 완료 로직 추가
        final boolean isCanceled = sessionData.getCompletionReason() == Canceled;
        final boolean isFailed = sessionData.getCompletionReason() == FailedToConnect;
        final boolean isFailedToRegister = sessionData.getCompletionReason() == FailedToRegisterUser;
        final boolean isTimeOut = sessionData.getCompletionReason() == ConnectionTimedOut;

        if (isFailedToRegister || isTimeOut) {
            // TODO: fail
            return;
        }

        completeRegister();
    }

    private void completeRegister() {
        if (deviceType == DeviceType.OMRON_WEIGHT) {
            PrefUtils.setOmronBleBpDeviceAddress(deviceAddress);
        } else if (deviceType == DeviceType.OMRON_BP) {
            PrefUtils.setOmronBleWeightDeviceAddress(deviceAddress);
            PrefUtils.setOmronBleWeightDeviceUserIndex(userIndex);
        }
    }

    // OmronListener
    @Override
    public void onScanned(List<DiscoveredDevice> discoveredDevice) {
        Log.d("woogear", "onScanned");
    }

    @Override
    public void onConnected() {
        Log.d("woogear", "onConnected");
    }

    @Override
    public void onFailed() {
        Log.d("woogear", "onFailed");
    }

    @Override
    public void onReceiveData(boolean isSuccess) {
        Log.d("woogear", "onReceiveData");
    }

    @Override
    public void onCanceled() {
        Log.d("woogear", "onCanceled");
    }
}