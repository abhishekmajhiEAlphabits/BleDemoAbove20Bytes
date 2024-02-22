package com.example.bledemo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModel;

public class GattServerViewModel extends ViewModel {
    private BluetoothDevice mBluetoothDevice;


    public GattServerViewModel(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    public String getServerName() {
        if (mBluetoothDevice == null) {
            return "";
        }
        return mBluetoothDevice.getAlias() + "\n" + mBluetoothDevice.getAddress();
    }
}
