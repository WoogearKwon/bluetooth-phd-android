package net.huray.phd.ui.request_data;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.huray.phd.R;
import net.huray.phd.bluetooth.OmronBleDeviceManager;
import net.huray.phd.bluetooth.model.entity.OmronOption;
import net.huray.phd.bluetooth.model.entity.SessionData;
import net.huray.phd.bluetooth.model.entity.WeightDeviceInfo;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.model.WeightData;
import net.huray.phd.utils.Const;
import net.huray.phd.utils.PrefUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;

import static jp.co.ohq.ble.enumerate.OHQGender.Female;
import static jp.co.ohq.ble.enumerate.OHQGender.Male;

public class OmronRequestActivity extends AppCompatActivity implements OmronBleDeviceManager.TransferListener {

    private OmronBleDeviceManager omronManager;
    private OmronDataAdapter adapter;
    private DeviceType deviceType;

    private ConstraintLayout progressBar;

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

        Button btnRequest = findViewById(R.id.btn_request_omron_data);
        btnRequest.setOnClickListener(v -> requestData());

        TextView tvDisconnect = findViewById(R.id.tv_disconnect_omron_device);
        tvDisconnect.setOnClickListener(v -> showConfirmDialog());

        adapter = new OmronDataAdapter(this, deviceType);
        ListView listView = findViewById(R.id.lv_requested_data_list);
        listView.setAdapter(adapter);

        progressBar = findViewById(R.id.progress_container);
        ConstraintLayout userIndexContainer = findViewById(R.id.constraint_user_index);
        TextView tvUserIndex = findViewById(R.id.tv_user_index);

        Button btnStop = findViewById(R.id.btn_stop_connection);
        btnStop.setOnClickListener(v -> omronManager.cancelSession());

        if (deviceType.isWeightDevice()) {
            userIndexContainer.setVisibility(View.VISIBLE);
            tvUserIndex.setText(String.valueOf(PrefUtils.getOmronBleWeightDeviceUserIndex()));
        }
    }

    private void requestData() {
        showLoadingView();

        if (deviceType.isBpDevice()) {
            omronManager.requestBpData(PrefUtils.getOmronBleBpDeviceAddress());
            return;
        }

        WeightDeviceInfo info = PrefUtils.getOmronWeightTransferInfo();
        omronManager.requestWeightData(info);
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
            PrefUtils.removeOmronWeightDeice();
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

    private void showLoadingView() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingView() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onTransferFailed(OHQCompletionReason reason) {
        hideLoadingView();

        if (reason.isCanceled()) {
            Toast.makeText(this, getString(R.string.request_canceled), Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isTimeOut()) {
            Toast.makeText(this, getString(R.string.please_check_device_is_on), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTransferSuccess(SessionData sessionData) {
        hideLoadingView();
        List<Map<OHQMeasurementRecordKey, Object>> results = sessionData.getMeasurementRecords();

        if (results.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_data_to_bring), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.success_to_receive_data), Toast.LENGTH_SHORT).show();

        if (deviceType.isBpDevice()) {
            // updateBpData();
            return;
        }

        if (deviceType.isWeightDevice()) {
            updateWeightData(results);
            PrefUtils.setOmronBleWeightDeviceSequenceNumber(sessionData.getSequenceNumberOfLatestRecord());

            if (omronManager.isUserInfoChanged(sessionData, OmronOption.getDemoUser())) {
                // updateIncrementDataKey();
            }
        }
    }

    private void updateWeightData(List<Map<OHQMeasurementRecordKey, Object>> results) {
        List<WeightData> data = results.stream()
                .map(this::mapWeightResult)
                .collect(Collectors.toList());
        adapter.addWeightData(data);
    }

    private WeightData mapWeightResult(Map<OHQMeasurementRecordKey, Object> data) {
        return new WeightData(
                (String) data.get(OHQMeasurementRecordKey.TimeStampKey),
                ((BigDecimal) data.get(OHQMeasurementRecordKey.BodyFatPercentageKey)).floatValue(),
                ((BigDecimal) data.get(OHQMeasurementRecordKey.WeightKey)).floatValue()
        );
    }
}