package org.example;

import java.util.List;

public class License {
    private String licenseUserName;
    private Long licenceCount;
    private List<String> ipAddresses;
    private Long validationTime;

    public License(String licenseUserName, Long licenceCount, List<String> ipAddresses, Long validationTime) {
        this.licenseUserName = licenseUserName;
        this.licenceCount = licenceCount;
        this.ipAddresses = ipAddresses;
        this.validationTime = validationTime;
    }

    public Long getLicenceCount() {
        return licenceCount;
    }

    public Long getValidationTime() {
        return validationTime;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public String getLicenseUserName() {
        return licenseUserName;
    }
}
