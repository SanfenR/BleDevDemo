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
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liking.android.bleopendoordemo.ble.BleContant;
import com.liking.android.bleopendoordemo.data.DataPack;
import com.liking.android.bleopendoordemo.spp.BluetoothConnectActivityReceiver;
import com.liking.android.bleopendoordemo.utils.NumberUtil;
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

    private AppCompatEditText mEtData;

    private BluetoothConnectActivityReceiver mConnectActivityReceiver;

    private static final String TAG = "NormalBluetoothFragment";
    BluetoothHeadset mBluetoothHeadset = null;

    ReadAndWriteTread mReadTread;

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
        View inflate = inflater.inflate(R.layout.fragment_connect, container, false);
        inflate.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initBluetooth();
            }
        });
        ((TextView) inflate.findViewById(R.id.tv_mac_address)).setText(mDevice.getAddress());
        mTvLog = inflate.findViewById(R.id.log);
        mEtData = inflate.findViewById(R.id.et_data);

        inflate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReadTread == null) {
                    appendTextView("----> 发送失败，需要先连接设备");
                } else {
                    String result = mEtData.getText().toString();
                    appendTextView("send: ", result);
                    byte[] request = DataPack.openDoor(result);
                    appendTextView(Arrays.toString(request));
                    appendTextView(NumberUtil.bytesToHexFun(request));
                    mReadTread.write(request);
                }
            }
        });

        inflate.findViewById(R.id.btn_device_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mReadTread == null) {
                    appendTextView("----> 发送失败，需要先连接设备");
                } else {
                    byte[] bytes = DataPack.deviceInfo();
                    appendTextView("send: ", "设备信息");
                    appendTextView(Arrays.toString(bytes));
                    appendTextView(NumberUtil.bytesToHexFun(bytes));
                    mReadTread.write(bytes);
                }
            }
        });

        inflate.findViewById(R.id.btn_clear_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLog();
            }
        });

        return inflate;
    }


    @Override
    public void onResume() {
        super.onResume();
        mBluetoothAdapter.getProfileProxy(getActivity(), mProfileListener, BluetoothProfile.HEADSET);
        mConnectActivityReceiver = new BluetoothConnectActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothConnectActivityReceiver.ACTION_PIN_ACTIVITY);
        getActivity().registerReceiver(mConnectActivityReceiver, intentFilter);
    }


    @Override
    public void onPause() {
        super.onPause();
        mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
        if (mReadTread != null) {
            mReadTread.quit();
        }
        if (mConnectActivityReceiver != null)
            getActivity().unregisterReceiver(mConnectActivityReceiver);
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
                Message message = mHandler.obtainMessage();
                message.what = TYPE_STRING;
                message.obj = "------>  连接成功";
                mHandler.sendMessage(message);
                Log.e(TAG, "------>  连接成功");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            manageConnectedSocket(mSocket);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void manageConnectedSocket(BluetoothSocket mSocket) {
        if (mReadTread != null) {
            mReadTread.quit();
            mReadTread = null;
        }
        mReadTread = new ReadAndWriteTread(mSocket);
        mReadTread.start();
    }


    public static final int TYPE_STRING = 100;
    public static final int TYPE_BYTE = 101;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TYPE_STRING:
                    appendTextView(msg.obj.toString());
                    break;
                case TYPE_BYTE:
                    byte[] b = (byte[]) msg.obj;
                    appendTextView("ret:", Arrays.toString(b));
                    appendTextView("ret:", NumberUtil.bytesToHexFun(b));
                    break;
            }


        }
    };

    private class ReadAndWriteTread extends Thread {
        InputStream inputStream;
        OutputStream outputStream;
        Boolean isLoop = true;
        byte[] bytes = new byte[512];

        public ReadAndWriteTread(BluetoothSocket socket) {
            try {
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (isLoop) {
                try {
                    int len = inputStream.read(bytes);
                    if (len == -1) {
                        quit();
                        return;
                    }
                    byte[] arr = new byte[len];
                    System.arraycopy(bytes, 0, arr, 0, len);
                    Message message = mHandler.obtainMessage();
                    message.what = TYPE_BYTE;
                    message.obj = arr;
                    mHandler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void quit() {
            isLoop = false;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
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

    private void clearLog() {
        builder.delete(0, builder.length());
        mTvLog.setText("");
    }

}
