package com.shadowcoder.ibus;

public class DriverStudent {
    private static Boolean isWorking = false;
    private static Boolean isDriver = false;
    private static Boolean isStudent = false;

    public DriverStudent() {

    }

    public Boolean getWorking() {
        return isWorking;
    }

    public void setWorking(Boolean working) {
        isWorking = working;
    }

    public Boolean getIsDriver() {
        return isDriver;
    }

    public void setIsDriver(Boolean isDriver) {
        DriverStudent.isDriver = isDriver;
    }

    public Boolean getIsStudent() {
        return isStudent;
    }

    public void setIsStudent(Boolean isStudent) {
        DriverStudent.isStudent = isStudent;
    }
}
