package net.huray.phd.enumerate;

public enum DeviceType {
    OMRON_WEIGHT(0),
    OMRON_BP(1),
    I_SENS_BS(3);

    private final int number;

    DeviceType(int number) {
        this.number = number;
    }

    public static DeviceType getDeviceType(int number) {
        if (number == 0) {
            return OMRON_WEIGHT;
        }

        if (number == 1) {
            return OMRON_BP;
        }

        return I_SENS_BS;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        if (this == OMRON_WEIGHT) {
            return "오므론 체성분계";
        }

        if (this == OMRON_BP) {
            return "오므론 혈압계";
        }

        return "아이센스 혈당계";
    }
}
