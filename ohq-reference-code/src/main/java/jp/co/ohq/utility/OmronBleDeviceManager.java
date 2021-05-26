package jp.co.ohq.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import net.huray.huraycare.io.BpLogsHandler;
import net.huray.huraycare.io.WeightLogsHandler;
import net.huray.huraycare.io.model.OmronDeviceSessionType;
import net.huray.huraycare.util.LogUtils;
import net.huray.huraycare.util.PrefUtils;
import net.huray.huraycare.util.UIUtils;
import net.huray.huraycare.util.UserDataUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.co.ohq.androidcorebluetooth.CBConfig;
import jp.co.ohq.ble.OHQConfig;
import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;
import jp.co.ohq.ble.enumerate.OHQDeviceCategory;
import jp.co.ohq.ble.enumerate.OHQGender;
import jp.co.ohq.ble.enumerate.OHQMeasurementRecordKey;
import jp.co.ohq.ble.enumerate.OHQSessionOptionKey;
import jp.co.ohq.ble.enumerate.OHQUserDataKey;
import jp.co.ohq.model.entity.DiscoveredDevice;

import static jp.co.ohq.ble.enumerate.OHQCompletionReason.Canceled;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.ConnectionTimedOut;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.FailedToConnect;
import static jp.co.ohq.ble.enumerate.OHQCompletionReason.FailedToRegisterUser;
import static jp.co.ohq.ble.enumerate.OHQGender.Female;
import static jp.co.ohq.ble.enumerate.OHQGender.Male;

public class OmronBleDeviceManager implements ScanController.Listener, SessionController.Listener {
    final OmronDeviceSessionType sessionType;
    final OHQDeviceCategory deviceType;
    final OmronDeviceListener omronListener;

    final WeightLogsHandler weightLogsHandler;
    final BpLogsHandler bpLogsHandler;

    public OmronBleDeviceManager(Context context, OHQDeviceCategory deviceType,
                                 OmronDeviceSessionType sessionType, OmronDeviceListener listener) {
        this.sessionType = sessionType;
        this.deviceType = deviceType;
        this.omronListener = listener;

        weightLogsHandler = new WeightLogsHandler(context);
        bpLogsHandler = new BpLogsHandler(context);
    }

    private static final int CONSENT_CODE_OHQ = 0x020E;
    private static final long REGISTER_WAIT_TIME = 30 * 1000L;
    private static final long REQUEST_WAIT_TIME = 15 * 1000L;

    private final ScanController scanController = new ScanController(this);
    private final SessionController sessionController = new SessionController(this);
    private final LoggingManager loggingManager = new LoggingManager();

    private String deviceAddress;
    private Integer userIndex = null;
    private boolean isScanning = false;
    private boolean isRegistrationSuccess = false;

    private Disposable disposable = null;

    public boolean isScanning() {
        return isScanning;
    }

    public void startScan() {
        scanController.setFilteringDeviceCategory(deviceType);

        if (!isScanning) {
            isScanning = true;
            scanController.startScan();
        }
    }

    public void stopScan() {
        if (isScanning()) {
            isScanning = false;
            scanController.stopScan();
        }
    }

    @Override
    public void onScan(@NonNull List<DiscoveredDevice> discoveredDevices) {
        omronListener.onScanned(discoveredDevices);
    }

    @Override
    public void onScanCompletion(@NonNull OHQCompletionReason reason) {
        isScanning = false;
    }

    public void connectWeightDevice(String address, int userIndex) {
        deviceAddress = address;
        this.userIndex = userIndex;

        scanController.stopScan();
        startOmronSession();
    }

    public void connectBpDevice(String address) {
        deviceAddress = address;
        scanController.stopScan();

        startOmronSession();
    }

    public void requestWeightData(String address) {
        deviceAddress = address;
        userIndex = PrefUtils.getOmronBleWeightDeviceUserIndex();
        startOmronSession();
    }

    public void requestBpData(String address) {
        this.deviceAddress = address;
        startOmronSession();
    }

    public void cancelSession() {
        sessionController.cancel();
    }

    /**
     * startOmronSession
     *
     * 오므론 기기와 연결하는 세션을 시작한다.
     * 세션은 기기와 최초 연결 그리고 데이터 수신을 위한 연결에 사용한다.
     */
    private void startOmronSession() {
        if (sessionController.isInSession()) {
            AppLog.i("세션이 이미 시작되었음");
            return;
        }

        Handler handler = new Handler();
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
                    sessionController.setConfig(getConfig());
                    sessionController.startSession(deviceAddress, getOptionKeys());
                });
            }
        });
    }

    @Override
    public void onConnectionStateChanged(@NonNull OHQConnectionState connectionState) {
        switch (connectionState) {
            case Connected:
                isRegistrationSuccess = true;
                break;

            case Disconnected:
                break;
        }
    }

    @Override
    public void onSessionComplete(@NonNull SessionData sessionData) {
        switch (sessionType) {
            case REGISTER:
                setResultForRegister(sessionData);
                break;
            case TRANSFER:
                setResultForTransfer(sessionData);
                break;
        }
    }

    /**
     * setResultForRegister
     *
     * @isCanceled: 사용자가 세션을 취소함
     * @isFailedToRegister: 기기에 사용자 등록을 실패함
     * @isTimeOut: 정해둔 세션 Timeout 시간을 초과함
     */
    private void setResultForRegister(SessionData sessionData) {
        final boolean isCanceled = sessionData.getCompletionReason() == Canceled;
        final boolean isFailed = sessionData.getCompletionReason() == FailedToConnect;
        final boolean isFailedToRegister = sessionData.getCompletionReason() == FailedToRegisterUser;
        final boolean isTimeOut = sessionData.getCompletionReason() == ConnectionTimedOut;

        if (isCanceled || isFailed) {
            omronListener.onCanceled();
        } else {
            if (isFailedToRegister || isTimeOut) {
                omronListener.onFailed();
            } else if (isRegistrationSuccess) {
                completeRegister();
            }
        }
    }

    private void completeRegister() {
        omronListener.onConnected();

        if (deviceType == OHQDeviceCategory.BloodPressureMonitor) {
            PrefUtils.setOmronBleBpDeviceAddress(deviceAddress);
        } else if (deviceType == OHQDeviceCategory.BodyCompositionMonitor) {
            PrefUtils.setOmronBleWeightDeviceAddress(deviceAddress);
            PrefUtils.setOmronBleWeightDeviceUserIndex(userIndex);
        }
    }

    private void setResultForTransfer(SessionData sessionData) {
        final boolean isCanceled = sessionData.getCompletionReason() == Canceled;
        final boolean isTimeout = sessionData.getCompletionReason() == ConnectionTimedOut;

        if (isCanceled) {
            LogUtils.LOGD(deviceType.name(), "세션 종료:::: Canceled");
            omronListener.onCanceled();
            return;
        } else if (isTimeout) {
            omronListener.onFailed();
            return;
        }

        List<Map<OHQMeasurementRecordKey, Object>> results = sessionData.getMeasurementRecords();
        LogUtils.LOGD(deviceType.name(), "session completed:::: measurement record = " + results);

        if (results != null) {
            if (results.isEmpty()) {
                omronListener.onReceiveData(false);
            } else {
                if (deviceType == OHQDeviceCategory.BodyCompositionMonitor) {
                    saveWeightData(sessionData, results);
                } else if (deviceType == OHQDeviceCategory.BloodPressureMonitor) {
                    saveBpData(results);
                }

                omronListener.onReceiveData(true);
            }
        }
    }

    /**
     * checkIfUserInfoChanged
     * <p>
     * 체성분계에 저장된 사용자정보와 앱의 사용자 정보 비교한다.
     * 두 정보가 다를 경우 다음 데이터 전송때 체성분계에 저장된 사용자 정보를 앱을 기준으로 수정한다.
     */
    private void checkIfUserInfoChanged(SessionData sessionData) {
        if (sessionData.getUserData() != null && sessionData.getDatabaseChangeIncrement() != null) {
            Map<OHQUserDataKey, Object> userData = sessionData.getUserData();

            BigDecimal receivedHeight = (BigDecimal) userData.get(OHQUserDataKey.HeightKey);
            String myBirth = UIUtils.formatOmronUser(UserDataUtils.getBirthday());
            String receivedBirthDate = (String) userData.get(OHQUserDataKey.DateOfBirthKey);
            OHQGender gender = UserDataUtils.getGender() == 1 ? Female : Male;

            boolean isBirthDateChanged = !myBirth.equals(receivedBirthDate);
            boolean isHeightChanged = !new BigDecimal(UserDataUtils.getHeight()).equals(receivedHeight);
            boolean isGenderChanged = gender != userData.get(OHQUserDataKey.GenderKey);

            if (isBirthDateChanged || isHeightChanged || isGenderChanged) {
                updateDbIncrementValueKey(sessionData.getDatabaseChangeIncrement());
            } else {
                updateDbIncrementValueKey(sessionData.getDatabaseChangeIncrement() - 1);
            }
        }
    }

    private Bundle getConfig() {
        CBConfig.CreateBondOption cOption = CBConfig.CreateBondOption.UsedBeforeGattConnection;
        CBConfig.RemoveBondOption rOption = CBConfig.RemoveBondOption.NotUse;

        return Bundler.bundle(
                OHQConfig.Key.CreateBondOption.name(), cOption,
                OHQConfig.Key.RemoveBondOption.name(), rOption,
                OHQConfig.Key.AssistPairingDialogEnabled.name(), false,
                OHQConfig.Key.AutoPairingEnabled.name(), false,
                OHQConfig.Key.AutoEnterThePinCodeEnabled.name(), false,
                OHQConfig.Key.PinCode.name(), "000000",
                OHQConfig.Key.StableConnectionEnabled.name(), true,
                OHQConfig.Key.StableConnectionWaitTime.name(), 1500L,
                OHQConfig.Key.ConnectionRetryEnabled.name(), true,
                OHQConfig.Key.ConnectionRetryDelayTime.name(), 1000L,
                OHQConfig.Key.ConnectionRetryCount.name(), 0,
                OHQConfig.Key.UseRefreshWhenDisconnect.name(), true
        );
    }

    /**
     * getOptionKeys
     *
     * 오므론 기기 첫 연결 등록 옵션 리턴
     * @return Map<OHQSessionOptionKey, Object> option 객체
     */
    private Map<OHQSessionOptionKey, Object> getOptionKeys() {
        Map<OHQSessionOptionKey, Object> options = new HashMap<>();
        final long waitTime = sessionType == OmronDeviceSessionType.REGISTER
                ? REGISTER_WAIT_TIME : REQUEST_WAIT_TIME;

        options.put(OHQSessionOptionKey.ConsentCodeKey, CONSENT_CODE_OHQ);
        options.put(OHQSessionOptionKey.ConnectionWaitTimeKey, waitTime);
        options.put(OHQSessionOptionKey.ReadMeasurementRecordsKey, true);

        if (deviceType == OHQDeviceCategory.BodyCompositionMonitor) {
            setWeightDeviceOptionKeys(options);
        }

        return options;
    }

    /**
     * setWeightDeviceOptionsKeys
     * 체성분계 HBF-222T 연결 옵션
     */
    private void setWeightDeviceOptionKeys(Map<OHQSessionOptionKey, Object> options) {
        if (sessionType == OmronDeviceSessionType.REGISTER) {
            options.put(OHQSessionOptionKey.RegisterNewUserKey, true);
            options.put(OHQSessionOptionKey.DatabaseChangeIncrementValueKey, 0L);
        } else {
            setTransferOptionKey(options);
        }

        if (userIndex != null) options.put(OHQSessionOptionKey.UserIndexKey, userIndex);
        options.put(OHQSessionOptionKey.UserDataKey, getUserData());
        options.put(OHQSessionOptionKey.UserDataUpdateFlagKey, true);
        options.put(OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey, true);
        options.put(OHQSessionOptionKey.AllowControlOfReadingPositionToMeasurementRecordsKey, true);
    }

    /**
     * setTransferOoptionKey
     * 오므론 체성분계 HBF-222T 데이터 전송시 사용하는 옵션
     *
     * @SequenceNumberOfFirstRecordToReadKey 이걸 사용하지 않으면 항상 체성분계에 저장된 모든 기록을 가져옴
     * 예를 들어 방금 체성분계에 101번째 기록이 정되었다면 sequenceNumber로 101을 전달해줘서 앱에 저장되지 않은 최근
     * 기록만 요청함
     * @DatabaseChangeIncrementValueKey 체성분계에 저장된 사용자 정보를 변경하는데 사용
     * 체성분계에서 수신된 데이터에 포함된 사용자 정보와 앱의 사용자 정보를 비교하여 일치하지 않으면 다음 요청시 체성분계의
     * 사용자 정보를 수정함
     * DatabaseIncrementKey에 +1한 값을 전달해주면 됨
     */
    private void setTransferOptionKey(Map<OHQSessionOptionKey, Object> options) {
        final int sequenceNumber = loadSequenceNumber();

        if (sequenceNumber != -1)
            options.put(OHQSessionOptionKey.SequenceNumberOfFirstRecordToReadKey,
                    sequenceNumber + 1);
        options.put(OHQSessionOptionKey.DatabaseChangeIncrementValueKey,
                PrefUtils.getOmronBleDataBaseIncrementKey());
    }

    /**
     * getUserData
     * 앱에 저장된 사용자의 생년월일/키/성별 정보를 체성분계에 전송 (체성분계에 저장된 정보가 없을 경우에만)
     */
    private Map<OHQUserDataKey, Object> getUserData() {
        Map<OHQUserDataKey, Object> userData = new HashMap<>();
        userData.put(OHQUserDataKey.DateOfBirthKey, UIUtils.formatOmronUser(UserDataUtils.getBirthday()));
        userData.put(OHQUserDataKey.HeightKey, new BigDecimal(UserDataUtils.getHeight()));
        userData.put(OHQUserDataKey.GenderKey, UserDataUtils.getGender() == 1 ? Female : Male);
        return userData;
    }

    private void saveSequenceNumber(int databaseChangeIncrement) {
        PrefUtils.setOmronBleWeightDeviceSequenceNumber(databaseChangeIncrement);
    }

    private int loadSequenceNumber() {
        return PrefUtils.getOmronBleWeightDeviceSequenceNumber();
    }

    @SuppressLint("CheckResult")
    private void saveWeightData(SessionData sessionData, List<Map<OHQMeasurementRecordKey, Object>> measurementRecords) {
        if (sessionData.getSequenceNumberOfLatestRecord() != null) {
            saveSequenceNumber(sessionData.getSequenceNumberOfLatestRecord());
        }

        disposable = Flowable.fromIterable(measurementRecords)
                .observeOn(Schedulers.io())
                .subscribe(weightLogsHandler::insertByPhd);

        checkIfUserInfoChanged(sessionData);
    }

    private void updateDbIncrementValueKey(long value) {
        PrefUtils.setOmronBleDataBaseIncrementKey(value);
    }

    @SuppressLint("CheckResult")
    private void saveBpData(List<Map<OHQMeasurementRecordKey, Object>> measurementRecords) {
        disposable = Flowable.fromIterable(measurementRecords)
                .observeOn(Schedulers.io())
                .subscribe(bpLogsHandler::insertByPhd);
    }

    public void dispose() {
        if (disposable != null) disposable.dispose();
    }
}