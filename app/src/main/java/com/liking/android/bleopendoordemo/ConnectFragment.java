package com.liking.android.bleopendoordemo;


import android.bluetooth.BluetoothDevice;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liking.android.bleopendoordemo.ble.BleService;
import com.liking.android.bleopendoordemo.ble.LkBleManager;
import com.orhanobut.logger.Logger;


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
    private TextInputEditText mEtDeviceId, mEtGymId;

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
        mEtDeviceId = inflate.findViewById(R.id.device_id);
        mEtGymId = inflate.findViewById(R.id.gym_id);
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
                mEtDeviceId.getText();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
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
                appendTextView("连接成功");
            } else if (BleService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Logger.i("BleService", "连接失败");
                appendTextView("连接失败");
            } else if (BleService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BleService.ACTION_CHARACTERISTIC_CHANGED.equals(action)) {

            }
        }
    };

    private StringBuilder builder = new StringBuilder();

    private void appendTextView(String... strings){
        for(String s: strings) {
            builder.append(s);
        }
        builder.append("\r\n");
        mTvLog.setText(builder.toString());
    }

    private void clearLog(){
        builder.delete(0, builder.length());
        mTvLog.setText("");
    }

}
