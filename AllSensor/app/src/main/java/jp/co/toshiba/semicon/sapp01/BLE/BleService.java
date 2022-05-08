/**
 * COPYRIGHT (C) 2017
 * TOSHIBA CORPORATION STORAGE & ELECTRONIC DEVICES SOLUTIONS COMPANY
 * ALL RIGHTS RESERVED
 *
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". TOSHIBA
 * CORPORATION MAKES NO OTHER WARRANTY OF ANY KIND, WHETHER EXPRESS, IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. TOSHIBA CORPORATION
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * TOSHIBA CORPORATION SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 */

package jp.co.toshiba.semicon.sapp01.BLE;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.*;
import java.util.UUID;

import jp.co.toshiba.semicon.sapp01.Main.*;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class BleService extends Service {


    private static final String TAG = MainActivity.LOG_TAG;

    private BluetoothManager mBtManager;
    private BluetoothAdapter mBtAdapter;
    private String mBtDevAddress;
    private BluetoothGatt mBtGatt;
    private int mStatus;

    /**
     *  Reflection of "BluetoothGatt.refresh()"
     **/
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method localMethod = gatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                boolean ret = (Boolean) localMethod.invoke(gatt);
                if (!ret) {
                    Log.d(TAG, "refreshDeviceCache() failed");
                }
                return ret;
            }
        }
        catch (Exception localException) {
            Log.d(TAG, "Exception occurred while refreshing device");
        }
        return false;
    }

    public boolean connectReq(final String address) {

        if (mBtAdapter == null || address == null) {
            return false;
        }

        if (mStatus == BluetoothProfile.STATE_CONNECTED) {
            Log.d(TAG, "connectReq() status error");
            return false;
        }

        if (address.equals(mBtDevAddress) && mBtGatt != null) {
            Log.d(TAG, "Trying to reconnect");
            if (mBtGatt.connect()) {
                mStatus = BluetoothProfile.STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }

        mStatus = BluetoothProfile.STATE_CONNECTING;
        mBtGatt = device.connectGatt(this, false, mGattCallback);

        setBtDevAddress(address);
        return true;
    }


    public boolean disconnectReq() {

        if (mBtAdapter == null || mBtGatt == null) {
            return false;
        }

        mStatus = BluetoothProfile.STATE_DISCONNECTING;
        mBtGatt.disconnect();

        return true;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "onConnectionStateChange: " + mStatus + " -> " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    MainActivity.threadSleep(600);

                    discoverServices();

                    // In order not to disconnect during discoverServices()
                    MainActivity.threadSleep(700);

                    broadcast(BLE_CONNECTED);
                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                    close();

                    if (mStatus == BluetoothProfile.STATE_DISCONNECTING) {
                        broadcast(BLE_DISCONNECTED);
                    }
                    else {
                        broadcast(BLE_DISCONNECTED_LL); // Link Loss
                    }
                }
                mStatus = newState;
            }
            else {

                Log.d(TAG, "onConnectionStateChange  error:" + status);
                mStatus = BluetoothProfile.STATE_DISCONNECTED;

                close();
                broadcast(BLE_DISCONNECTED_LL);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if ((gatt.getService(SERVICE_GENERIC_ACCESS)) != null) {
                    broadcast(BLE_SERVICE_DISCOVERED_GA);
                }
                if ((gatt.getService(SERVICE_DEVICE_INFO)) != null) {
                    broadcast(BLE_SERVICE_DISCOVERED_DI);
                }
                if ((gatt.getService(SERVICE_BATTERY)) != null) {
                    broadcast(BLE_SERVICE_DISCOVERED_BAS);
                }
                if ((gatt.getService(SERVICE_SAPPHIRE_ORIGINAL)) != null) {
                    broadcast(BLE_SERVICE_DISCOVERED_SOS);
                }
                if ((gatt.getService(SERVICE_EXTERNAL_MEMORY_MAINTENANCE)) != null) {
                    broadcast(BLE_SERVICE_DISCOVERED_EMM);
                }
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (CHARA_DEVICE_NAME.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_GA_DEVICE_NAME, characteristic);
                }
                else if (CHARA_APPEARANCE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_GA_APPEARANCE, characteristic);
                }
                else if (CHARA_SOFTWARE_REV.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_DI_SW_REV, characteristic);
                }
                else if (CHARA_FIRMWARE_REV.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_DI_FW_REV, characteristic);
                }
                else if (CHARA_HARDWARE_REV.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_DI_HW_REV, characteristic);
                }
                else if (CHARA_MANUFACTURE_NAME.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_R_DI_MANUFACTURER_NAME, characteristic);
                }
                else if (CHARA_EXTERNAL_MEMORY_READ2.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_EXTERNAL_MEMORY_READ2, characteristic);
                }
                else if (CHARA_EXTERNAL_MEMORY_CHECKSUM2.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2, characteristic);
                }
                else if (CHARA_FLAG_READ2.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLAG_READ2, characteristic);
                }
            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (CHARA_LED_CONFIG.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_W_LED, characteristic);
                }
                else if (CHARA_CONSOLE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHAR_W_CONSOLE, characteristic);
                }
                else if (CHARA_FLASH_OPEN.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLASH_OPEN, characteristic);
                }
                else if (CHARA_FLASH_CLOSE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLASH_CLOSE, characteristic);
                }
                else if (CHARA_EXTERNAL_MEMORY_READ1.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_EXTERNAL_MEMORY_READ1, characteristic);
                }
                else if (CHARA_EXTERNAL_MEMORY_CHECKSUM1.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1, characteristic);
                }
                else if (CHARA_FLASH_ERASE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLASH_ERASE, characteristic);
                }
                else if (CHARA_EXTERNAL_MEMORY_WRITE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_EXTERNAL_MEMORY_WRITE, characteristic);
                }
                else if (CHARA_FLAG_READ1.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLAG_READ1, characteristic);
                }
                else if (CHARA_FLAG_CHANGE.equals(characteristic.getUuid())) {
                    broadcast(BLE_CHARA_FLAG_CHANGE, characteristic);
                }
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            if (CHARA_BATTERY_LV.equals(characteristic.getUuid())) {
                broadcast(BLE_CHAR_N_BATTERY_LV, characteristic);
            }
            else if (CHARA_SWITCH.equals(characteristic.getUuid())) {
                broadcast(BLE_CHAR_N_SWITCH, characteristic);
            }
        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) {
                    if (CHARA_BATTERY_LV.equals(descriptor.getCharacteristic().getUuid())) {
                        broadcast(BLE_DESC_W_BATTERY_LV, descriptor);
                    }
                    else if (CHARA_SWITCH.equals(descriptor.getCharacteristic().getUuid())) {
                        broadcast(BLE_DESC_W_SWITCH, descriptor);
                    }
                }
            }
        }
    };


    private void broadcast(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    private void broadcast(final String action, final BluetoothGattCharacteristic characteristic) {

        final Intent intent = new Intent(action);

        if ((CHARA_DEVICE_NAME.equals(characteristic.getUuid())) ||
                (CHARA_APPEARANCE.equals(characteristic.getUuid())) ||
                (CHARA_SOFTWARE_REV.equals(characteristic.getUuid())) ||
                (CHARA_FIRMWARE_REV.equals(characteristic.getUuid())) ||
                (CHARA_HARDWARE_REV.equals(characteristic.getUuid())) ||
                (CHARA_MANUFACTURE_NAME.equals(characteristic.getUuid())) ||
                (CHARA_CONSOLE.equals(characteristic.getUuid())) 
                ) {
            final String data = characteristic.getStringValue(0);
            if (data != null && data.length() > 0) {
                intent.putExtra(EXTRA_DATA, data);
            }
        }
        else if ( (CHARA_EXTERNAL_MEMORY_READ2.equals(characteristic.getUuid())) ||
                (CHARA_EXTERNAL_MEMORY_CHECKSUM2.equals(characteristic.getUuid())) ||
                (CHARA_FLAG_READ2.equals(characteristic.getUuid()))
                ){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                intent.putExtra(EXTRA_DATA, data);
            }
        }
        else {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X", byteChar));
                }
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }

        sendBroadcast(intent);
    }


    private void broadcast(final String action, final BluetoothGattDescriptor descriptor) {

        final Intent intent = new Intent(action);

        if (CLIENT_CHARACTERISTIC_CONFIG.equals(descriptor.getUuid())) {

            final byte[] data = descriptor.getValue();

            if (data != null && data.length == 2) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X", byteChar));
                }
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }


    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }


    private final IBinder mBinder = new LocalBinder();

    public boolean init() {

        mStatus = BluetoothProfile.STATE_DISCONNECTED;

        if (mBtManager == null) {
            mBtManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBtManager == null) {
                return false;
            }
        }
        mBtAdapter = mBtManager.getAdapter();

        return mBtAdapter != null;
    }


    private void close() {

        if (mBtGatt == null) {
            return;
        }
        mBtGatt.close();
        mBtGatt = null;
    }


    private void discoverServices() {
        if (mBtAdapter == null || mBtGatt == null) {
            return;
        }
        mBtGatt.discoverServices();
    }


    public void readCharacteristic(BluetoothGattCharacteristic ch) {
        if (mBtAdapter == null || mBtGatt == null) {
            return;
        }
        mBtGatt.readCharacteristic(ch);
    }


    public void writeCharacteristic(BluetoothGattCharacteristic ch) {
        if (mBtAdapter == null || mBtGatt == null) {
            return;
        }
        mBtGatt.writeCharacteristic(ch);
    }


    public void enableNotification(BluetoothGattCharacteristic ch) {

        if (mBtAdapter == null || mBtGatt == null) {
            return;
        }
        mBtGatt.setCharacteristicNotification(ch, true);

        BluetoothGattDescriptor descriptor = ch.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            Log.d(TAG, "get descriptor failed");
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBtGatt.writeDescriptor(descriptor);
    }


    public void disableNotificationIndication(BluetoothGattCharacteristic ch) {

        if (mBtAdapter == null || mBtGatt == null) {
            return;
        }

        BluetoothGattDescriptor descriptor = ch.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            Log.d(TAG, "get descriptor failed");
            return;
        }
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE );
        mBtGatt.writeDescriptor(descriptor);

        mBtGatt.setCharacteristicNotification(ch, false);
    }


    public BluetoothGattCharacteristic getCharacteristicByUuid( UUID serviceUUID,  UUID charaUUID ) {

        if (mBtAdapter == null || mBtGatt == null) {
            return null;
        }

        BluetoothGattService ser = mBtGatt.getService(serviceUUID);
        if (ser == null) {
            return null;
        }
        return ser.getCharacteristic(charaUUID);
    }


    public BluetoothAdapter getBtAdapter() {
        return mBtAdapter;
    }


    private void setBtDevAddress(String deviceAddress) {
        mBtDevAddress = deviceAddress;
    }


    public int getStatus() {
        return mStatus;
    }

    public static byte[] hexStringToBinary(String hexString) {

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(i * 2, (i + 1) * 2), 16);
        }
        return bytes;
    }
}
