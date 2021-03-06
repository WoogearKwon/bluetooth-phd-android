package net.huray.phd.bluetooth;

import androidx.annotation.NonNull;

import net.huray.phd.bluetooth.controller.ScanController;
import net.huray.phd.bluetooth.controller.SessionController;
import net.huray.phd.bluetooth.controller.util.AppLog;
import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.OmronOption;
import net.huray.phd.bluetooth.model.entity.SessionData;
import net.huray.phd.bluetooth.model.entity.WeightDeviceInfo;
import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;
import net.huray.phd.bluetooth.system.LoggingManager;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.utility.Handler;

public class OmronBleDeviceManager implements ScanController.Listener, SessionController.Listener {
    private final ScanController scanController = new ScanController(this);
    private final SessionController sessionController = new SessionController(this);
    private final LoggingManager loggingManager = new LoggingManager();

    private final OHQDeviceCategory deviceCategory;
    private final OHQSessionType sessionType;

    private RegisterListener registerListener;
    private TransferListener transferListener;

    private WeightDeviceInfo weightDeviceInfo;

    private String deviceAddress;

    private boolean isScanning = false;

    private OmronBleDeviceManager(OHQDeviceCategory deviceType, OHQSessionType sessionType) {
        this.deviceCategory = deviceType;
        this.sessionType = sessionType;
    }

    public OmronBleDeviceManager(OHQDeviceCategory deviceType,
                                 OHQSessionType sessionType,
                                 RegisterListener listener) {
        this(deviceType, sessionType);
        this.registerListener = listener;
    }

    public OmronBleDeviceManager(OHQDeviceCategory deviceType,
                                 OHQSessionType sessionType,
                                 TransferListener listener) {
        this(deviceType, sessionType);
        this.transferListener = listener;
    }

    public boolean isScanning() {
        return isScanning;
    }

    public void startScan() {
        scanController.setFilteringDeviceCategory(deviceCategory);

        if (isScanning) return;

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
        weightDeviceInfo = info;
        deviceAddress = info.getAddress();
        stopScan();

        startOmronSession();
    }

    public void connectBpDevice(String address) {
        deviceAddress = address;
        stopScan();

        startOmronSession();
    }

    public void requestWeightData(WeightDeviceInfo info) {
        weightDeviceInfo = info;
        deviceAddress = info.getAddress();

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
     * ????????? ????????? ???????????? ????????? ????????????.
     * ????????? ????????? ?????? ?????? ????????? ????????? ????????? ?????? ????????? ????????????.
     */
    private void startOmronSession() {
        if (sessionController.isInSession()) {
            AppLog.i("????????? ?????? ???????????????");
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
                handler.post(() -> {
                        sessionController.setConfig(OmronOption.getConfig());
                        sessionController.startSession(deviceAddress, getOptionKeys());
                });
            }
        });
    }

    private Map<OHQSessionOptionKey, Object> getOptionKeys() {
        if (deviceCategory == OHQDeviceCategory.BodyCompositionMonitor) {
            if (weightDeviceInfo == null) {
                throw new NullPointerException("weightDeviceInfo is null");
            }
            return OmronOption.getWeightOptionsKeys(weightDeviceInfo);
        }

        return OmronOption.getOptionsKeys(sessionType);
    }

    /**
     * checkIfUserInfoChanged
     * <p>
     * ??????????????? ????????? ?????????????????? ?????? ????????? ?????? ????????????.
     * ??? ????????? ?????? ?????? ?????? ????????? ????????? ??????????????? ????????? ????????? ????????? ?????? ???????????? ????????????.
     */
    public boolean isUserInfoChanged(SessionData sessionData, Map<OHQUserDataKey, Object> user) {
        if (sessionData.getUserData() != null && sessionData.getDatabaseChangeIncrement() != null) {
            Map<OHQUserDataKey, Object> deviceUser = sessionData.getUserData();

            BigDecimal userHeight = (BigDecimal) user.get(OHQUserDataKey.HeightKey);
            BigDecimal deviceHeight = (BigDecimal) deviceUser.get(OHQUserDataKey.HeightKey);

            String userBirthDate = (String) user.get(OHQUserDataKey.DateOfBirthKey);
            String deviceBirthDate = (String) deviceUser.get(OHQUserDataKey.DateOfBirthKey);

            OHQGender userGender = (OHQGender) user.get(OHQUserDataKey.GenderKey);
            OHQGender deviceGender = (OHQGender) deviceUser.get(OHQUserDataKey.GenderKey);

            boolean isHeightChanged = !Objects.equals(userHeight, deviceHeight);
            boolean isBirthDateChanged = !Objects.equals(userBirthDate, deviceBirthDate);
            boolean isGenderChanged = !Objects.equals(userGender, deviceGender);

            return isBirthDateChanged || isHeightChanged || isGenderChanged;
        }

        return false;
    }

    @Override
    public void onScan(@NonNull @NotNull List<DiscoveredDevice> discoveredDevices) {
        throwExceptionForScanListener();
        registerListener.onScanned(discoveredDevices);
    }

    @Override
    public void onScanCompletion(@NonNull @NotNull OHQCompletionReason reason) {
    }

    @Override
    public void onConnectionStateChanged(@NonNull @NotNull OHQConnectionState connectionState) {
    }

    @Override
    public void onSessionComplete(@NonNull @NotNull SessionData sessionData) {
        OHQCompletionReason reason = sessionData.getCompletionReason();

        assert reason != null;
        if (reason.isCanceled() || reason.isFailedToConnect()
                || reason.isFailedToRegisterUser() || reason.isTimeOut()) {
            setSessionFailed(sessionData.getCompletionReason());
            return;
        }

        if (sessionType == OHQSessionType.REGISTER) {
            throwExceptionForScanListener();
            registerListener.onRegisterSuccess();
            return;
        }

        throwExceptionForTransferListener();
        transferListener.onTransferSuccess(sessionData);

    }

    private void setSessionFailed(OHQCompletionReason reason) {
        if (registerListener != null) {
            registerListener.onRegisterFailed(reason);
        }

        if (transferListener != null) {
            transferListener.onTransferFailed(reason);
        }
    }

    private void throwExceptionForScanListener() throws IllegalStateException {
        if (registerListener == null) {
            throw new IllegalStateException("RegisterListener is not initialized");
        }
    }

    private void throwExceptionForTransferListener() throws IllegalStateException {
        if (transferListener == null) {
            throw new IllegalStateException("TransferListener is not initialized");
        }
    }

    public interface RegisterListener {

        void onScanned(List<DiscoveredDevice> discoveredDevices);

        void onRegisterFailed(OHQCompletionReason reason);

        void onRegisterSuccess();
    }

    public interface TransferListener {

        void onTransferFailed(OHQCompletionReason reason);

        void onTransferSuccess(SessionData sessionData);
    }
}
