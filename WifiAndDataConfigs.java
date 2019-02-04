package com.wildcoder.hotspotconfig;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WifiAndDataConfigs {
    /**
     * @return status hot spot enabled or not
     */
    public static boolean isHotSpotEnabled(Context context) {
        Method method = null;
        int actualState = 0;
        try {
            WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            method = mWifiManager.getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            actualState = (Integer) method.invoke(mWifiManager, (Object[]) null);
            if (actualState == HSConstants.AP_STATE_ENABLING || actualState == HSConstants.AP_STATE_ENABLED) {
                return true;
            }
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param on Boolean status
     */
    public static boolean enableMobileData(Context context, boolean on) {
        try {
            ConnectivityManager mConnectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Method method = mConnectivityManager.getClass().getMethod("setMobileDataEnabled", boolean.class);
            method.invoke(mConnectivityManager, on);
            return true;
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * @param context  Application Context
     * @param name     Name of hotspot
     * @param password Password of hotspot
     * @return
     */
    public static boolean configureHotspot(Context context, String name, String password) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration configuration = getCustomConfigs(manager, name, password);
        return configure(manager, configuration);
    }

    /**
     * @return
     */
    private static boolean configure(WifiManager manager, WifiConfiguration wifiConfig) {
        try {
            Method setConfigMethod =
                    manager.getClass()
                            .getMethod("setWifiApConfiguration", WifiConfiguration.class);
            boolean status = (boolean) setConfigMethod.invoke(manager, wifiConfig);
            return status;
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * @param ssid Requested ssid.
     * @param pass Request password.
     * @return
     */
    private static WifiConfiguration getCustomConfigs(WifiManager manager, String ssid, String pass) {
        WifiConfiguration wifiConfig = null;
        try {
            Method getConfigMethod = manager.getClass().getMethod("getWifiApConfiguration");
            wifiConfig = (WifiConfiguration) getConfigMethod.invoke(manager);
            if (!TextUtils.isEmpty(ssid))
                wifiConfig.SSID = ssid;
            if (!TextUtils.isEmpty(pass))
                wifiConfig.preSharedKey = pass;
            return wifiConfig;
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wifiConfig == null) {
            wifiConfig = new WifiConfiguration();
            if (!TextUtils.isEmpty(ssid))
                wifiConfig.SSID = ssid;
            if (!TextUtils.isEmpty(pass))
                wifiConfig.preSharedKey = pass;
            wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        return wifiConfig;
    }

}
