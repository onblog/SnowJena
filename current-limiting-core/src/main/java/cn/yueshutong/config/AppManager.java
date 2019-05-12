package cn.yueshutong.config;

/**
 * App name
 */
public class AppManager {
    private static String app;

    public static String getApp() {
        assert app!=null;
        return app;
    }

    public static void setApp(String app) {
        AppManager.app = app;
    }
}