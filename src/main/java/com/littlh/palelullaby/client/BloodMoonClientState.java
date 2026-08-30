package com.littlh.palelullaby.client;

/** 血月客户端状态：仅保存同步过来的布尔值，可在服务端安全加载。 */
public final class BloodMoonClientState {
    private static boolean active = false;

    private BloodMoonClientState() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean value) {
        active = value;
    }
}
