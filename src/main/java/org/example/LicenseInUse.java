package org.example;

public class LicenseInUse {
    String licenseUserName;
    String expiredTime;


    public LicenseInUse(String licenseUserName, String expiredTime) {
        this.licenseUserName = licenseUserName;
        this.expiredTime = expiredTime;
    }

    public String getLicenseUserName() {
        return licenseUserName;
    }

    public String getExpiredTime() {
        return expiredTime;
    }
}
