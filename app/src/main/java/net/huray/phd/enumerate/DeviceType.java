package net.huray.phd.enumerate;

import net.huray.phd.R;

public enum DeviceType {
    OMRON_WEIGHT(0),
    OMRON_BP(1),
    I_SENS_BS(3);

    private final int number;

    DeviceType(int number) {
        this.number = number;
    }

    public static DeviceType getDeviceType(int number) {
        if (number == 0) return OMRON_WEIGHT;
        if (number == 1) return OMRON_BP;
        return I_SENS_BS;
    }

    public int getNumber() {
        return number;
    }

    public int getName() {
        if (this == OMRON_WEIGHT) return R.string.omron_body_composition_monitor;
        if (this == OMRON_BP) return R.string.omron_blood_pressure_monitor;
        return R.string.isens_care_sense_n_premier;
    }

    public boolean isWeightDevice() {
        return this == OMRON_WEIGHT;
    }

    public boolean isBpDevice() {
        return this == OMRON_BP;
    }

    public boolean isBsDevice() {
        return this == I_SENS_BS;
    }
}
