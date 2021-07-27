package net.huray.phd.bluetooth;

import androidx.annotation.NonNull;

import net.huray.phd.bluetooth.controller.ScanController;
import net.huray.phd.bluetooth.controller.SessionController;
import net.huray.phd.bluetooth.controller.util.AppLog;
import net.huray.phd.bluetooth.listener.OmronDeviceListener;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.OmronOption;
import net.huray.phd.bluetooth.model.entity.SessionData;
import net.huray.phd.bluetooth.model.entity.WeightDeviceInfo;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.bluetooth.system.LoggingManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.utility.Handler;

public class OmronBleDeviceManager implements ScanController.Listener, SessionController.Listener {
    private final OHQDeviceCategory deviceCategory;
    private final OHQSessionType sessionType;
    private final OmronDeviceListener omronListener;

    private final ScanController scanController = new ScanController(this);
    private final SessionController sessionController = new SessionController(this);
    private final LoggingManager loggingManager = new LoggingManager();

    private String deviceAddress;
    private int userIndex = -1;
    private boolean isScanning = false;
    private Map<OHQUserDataKey, Object> userData;
    private int sequenceNumber = -1;
    private int incrementKey = -1;

    public OmronBleDeviceManager(OHQDeviceCategory deviceType, OHQSessionType sessionType, OmronDeviceListener omronListener) {
        this.deviceCategory = deviceType;
        this.sessionType = sessionType;
        this.omronListener = omronListener;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void startScan() {
        scanController.setFilteringDeviceCategory(deviceCategory);

        if (isScanning) {
            return;
        }

        isScanning = true;
        scanController.startScan();
    }

    public void stopScan() {
        if (isScanning) {
            isScanning = false;
            scanController.stopScan();
        }
    }

    public void connectWeightDevice(WeightDeviceInfo info) {
        userData = info.getUserData();
        deviceAddress = info.getAddress();
        userIndex = info.getIndex();
        stopScan();

        startOmronSession();
    }

    public void connectBpDevice(String address) {
        deviceAddress = address;
        stopScan();

        startOmronSession();
    }

    public void requestWeightData(WeightDeviceInfo info) {
        userData = info.getUserData();
        deviceAddress = info.getAddress();
        userIndex = info.getIndex();
        sequenceNumber = info.getSequenceNumber();
        incrementKey = info.getIncrementKey();

        startOmronSession();
    }

    public void requestBpData(String address) {
        deviceAddress = address;

        startOmronSession();
    }

    public void cancelSession() {
        sessionController.cancel();
    }

    /**
     * 오므론 기기와 연결하는 세션을 시작한다.
     * 세션은 기기와 최초 연결 그리고 데이터 수신을 위한 연결에 사용한다.
     */
    private void startOmronSession() {
        if (sessionController.isInSession()) {
            AppLog.i("세션이 이미 시작되었음");
            return;
        }

        final Handler handler = new Handler();
        loggingManager.start(new LoggingManager.ActionListener() {
            @Override
            public void onSuccess() {
                onStarted();
            }

            @Override
            public void onFailure() {
                onStarted();
            }

            private void onStarted() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sessionController.setConfig(OmronOption.getConfig());
                        sessionController.startSession(deviceAddress, getOptionKeys());
                    }
                });
            }
        });
    }

    private Map<OHQSessionOptionKey, Object> getOptionKeys() {
        if (deviceCategory == OHQDeviceCategory.BodyCompositionMonitor) {
            WeightDeviceInfo info = new WeightDeviceInfo(deviceAddress, userIndex, userData,
                    sequenceNumber, incrementKey, sessionType);

            return OmronOption.getWeightOptionsKeys(info);
        }

        return OmronOption.getOptionsKeys(sessionType);
    }

    @Override
    public void onScan(@NonNull @NotNull List<DiscoveredDevice> discoveredDevices) {
        omronListener.onScanned(discoveredDevices);
    }

    @Override
    public void onScanCompletion(@NonNull @NotNull OHQCompletionReason reason) {
        omronListener.onScanCompleted(reason);
    }

    @Override
    public void onConnectionStateChanged(@NonNull @NotNull OHQConnectionState connectionState) {
        omronListener.onConnectionStateChanged(connectionState);
    }

    @Override
    public void onSessionComplete(@NonNull @NotNull SessionData sessionData) {
        omronListener.onSessionComplete(sessionData);
    }
}
