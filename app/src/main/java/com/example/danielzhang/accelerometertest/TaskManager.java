package com.example.danielzhang.accelerometertest;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.List;


public class TaskManager {

    public static void killProcessByName(String processName, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        manager.killBackgroundProcesses(processName);
    }

    public static void killProcessByPID(int pid, Context context) {
        String processName = findProcessNameByPID(pid, context);
        killProcessByName(processName, context);
    }

    public static String findProcessNameByPID(int pid, Context context) {
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : runningProcesses) {
            if (process.pid == pid)
                processName = process.processName;
        }
        return processName;
    }

    //TODO change the service name into enum type
    public static void killService(String serviceName, Context context) {
        switch (serviceName) {
            case "blueTooth":
                setBluetooth(false);
            case "wifi":
                setWifi(false, context);
            default:
                throw new RuntimeException("No such service");
        }
    }

    private static boolean setWifi(boolean enable, Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean isEnabled = wifiManager.isWifiEnabled();
        if(enable && !isEnabled){
            wifiManager.setWifiEnabled(true);
            return true;
        } else if(!enable && isEnabled){
            wifiManager.setWifiEnabled(false);
            return false;
        }
        return true;
    }


    private static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

}