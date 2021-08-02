package net.huray.phd.bluetooth.model.entity;

import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;

import java.util.Map;

import jp.co.ohq.ble.enumerate.OHQUserDataKey;

public class WeightDeviceInfo {
    private final String address;
    private final int index;
    private int sequenceNumber = -1;
    private long incrementKey = 0;
    private OHQSessionType sessionType;

    public WeightDeviceInfo(String address, int index) {
        this.address = address;
        this.index = index;
    }

    public WeightDeviceInfo(String address, int index, int sequenceNumber, long incrementKey,
                            OHQSessionType sessionType) {
        this(address, index);
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

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getIncrementKey() {
        return incrementKey;
    }

    public OHQSessionType getSessionType() {
        return sessionType;
    }
}
