package com.liking.android.bleopendoordemo;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liking.android.bleopendoordemo.ble.BleContant;
import com.liking.android.bleopendoordemo.ble.BleService;
import com.liking.android.bleopendoordemo.ble.LkBleManager;
import com.liking.android.bleopendoordemo.data.DataPack;
import com.liking.android.bleopendoordemo.utils.NumberUtil;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectFragment extends Fragment {
    private static final String BLUETOOTH_DEVICE_PARAM = "BLUETOOTH_DEVICE";

    private BluetoothDevice mDevice;

    private AppCompatTextView mTvMacAddress, mTvLog;
    private AppCompatButton mBtnConnect, mBtnSend;
    private TextInputEditText mEtData;

    public ConnectFragment() {
        // Required empty public constructor
    }


    public static ConnectFragment newInstance(BluetoothDevice device) {
        ConnectFragment fragment = new ConnectFragment();
        Bundle args = new Bundle();
        args.putParcelable(BLUETOOTH_DEVICE_PARAM, device);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDevice = getArguments().getParcelable(BLUETOOTH_DEVICE_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflate = inflater.inflate(R.layout.fragment_connect, container, false);
        initView(inflate);
        return inflate;
    }

    private void initView(View inflate) {
        mTvMacAddress = inflate.findViewById(R.id.tv_mac_address);
        mBtnConnect = inflate.findViewById(R.id.btn_connect);
        mEtData = inflate.findViewById(R.id.et_data);
        mBtnSend = inflate.findViewById(R.id.btn_send);
        mTvLog = inflate.findViewById(R.id.log);

        mTvMacAddress.setText(mDevice.getAddress());
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LkBleManager.getInstance().connect(mDevice.getAddress());
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = mEtData.getText().toString();
                appendTextView("send: ", result);
                byte[] request = DataPack.openDoor(result);
                appendTextView(Arrays.toString(request));
                appendTextView(NumberUtil.bytesToHexFun(request));
                write(request);
            }
        });

        inflate.findViewById(R.id.btn_device_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = DataPack.deviceInfo();
                write(bytes);
            }
        });

        inflate.findViewById(R.id.btn_clear_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLog();
            }
        });


    }

    BluetoothGattService mGattService;

    public void write(byte[] bytes) {
        if (mGattService == null) {
            appendTextView("未找到GattService, 发送失败请先获取服务");
            return;
        }
        BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(BleContant.WRITE_UUID);

        if (characteristic == null) {
            appendTextView("未找到WriteCharacteristic, 发送失败请先获取服务");
        } else {
            LkBleManager.getInstance().wirteCharacteristic(characteristic, bytes);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, intentFilter);
        LkBleManager.getInstance().bindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        LkBleManager.getInstance().disconnect();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        LkBleManager.getInstance().unBindService();

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleService.ACTION_CHARACTERISTIC_CHANGED);
        return intentFilter;
    }

    private IntentFilter intentFilter = makeGattUpdateIntentFilter();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BleService.ACTION_GATT_CONNECTED.equals(action)) {
                Logger.i("BleService", "连接成功");
                appendTextView("连接成功...");
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Logger.i("BleService", "连接失败");
                appendTextView("连接失败...");
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Logger.i("BleService", "建立服务");
                appendTextView("建立服务...");
                List<BluetoothGattService> services = LkBleManager.getInstance().getSupportedGattServices();
                for (BluetoothGattService service : services) {
                    appendTextView(service.getUuid().toString());
                    if (service.getUuid().toString().toLowerCase().equals(BleContant.SERVER_UUID.toString().toLowerCase())) {
                        mGattService = service;
                        break;
                    }
                }
                if (mGattService == null) {
                    appendTextView("未找到GattService");
                } else {
                    appendTextView("找到GattService");
                    BluetoothGattCharacteristic characteristic = mGattService.getCharacteristic(BleContant.READ_UUID);
                    if (characteristic != null) {
                        appendTextView("设置ReadCharacteristic成功");
                        LkBleManager.getInstance().setCharacteristicNotification(characteristic, true);
                    } else {
                        appendTextView("未找到ReadCharacteristic");
                    }
                }
            } else if (BleService.ACTION_CHARACTERISTIC_CHANGED.equals(action)) {
                Logger.i("BleService", "收到回包");
                appendTextView("收到回包...");
                byte[] data = intent.getByteArrayExtra(BleService.EXTRA_DATA);
                String logRet = NumberUtil.bytesToHexFun(data);
                Logger.i("BleService", "unpack" + logRet);
                appendTextView(Arrays.toString(data));
                appendTextView(logRet);
                String unpack = DataPack.unpack(data);
                if (unpack != null) {
                    Logger.i("BleService", "unpack" + unpack);
                    appendTextView("ret:", unpack);
                }
            }
        }
    };

    private StringBuilder builder = new StringBuilder();

    private void appendTextView(String... strings) {
        for (String s : strings) {
            builder.append(s);
        }
        builder.append("\r\n");
        mTvLog.setText(builder.toString());
    }

    private void clearLog() {
        builder.delete(0, builder.length());
        mTvLog.setText("");
    }

}
