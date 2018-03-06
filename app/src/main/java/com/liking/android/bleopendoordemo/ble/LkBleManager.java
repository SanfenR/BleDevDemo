package com.liking.android.bleopendoordemo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * Created on 2018/3/2
 * Created by sanfen
 *
 * @version 1.0.0
 */

public class LkBleManager implements LkBleImp {
    private Context mContext;
    private BleScanner mScanner;
    private BleService mBleService;

    private static final String TAG = "LkBleManager";

    private LkBleManager() {
    }

    private static class LkBleManagerFactory {
        private static LkBleManager mInstance = new LkBleManager();
    }

    public static LkBleManager getInstance() {
        return LkBleManagerFactory.mInstance;
    }

    public void initialize(Context context) {
        mContext = context;
        mScanner = new BleScanner(context);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBleService = ((BleService.BleServiceBinder) service).getService();
            if (!mBleService.initialize()) {
                Logger.e(TAG, "Unable to initialize Bluetooth");
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    public void bindService(){
        Intent gattServiceIntent = new Intent(mContext, BleService.class);
        mContext.bindService(gattServiceIntent, mServiceConnection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);
    }

    public void unBindService() {
        mContext.unbindService(mServiceConnection);
    }


    public void connect(String macAddress) {
        if (mBleService != null)
            mBleService.connect(macAddress);
    }

    public void disconnect() {
        if (mBleService != null)
            mBleService.disconnect();
    }

    public List<BluetoothGattService> getSupportedGattServices(){
        if (mBleService != null) {
            return mBleService.getSupportedGattServices();
        }
        return null;
    }


    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBleService!= null) {
            mBleService.setCharacteristicNotification(characteristic, enabled);
        }
    }

    /**
     * 写入数据
     *
     * @param characteristic
     * @param bytes
     */
    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic, byte[] bytes) {
        if (mBleService != null) {
            mBleService.wirteCharacteristic(characteristic, bytes);
        }
    }


    public void discoverServices() {
        if (mBleService != null) {
            mBleService.discoverServices();
        }
    }

    public void startLeScan(BluetoothAdapter.LeScanCallback callback) {
        mScanner.setLeScanCallback(callback);
        mScanner.scanLeDevice(true);
    }

    public void stopLeScan() {
        mScanner.scanLeDevice(false);
    }
}
