package jp.co.ohq.utility;

import java.util.List;

import jp.co.ohq.model.entity.DiscoveredDevice;

public interface OmronDeviceListener {
    void onScanned(List<DiscoveredDevice> discoveredDevice);

    void onConnected();

    void onFailed();

    void onReceiveData(boolean isSuccess);

    void onCanceled();
}
