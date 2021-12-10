package net.huray.phd.enumerate;

import net.huray.phd.R;

import jp.co.ohq.ble.enumerate.OHQDeviceCategory;

public enum DeviceType {
    OMRON_WEIGHT(0),
    OMRON_BP(1);

    private final int number;

    DeviceType(int number) {
        this.number = number;
    }

    public static DeviceType getDeviceType(int number) {
        if (number == 0) return OMRON_WEIGHT;
        return OMRON_BP;
    }

    public int getNumber() {
        return number;
    }

    public int getName() {
        if (this == OMRON_WEIGHT) return R.string.omron_body_composition_monitor;
        return R.string.omron_blood_pressure_monitor;
    }

    public OHQDeviceCategory getOmronDeviceCategory() {
        if (isWeightDevice()) {
            return OHQDeviceCategory.BodyCompositionMonitor;
        }

        if (isBpDevice()) {
            return OHQDeviceCategory.BloodPressureMonitor;
        }

        throw new IllegalStateException("Blood Sugar device is NOT Omron device.");
    }

    public boolean isWeightDevice() {
        return this == OMRON_WEIGHT;
    }

    public boolean isBpDevice() {
        return this == OMRON_BP;
    }
}
