package net.huray.phd.bluetooth.listener;

import net.huray.phd.bluetooth.model.entity.DiscoveredDevice;

import java.util.List;

public interface OmronDeviceListener {
    void onScanned(List<DiscoveredDevice> discoveredDevice);

    void onConnected();

    void onFailed();

    void onReceiveData(boolean isSuccess);

    void onCanceled();
}
