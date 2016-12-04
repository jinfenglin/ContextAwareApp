package com.example.danielzhang.accelerometertest;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import android.util.Log;

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
    public static void killService(String serviceName) {
        switch (serviceName) {
            case "blueTooth":
                setBluetooth(false);
        }
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

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_state);

        Button runningApp = (Button) findViewById(R.id.runningApp);
        runningApp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String nameOfProcess = "com.example.filepath";
                ActivityManager manager;
                manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> listOfProcesses = manager.getRunningAppProcesses();
                for (ActivityManager.RunningAppProcessInfo process : listOfProcesses) {
                    if (process.processName.contains(nameOfProcess)) {
                        Log.e("Proccess", process.processName + " : " + process.pid);
                        android.os.Process.killProcess(process.pid);
                        android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
                        manager.killBackgroundProcesses(process.processName);
                        break;
                    }
                }
            }
        });

        String state = getState();
    }*/

    /**
     * Determine User State
     **/
    public String getState() {
        return "Sleeping";
    }

}