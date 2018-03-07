package com.liking.android.bleopendoordemo;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liking.android.bleopendoordemo.ble.BleContant;
import com.orhanobut.logger.Logger;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NormalBluetoothFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NormalBluetoothFragment extends Fragment {
    private static final String BLUETOOTH_DEVICE_PARAM = "BLUETOOTH_DEVICE";
    private BluetoothDevice mDevice;

    public NormalBluetoothFragment() {
    }

    public static NormalBluetoothFragment newInstance(BluetoothDevice device) {
        NormalBluetoothFragment fragment = new NormalBluetoothFragment();
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
        View inflate = inflater.inflate(R.layout.fragment_normal_bluetooth, container, false);
        initBluetooth();
        return inflate;
    }

    private void initBluetooth() {
        new ConnectThread(mDevice).start();
    }


    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;
        private BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

        public ConnectThread(BluetoothDevice mDevice) {
            this.mDevice = mDevice;
            BluetoothSocket tmp = null;
            try {
                tmp = mDevice.createRfcommSocketToServiceRecord(BleContant.SERVER_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        public void run() {
            mAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                Logger.e("------>  连接成功");
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
