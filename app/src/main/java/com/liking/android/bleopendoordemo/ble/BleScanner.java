package com.liking.android.bleopendoordemo.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import com.orhanobut.logger.Logger;
import java.util.List;


public class BleScanner {
    public static final int SCANNER_DELAY_MILLIS = 1000 * 10;
    private static final String TAG = "BleScanner";
    private Context mContext;
    private boolean mIsScanning = false;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Logger.d(TAG, device, rssi, scanRecord);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };


    public BleScanner(Context context) {
        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        mContext = context;
    }

    public void setLeScanCallback(BluetoothAdapter.LeScanCallback leScanCallback) {
        mLeScanCallback = leScanCallback;
    }

    /**
     * 扫描低功耗蓝牙设备
     *
     * @param enable
     */
    public void scanLeDevice(final boolean enable) {
        if (!BleUtils.isSupportBleDevice(mContext)) {
            return;
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (enable) {
//                mHandler.postDelayed(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//                    @Override
//                    public void run() {
//                        mIsScanning = false;
//                        mBluetoothLeScanner.stopScan(mScanCallback);
//                    }
//                }, SCANNER_DELAY_MILLIS);
//                mBluetoothLeScanner.startScan(mScanCallback);
//            } else {
//                mIsScanning = false;
//                mBluetoothLeScanner.stopScan(mScanCallback);
//            }
//        } else {
            if (enable) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                }, SCANNER_DELAY_MILLIS);
                mIsScanning = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mIsScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
//        }
    }

    /**
     * 判断是否正在扫描中
     *
     * @return true: 是
     */
    public boolean isScanning() {
        return mIsScanning;
    }

    public void destroy() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mContext = null;
        mLeScanCallback = null;
        mBluetoothAdapter = null;
    }
}
