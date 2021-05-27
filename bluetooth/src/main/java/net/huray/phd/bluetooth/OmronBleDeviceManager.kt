package net.huray.phd.bluetooth

import android.os.Bundle
import jp.co.ohq.androidcorebluetooth.CBConfig
import jp.co.ohq.androidcorebluetooth.CBConfig.CreateBondOption
import jp.co.ohq.androidcorebluetooth.CBConfig.RemoveBondOption
import jp.co.ohq.ble.enumerate.*
import jp.co.ohq.model.entity.DiscoveredDevice
import jp.co.ohq.model.enumerate.OHQSessionType
import jp.co.ohq.utility.*

class OmronBleDeviceManager(
    private val deviceType: OHQDeviceCategory,
    private val sessionType: OHQSessionType,
    private val scanListener: ScanController.Listener,
    private val sessionListener: SessionController.Listener,
    private val omronListener: OmronDeviceListener
) : ScanController.Listener, SessionController.Listener {

    private val scanController = ScanController(scanListener)
    private val sessionController = SessionController(sessionListener)
    private val loggingManager = LoggingManager()

    private lateinit var deviceAddress: String
    private var userIndex = -1
    private var isScanning = false
    private var isRegistrationSuccess = false
    private lateinit var userData: Map<OHQUserDataKey, Any>
    private var sequenceNumber: Int = -1
    private var incrementKey: Int = -1

    companion object {
        private const val CONSENT_CODE_OHQ: Int = 0x020E
        private const val REGISTER_WAIT_TIME: Long = 30 * 1000L
        private const val REQUEST_WAIT_TIME: Long = 15 * 1000L
    }

    override fun onScan(discoveredDevices: MutableList<DiscoveredDevice>) {
        omronListener.onScanned(discoveredDevices)
    }

    override fun onScanCompletion(reason: OHQCompletionReason) {
        isScanning = false
    }

    override fun onConnectionStateChanged(connectionState: OHQConnectionState) {
        sessionListener.onConnectionStateChanged(connectionState)
    }

    override fun onSessionComplete(sessionData: SessionData) {
        sessionListener.onSessionComplete(sessionData)
    }

    fun isScanning() = isScanning

    fun startScan() {
        scanController.setFilteringDeviceCategory(deviceType)

        if (!isScanning) {
            isScanning = true
            scanController.startScan()
        }
    }

    fun stopScan() {
        if (isScanning) {
            isScanning = false
            scanController.stopScan()
        }
    }

    fun connectWeightDevice(address: String, index: Int, user: Map<OHQUserDataKey, Any>) {
        deviceAddress = address
        userIndex = index

        scanController.stopScan()
        startOmronSession()
    }

    fun connectBpDevice(address: String) {
        deviceAddress = address
        scanController.stopScan()

        startOmronSession()
    }

    fun requestWeightData(
        address: String,
        index: Int,
        user: Map<OHQUserDataKey, Any>,
        sequenceNumber: Int,
        incrementKey: Int
    ) {
        deviceAddress = address
        userIndex = index
        userData = user
        this.sequenceNumber = sequenceNumber
        this.incrementKey = incrementKey
        startOmronSession()
    }

    fun requestBpData(address: String) {
        deviceAddress = address
        startOmronSession()
    }

    fun cancelSession() {
        sessionController.cancel()
    }

    /**
     * startOmronSession
     *
     * 오므론 기기와 연결하는 세션을 시작한다.
     * 세션은 기기와 최초 연결 그리고 데이터 수신을 위한 연결에 사용한다.
     */
    private fun startOmronSession() {
        if (sessionController.isInSession) {
            AppLog.i("세션이 이미 시작되었음")
            return
        }

        val handler = Handler()
        loggingManager.start(object : LoggingManager.ActionListener {
            override fun onSuccess() {
                onStarted()
            }

            override fun onFailure() {
                onStarted()
            }

            private fun onStarted() {
                handler.post {
                    sessionController.setConfig(getConfig())
                    sessionController.startSession(deviceAddress, getOptionKeys())
                }
            }
        })
    }

    private fun getConfig(): Bundle {
        val cOption = CreateBondOption.UsedBeforeGattConnection
        val rOption = RemoveBondOption.NotUse

        return Bundler.bundle(
            CBConfig.Key.CreateBondOption.name, cOption,
            CBConfig.Key.RemoveBondOption.name, rOption,
            CBConfig.Key.AssistPairingDialogEnabled.name, false,
            CBConfig.Key.AutoPairingEnabled.name, false,
            CBConfig.Key.AutoEnterThePinCodeEnabled.name, false,
            CBConfig.Key.PinCode.name, "000000",
            CBConfig.Key.StableConnectionEnabled.name, true,
            CBConfig.Key.StableConnectionWaitTime.name, 1500L,
            CBConfig.Key.ConnectionRetryEnabled.name, true,
            CBConfig.Key.ConnectionRetryDelayTime.name, 1000L,
            CBConfig.Key.ConnectionRetryCount.name, 0,
            CBConfig.Key.UseRefreshWhenDisconnect.name, true
        )
    }

    private fun getOptionKeys(): Map<OHQSessionOptionKey, Any> {
        val options = HashMap<OHQSessionOptionKey, Any>()
        val waitTime =
            if (sessionType == OHQSessionType.REGISTER) REGISTER_WAIT_TIME else REQUEST_WAIT_TIME

        options[OHQSessionOptionKey.ConsentCodeKey] = CONSENT_CODE_OHQ
        options[OHQSessionOptionKey.ConnectionWaitTimeKey] = waitTime
        options[OHQSessionOptionKey.ReadMeasurementRecordsKey] = true

        if (deviceType == OHQDeviceCategory.BodyCompositionMonitor) {
            setWeightDeviceOptionKeys(options)
        }

        return options
    }

    /**
     * setWeightDeviceOptionsKeys
     * 체성분계 HBF-222T 연결 옵션
     */
    private fun setWeightDeviceOptionKeys(
        options: MutableMap<OHQSessionOptionKey, Any>
    ) {
        if (sessionType === OHQSessionType.REGISTER) {
            options[OHQSessionOptionKey.RegisterNewUserKey] = true
            options[OHQSessionOptionKey.DatabaseChangeIncrementValueKey] = 0L
        } else {
            setTransferOptionKey(options)
        }
        options[OHQSessionOptionKey.UserIndexKey] = userIndex
        options[OHQSessionOptionKey.UserDataKey] = userData
        options[OHQSessionOptionKey.UserDataUpdateFlagKey] = true
        options[OHQSessionOptionKey.AllowAccessToOmronExtendedMeasurementRecordsKey] = true
        options[OHQSessionOptionKey.AllowControlOfReadingPositionToMeasurementRecordsKey] = true
    }

    /**
     * setTransferOptionKey
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
    private fun setTransferOptionKey(
        options: MutableMap<OHQSessionOptionKey, Any>
    ) {
        if (sequenceNumber != -1) options[OHQSessionOptionKey.SequenceNumberOfFirstRecordToReadKey] =
            sequenceNumber + 1
        options[OHQSessionOptionKey.DatabaseChangeIncrementValueKey] = incrementKey
    }
}