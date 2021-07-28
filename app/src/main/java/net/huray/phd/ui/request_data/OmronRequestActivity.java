package net.huray.phd.ui.request_data;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.huray.phd.R;
import net.huray.phd.bluetooth.OmronBleDeviceManager;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.utils.Const;
import net.huray.phd.utils.PrefUtils;

import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;

public class OmronRequestActivity extends AppCompatActivity implements OmronBleDeviceManager.TransferListener {

    private OmronBleDeviceManager omronManager;

    private DeviceType deviceType;

    private Button btnRequest;
    private TextView tvDisconnect, tvUserIndex;
    private ConstraintLayout progressBar, userIndexContainer;

    private OmronDataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omron_request);

        setDeviceType();
        initViews();
        initDeviceManager();
    }

    private void setDeviceType() {
        int deviceTypeNumber = getIntent().getIntExtra(Const.EXTRA_DEVICE_TYPE, 0);
        deviceType = DeviceType.getDeviceType(deviceTypeNumber);
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_request_omron_title);
        tvTitle.setText(deviceType.getName());

        btnRequest = findViewById(R.id.btn_request_omron_data);

        tvDisconnect = findViewById(R.id.tv_disconnect_omron_device);
        tvDisconnect.setOnClickListener(v -> showConfirmDialog());

        adapter = new OmronDataAdapter(this, deviceType);
        ListView listView = findViewById(R.id.lv_requested_data_list);
        listView.setAdapter(adapter);

        progressBar = findViewById(R.id.progress_container);
        userIndexContainer = findViewById(R.id.constraint_user_index);
        tvUserIndex = findViewById(R.id.tv_user_index);

        if (deviceType.isWeightDevice()) {
            userIndexContainer.setVisibility(View.VISIBLE);
            tvUserIndex.setText(String.valueOf(PrefUtils.getOmronBleWeightDeviceUserIndex()));
        }
    }
    
    private void showConfirmDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle(getString(R.string.alert));
        dialog.setMessage(getString(R.string.sure_to_disconnect));
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cacel), (dialogInterface, i) -> {
            dialog.dismiss();
        });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.disconnect), (dialogInterface, i) -> {
            disconnectDevice();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void disconnectDevice() {
        if (deviceType.isWeightDevice()) {
            PrefUtils.setOmronBleWeightDeviceAddress(null);
            PrefUtils.setOmronBleWeightDeviceUserIndex(-1);
            PrefUtils.setOmronBleWeightDeviceSequenceNumber(-1);
        }

        if (deviceType.isBpDevice()) {
            PrefUtils.setOmronBleBpDeviceAddress(null);
        }

        finish();
    }

    private void initDeviceManager() {
        omronManager = new OmronBleDeviceManager(
                deviceType.getOmronDeviceCategory(),
                OHQSessionType.TRANSFER,
                this);
    }

    @Override
    public void onTransferFailed(OHQCompletionReason reason) {

    }

    @Override
    public void onTransferSuccess(List<Map<OHQMeasurementRecordKey, Object>> results) {

    }
}