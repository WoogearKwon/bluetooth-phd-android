package net.huray.phd.bluetooth.listener;

import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;
import net.huray.phd.bluetooth.model.entity.SessionData;

import java.util.List;

import jp.co.ohq.ble.enumerate.OHQCompletionReason;
import jp.co.ohq.ble.enumerate.OHQConnectionState;

public interface OmronDeviceListener {
    void onConnectionStateChanged(OHQConnectionState connectionState);

    void onScanned(List<DiscoveredDevice> discoveredDevices);

    void onScanCompleted(final OHQCompletionReason reason);

    void onSessionComplete(SessionData sessionData);
}
