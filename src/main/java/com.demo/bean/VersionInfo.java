package com.demo.bean;

import com.demo.tools.HttpServletUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class VersionInfo implements Serializable {
    private static final long serialVersionUID = 5359709211352400086L;

    private String uuid = "00000000000000000000000000000000";

    private String androidRelease = "";

    private String androidTest = "";

    private String mcuRelease = "";

    private String mcuTest = "";

    private String chipRelease = "";

    private String chipTest = "";

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAndroidRelease() {
        return androidRelease;
    }

    public void setAndroidRelease(String androidRelease) {
        this.androidRelease = androidRelease;
    }

    public String getAndroidTest() {
        return androidTest;
    }

    public void setAndroidTest(String androidTest) {
        this.androidTest = androidTest;
    }

    public String getMcuRelease() {
        return mcuRelease;
    }

    public void setMcuRelease(String mcuRelease) {
        this.mcuRelease = mcuRelease;
    }

    public String getMcuTest() {
        return mcuTest;
    }

    public void setMcuTest(String mcuTest) {
        this.mcuTest = mcuTest;
    }

    public String getChipRelease() {
        return chipRelease;
    }

    public void setChipRelease(String chipRelease) {
        this.chipRelease = chipRelease;
    }

    public String getChipTest() {
        return chipTest;
    }

    public void setChipTest(String chipTest) {
        this.chipTest = chipTest;
    }
}
