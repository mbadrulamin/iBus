package com.shadowcoder.ibus;

public class DriverWorking {
    private static Boolean isWorking = false;

    public DriverWorking() {

    }

    public Boolean getWorking() {
        return isWorking;
    }

    public void setWorking(Boolean working) {
        isWorking = working;
    }
}
