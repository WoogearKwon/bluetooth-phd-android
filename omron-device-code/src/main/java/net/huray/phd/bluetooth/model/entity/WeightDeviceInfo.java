package net.huray.phd.bluetooth.model.entity;

import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;

import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQUserDataKey;

public class WeightDeviceInfo {
    private final String address;
    private final int index;
    private final Map<OHQUserDataKey, Object> userData;
    private int sequenceNumber = -1;
    private int incrementKey = -1;
    private OHQSessionType sessionType;

    public WeightDeviceInfo(String address, int index, Map<OHQUserDataKey, Object> userData) {
        this.address = address;
        this.index = index;
        this.userData = userData;
    }

    public WeightDeviceInfo(String address, int index, Map<OHQUserDataKey, Object> userData,
                            int sequenceNumber, int incrementKey, OHQSessionType sessionType) {
        this(address, index, userData);
        this.sequenceNumber = sequenceNumber;
        this.incrementKey = incrementKey;
        this.sessionType = sessionType;
    }

    public String getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public Map<OHQUserDataKey, Object> getUserData() {
        return userData;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getIncrementKey() {
        return incrementKey;
    }

    public OHQSessionType getSessionType() {
        return sessionType;
    }
}
