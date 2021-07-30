package net.huray.phd.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.huray.phd.R;
import net.huray.phd.bluetooth.OmronBleDeviceManager;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.OmronOption;
import net.huray.phd.bluetooth.model.entity.WeightDeviceInfo;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.ui.request_data.OmronRequestActivity;
import net.huray.phd.utils.Const;
import net.huray.phd.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQGender;

public class OmronDeviceRegisterActivity extends AppCompatActivity
        implements OmronBleDeviceManager.RegisterListener {

    private DeviceRegisterAdapter adapter;
    private DeviceType deviceType;
    private OmronBleDeviceManager omronManager;

    private Button btnScan;
    private TextView tvDescription;
    private ConstraintLayout progressBarContainer;

    private int userIndex = 0;
    private String deviceAddress;

    private List<Integer> radioButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omron_device_register);

        setDeviceType();
        initViews();
        initDeviceManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScanOmron();
    }

    private void setDeviceType() {
        int deviceTypeNumber = getIntent().getIntExtra(Const.EXTRA_DEVICE_TYPE, 0);
        deviceType = DeviceType.getDeviceType(deviceTypeNumber);
    }

    private void initDeviceManager() {
        omronManager = new OmronBleDeviceManager(
                deviceType.getOmronDeviceCategory(),
                OHQSessionType.REGISTER,
                this);
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
        setViewForReadyToScan();
    }

    private void setViewForReadyToScan() {
        btnScan.setText(getString(R.string.start_scan_device));
        tvDescription.setText(getString(R.string.click_device_scan_button));
    }

    private void connectOmronWeightDevice(int position) {
        if (userIndex == 0) {
            Toast.makeText(this, getString(R.string.select_user_index), Toast.LENGTH_SHORT).show();
            return;
        }

        deviceAddress = adapter.getDeviceAddress(position);

        WeightDeviceInfo deviceData = new WeightDeviceInfo(
                deviceAddress,
                userIndex,
                OmronOption.getWeightUserData("2001-01-01", "170.5", OHQGender.Male));
        omronManager.connectWeightDevice(deviceData);

        showLoadingView();
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_scan_title);
        tvTitle.setText(getString(deviceType.getName()));

        adapter = new DeviceRegisterAdapter(this, deviceType);
        ListView listView = findViewById(R.id.lv_scanned_device_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> connectOmronWeightDevice(position));

        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> startScanOmron());

        tvDescription = findViewById(R.id.tv_scan_description);
        progressBarContainer = findViewById(R.id.progress_container);

        Button btnStopConnection = findViewById(R.id.btn_stop_connection);
        btnStopConnection.setOnClickListener(v -> omronManager.cancelSession());

        initRadioButtons();
    }

    private void initRadioButtons() {
        radioButtons = new ArrayList<>();
        radioButtons.add(R.id.rb_one);
        radioButtons.add(R.id.rb_two);
        radioButtons.add(R.id.rb_three);
        radioButtons.add(R.id.rb_four);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        if (deviceType.isWeightDevice()) {
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
        progressBarContainer.setVisibility(View.VISIBLE);
    }

    private void hideLoadingView() {
        progressBarContainer.setVisibility(View.GONE);
    }

    private void completeRegister() {
        if (deviceType.isWeightDevice()) {
            PrefUtils.setOmronBleWeightDeviceAddress(deviceAddress);
            PrefUtils.setOmronBleWeightDeviceUserIndex(userIndex);
            return;
        }

        PrefUtils.setOmronBleBpDeviceAddress(deviceAddress);
    }

    private void moveToRequestActivity() {
        Intent intent = new Intent(this, OmronRequestActivity.class);
        intent.putExtra(Const.EXTRA_DEVICE_TYPE, deviceType.getNumber());
        startActivity(intent);
        finish();
    }

    @Override
    public void onScanned(List<DiscoveredDevice> discoveredDevices) {
        adapter.updateOmronDevices(discoveredDevices);
    }

    @Override
    public void onRegisterFailed(OHQCompletionReason reason) {
        hideLoadingView();

        if (reason.isCanceled()) {
            Toast.makeText(this, getString(R.string.connection_canceled), Toast.LENGTH_SHORT).show();
            setViewForReadyToScan();
            return;
        }

        if (reason.isFailedToConnect() || reason.isFailedToRegisterUser() || reason.isTimeOut()) {
            Toast.makeText(this, getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
            setViewForReadyToScan();
        }
    }

    @Override
    public void onRegisterSuccess() {
        hideLoadingView();

        Toast.makeText(this, getString(R.string.connection_success), Toast.LENGTH_SHORT).show();
        completeRegister();
        moveToRequestActivity();
    }
}