package net.huray.phd.ui.request_data;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.huray.phd.R;
import net.huray.phd.enumerate.DeviceType;
import net.huray.phd.utils.Const;
import net.huray.phd.utils.PrefUtils;

public class OmronRequestActivity extends AppCompatActivity {
    private DeviceType deviceType;

    private Button btnRequest;
    private TextView tvDisconnect;
    private ConstraintLayout progressBar, userIndexContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_omron_request);

        setDeviceType();
        initViews();
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
        tvDisconnect.setOnClickListener(v -> disconnectDevice());

        ListView listView = findViewById(R.id.lv_requested_data_list);

        progressBar = findViewById(R.id.progress_container);
        userIndexContainer = findViewById(R.id.constraint_user_index);

        if (deviceType.isWeightDevice()) {
            userIndexContainer.setVisibility(View.VISIBLE);
        }
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
}