package com.nazir.urlshortener.domain.enums;

/**
 * Classifies user devices into broad categories.
 * Maps from Yauaa device class strings to our simplified enum.
 */
public enum DeviceType {

    DESKTOP,
    MOBILE,
    TABLET,
    BOT,
    UNKNOWN;

    /**
     * Maps Yauaa device class strings to our DeviceType.
     * Yauaa returns values like: "Desktop", "Phone", "Tablet", "Robot", etc.
     */
    public static DeviceType fromYauaaDeviceClass(String deviceClass) {
        if (deviceClass == null) {
            return UNKNOWN;
        }

        return switch (deviceClass.toLowerCase()) {
            case "desktop"                                              -> DESKTOP;
            case "phone", "mobile"                                     -> MOBILE;
            case "tablet"                                              -> TABLET;
            case "robot", "robot mobile", "robot imitator", "spider"   -> BOT;
            default                                                    -> UNKNOWN;
        };
    }
}
