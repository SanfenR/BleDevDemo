package com.liking.android.bleopendoordemo;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liking.android.bleopendoordemo.ble.BleContant;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NormalBluetoothFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NormalBluetoothFragment extends Fragment {
    private static final String BLUETOOTH_DEVICE_PARAM = "BLUETOOTH_DEVICE";
    private BluetoothDevice mDevice;

    private static final String TAG = "NormalBluetoothFragment";
    BluetoothHeadset mBluetoothHeadset = null;

    ReadTread mReadTread;
    OutputStream mOutputStream;

    TextView mTvLog;

    // Get the default adapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;

            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };

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
        inflate.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBluetooth();
            }
        });
        ((TextView)inflate.findViewById(R.id.tv_mac_address)).setText(mDevice.getAddress());
        mTvLog = inflate.findViewById(R.id.tv_log);
        return inflate;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEADSET);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
    }

    private void initBluetooth() {
        new ConnectThread(mDevice).start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice mDevice) {
            this.mDevice = mDevice;
            BluetoothSocket tmp = null;
            try {

                ParcelUuid[] uuids = mDevice.getUuids();
                tmp = mDevice.createRfcommSocketToServiceRecord(BleContant.SPP_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
                appendTextView("------>  连接成功");
                Log.e(TAG, "------>  连接成功");
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                if (mReadTread != null) {
                    mReadTread.quit();
                    mReadTread = null;
                }
                mReadTread = new ReadTread(mSocket.getInputStream());
                mOutputStream = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            appendTextView(msg.obj.toString());

        }
    };


    private class ReadTread extends Thread {
        InputStream inputStream;

        Boolean isLoop = true;

        byte[] bytes = new byte[512];

        public ReadTread(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            while (ReadTread.interrupted() && isLoop) {
                try {
                    int len = inputStream.read(bytes);
                    if (len == -1) {
                        quit();
                        return;
                    }

                    byte[] arr = new byte[len];

                    for (int i = 0; i < len; i ++) {
                        arr[i] = bytes[i];
                    }


                    Message message = mHandler.obtainMessage();
                    message.obj = arr;
                    mHandler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void quit() {
            interrupt();
            isLoop = false;
        }
    }

    private StringBuilder builder = new StringBuilder();

    private void appendTextView(String... strings) {
        for (String s : strings) {
            builder.append(s);
        }
        builder.append("\r\n");
        mTvLog.setText(builder.toString());
    }

}
