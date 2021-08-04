package net.huray.phd.bluetooth.model.entity;

import net.huray.phd.bluetooth.model.enumerate.OHQSessionType;

public class WeightDeviceInfo {
    private final String address;
    private final int index;
    private int sequenceNumber = -1;
    private long incrementKey = 0;
    private final OHQSessionType sessionType;

    public static WeightDeviceInfo newInstanceForRegister(String address, int index) {
        return new WeightDeviceInfo(address, index);
    }

    public static WeightDeviceInfo newInstanceForTransfer(String address, int index,
                                                          int sequenceNumber, long incrementKey) {
        return new WeightDeviceInfo(address, index, sequenceNumber, incrementKey);
    }

    private WeightDeviceInfo(String address, int index) {
        this.address = address;
        this.index = index;
        this.sessionType = OHQSessionType.REGISTER;
    }

    private WeightDeviceInfo(String address, int index, int sequenceNumber, long incrementKey) {
        this.address = address;
        this.index = index;
        this.sequenceNumber = sequenceNumber;
        this.incrementKey = incrementKey;
        this.sessionType = OHQSessionType.TRANSFER;
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
