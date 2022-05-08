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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.*;

import jp.co.toshiba.semicon.sapp01.*;
import jp.co.toshiba.semicon.sapp01.Function.FragmentADC;
import jp.co.toshiba.semicon.sapp01.Main.*;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentBLE extends Fragment {

    public static FragmentBLE newInstance(int sectionNumber) {
        FragmentBLE fragment = new FragmentBLE();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentBLE() {
    }


    private Callbacks mCallbacks;
    private BluetoothAdapter mBTAdapter;
    private BluetoothLeScanner mBTLeScanner;
    private DeviceAdapter mDeviceAdapter;
    private BluetoothDevice mSelectedDevice;
    private String mDeviceAddress;
    private String mDeviceName;
    private ToggleButton mTglBtnScan;
    private Button mBtnDisconnect;
    private LinearLayout mLayoutSelDev;
    private TextView mTxtVwDeviceName;
    private TextView mTxtVwBdAddress;
    private TextView mTxtVwConnState;
    private boolean mIsScanning;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;

    private static final String BONDING_START    = "com.toshiba.semicon.sapp01.BONDING_START";
    private static final String BONDING_EXIT     = "com.toshiba.semicon.sapp01.BONDING_EXIT";
    private static final String BONDING_ERR_EXIT0 = "com.toshiba.semicon.sapp01.BONDING_ERR_EXIT0";
    private static final String BONDING_ERR_EXIT1 = "com.toshiba.semicon.sapp01.BONDING_ERR_EXIT1";
    private static final String BONDING_ERR_EXIT2 = "com.toshiba.semicon.sapp01.BONDING_ERR_EXIT2";

    public interface Callbacks {
        void cbAppendLog(String text);
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("activity does not implemented Callbacks");
        }
        else {
            mCallbacks = ((Callbacks) activity);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException("activity does not implemented Callbacks");
        }
        else {
            mCallbacks = ((Callbacks) context);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsScanning = false;
        mDeviceAddress = null;
        mDeviceName = null;

        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ble, container, false);

        Button BtnShowBondDev = (Button) rootView.findViewById(R.id.btn_showBondDev);
        mTglBtnScan = (ToggleButton)rootView.findViewById(R.id.toggleButtonScan);
        mLayoutSelDev = (LinearLayout)rootView.findViewById(R.id.layoutSelDev);
        mBtnDisconnect    = (Button)rootView.findViewById(R.id.btn_Disconnect);
        mTxtVwDeviceName = (TextView)rootView.findViewById(R.id.txtVw_DeviceName);
        mTxtVwBdAddress = (TextView)rootView.findViewById(R.id.txtVw_BdAddress);
        mTxtVwConnState   = (TextView)rootView.findViewById(R.id.txtVw_ConnState);

        mLayoutSelDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("ss" , "mLayoutSelDev.setOnClickListener ==== ");
                mLayoutSelDev.setBackgroundColor(0xFFD0FFD0);
//                if(mLayoutSelDev.getResources().getColor(0xFFD0FFD0)){
                    FragmentADC.newInstance(3);

//                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            if (mIsScanning) {
                                if ((mDeviceAdapter != null) && (result.getDevice() != null)) {
                                    mDeviceAdapter.update(result.getDevice(), result.getRssi());
                                }
                            }
                        }
                    });
                }
            };
        }
        else{
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice newDevice, final int newRssi, final byte[] newScanRecord) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsScanning) {
                                if ((mDeviceAdapter != null) && (newDevice != null)) {
                                    mDeviceAdapter.update(newDevice, newRssi);
                                }
                            }
                        }
                    });
                }
            };
        }

        final BleService service = MainActivity.sBleService;
        if (service != null) {
            mTxtVwDeviceName.setText(mDeviceName);
            mTxtVwBdAddress.setText(mDeviceAddress);
            if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                mBtnDisconnect.setEnabled(true);
                mTglBtnScan.setEnabled(false);
                mLayoutSelDev.setBackgroundColor(0xFFD0FFD0);
                mTxtVwConnState.setText(getString(R.string.str_Connected));


            }
            else {
                mBtnDisconnect.setEnabled(false);
                mTglBtnScan.setEnabled(true);
                mLayoutSelDev.setBackgroundColor(0xFFFBFFFF);
                mTxtVwConnState.setText(getString(R.string.str_ChooseDevToConn));
            }
        }
        else {
            mLayoutSelDev.setBackgroundColor(0xFFFBFFFF);
            mBtnDisconnect.setEnabled(false);

            mTxtVwDeviceName.setText(getString(R.string.str_defaultValue));
            mTxtVwBdAddress.setText(getString(R.string.str_defaultValue));
            mTxtVwConnState.setText(getString(R.string.str_ChooseDevToConn));
        }
        mTglBtnScan.setChecked(mIsScanning);


        mTglBtnScan.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MainActivity.sProgressBar.setVisibility(View.VISIBLE);
                    startScan();
                }
                else {
                    MainActivity.sProgressBar.setVisibility(View.GONE);
                    stopScan();
                }
            }
        });


        mBtnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleDisconnect();
            }
        });

        BtnShowBondDev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopScan();

                mDeviceAdapter.clear();
                mBTAdapter = MainActivity.sBleService.getBtAdapter();

                if (mBTAdapter != null) {
                    Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();

                    for (final BluetoothDevice device : pairedDevices) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDeviceAdapter != null) {
                                    mDeviceAdapter.update(device, 0x7F); // rssi=0x7F provisional interface
                                }
                            }
                        });
                    }
                }
            }
        });

        ListView deviceListView = (ListView)rootView.findViewById(R.id.deviceList);
        mDeviceAdapter = new DeviceAdapter(getActivity(), new ArrayList<ScannedDevice>());
        deviceListView.setAdapter(mDeviceAdapter);

        deviceListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                final ScannedDevice item = mDeviceAdapter.getItem(position);

                if (item != null) {
                    mSelectedDevice = item.getDevice();
                    bondingOrConnecting();
                }
            }
        });
        stopScan();

        return rootView;
    }


    @Override
    public void onDestroy() {

        stopScan();
        mCallbacks.cbAppendLog("disconnect request  onDestroy");
        MainActivity.sBleService.disconnectReq();

        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }


    @SuppressWarnings("deprecation")
    private void startScan() {

        mDeviceAdapter.clear();
        mBTAdapter = MainActivity.sBleService.getBtAdapter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBTLeScanner = mBTAdapter.getBluetoothLeScanner();
        }

        if ((mBTAdapter != null) && (!mIsScanning)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCallbacks.cbAppendLog("startScan()");
                mBTLeScanner.startScan(mScanCallback);
            }
            else {
                mCallbacks.cbAppendLog("startLeScan()");
                mBTAdapter.startLeScan(mLeScanCallback);
            }
            mIsScanning = true;
            mTglBtnScan.setChecked(true);
        }
    }


    @SuppressWarnings("deprecation")
    private void stopScan() {

        if (mBTAdapter != null) {
            mIsScanning = false;
            mTglBtnScan.setChecked(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mCallbacks.cbAppendLog("stopScan()");
                if (mBTLeScanner != null){
                    mBTLeScanner.stopScan(mScanCallback);
                }
            }
            else {
                mCallbacks.cbAppendLog("stopLeScan()");
                mBTAdapter.stopLeScan(mLeScanCallback);
            }
        }
        else {
            mCallbacks.cbAppendLog("BluetoothAdapter :null");
        }
    }


    private void bleDisconnect() {

        mCallbacks.cbAppendLog("disconnect request");

        if( MainActivity.sBleService.disconnectReq()) {
            MainActivity.startWaitDialog();
        }
        else {
            mCallbacks.cbAppendLog("disconnect request failed");
        }
    }


    private void bleConnect(BluetoothDevice btDevice) {

        stopScan();

        // Erase listView.
        mDeviceAdapter.clear();
        mDeviceAdapter.notifyDataSetChanged();

        // Set Selected Device
        mDeviceAddress = btDevice.toString();
        mTxtVwBdAddress.setText(mDeviceAddress);

        mDeviceName = btDevice.getName();
        if (mDeviceName == null) {
            mDeviceName = "SAPPHIRE ?"; // WORKAROUND: case of connecting by NFC Tag
        }
        mTxtVwDeviceName.setText(mDeviceName);

        mCallbacks.cbAppendLog("bleConnect()  ADDRESS: " + mDeviceAddress);


        mCallbacks.cbAppendLog("connect request");

        if( MainActivity.sBleService.connectReq(mDeviceAddress)) {
            MainActivity.startWaitDialog();
        }
        else {
            mCallbacks.cbAppendLog("connect request failed");
        }
    }


    private void bondingOrConnecting() {

        int bond = mSelectedDevice.getBondState();
        final String deviceAddress = mSelectedDevice.toString();
        final String deviceName = mSelectedDevice.getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (bond == BluetoothDevice.BOND_NONE) {
            mCallbacks.cbAppendLog("getBondState  BOND_NONE");

            builder.setMessage("Start pairing?\n (and start connecting)" +
                    "\n\n  NAME:  \t" + deviceName +
                    "\n  ADDRESS:  \t" + deviceAddress);

            builder.setPositiveButton(getString(R.string.str_Yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // AlertDialog "Yes"
                    stopScan();

                    // Erase all contents of listView.
                    mDeviceAdapter.clear();
                    mDeviceAdapter.notifyDataSetChanged();

                    broadcast(BONDING_START);
                }
            });

            builder.setNegativeButton(getString(R.string.str_No), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // AlertDialog "No"
                }
            });

            builder.show();
        } else if (bond == BluetoothDevice.BOND_BONDED) {

            if (MainActivity.sBleService.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                return;
            }

            mCallbacks.cbAppendLog("getBondState  BOND_BONDED");

            builder.setMessage("Start connecting?" +
                    "\n\n  NAME:  \t" + deviceName +
                    "\n  ADDRESS:  \t" + deviceAddress);


            builder.setPositiveButton(getString(R.string.str_Yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    bleConnect(mSelectedDevice);
                }
            });

            builder.setNegativeButton(getString(R.string.str_No), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // AlertDialog "No"
                }
            });

            builder.show();
        }
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (BLE_CONNECTED.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: CONNECTED");

                mTxtVwConnState.setText(getString(R.string.str_Connected));
                mLayoutSelDev.setBackgroundColor(0xFFD0FFD0);

                mBtnDisconnect.setEnabled(true);
                mTglBtnScan.setEnabled(false);

                MainActivity.stopWaitDialog();
            }
            else if (BLE_DISCONNECTED.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: DISCONNECTED");

                mLayoutSelDev.setBackgroundColor(0xFFFBFFFF);
                mTxtVwConnState.setText(getString(R.string.str_Disconnected));

                mBtnDisconnect.setEnabled(false);
                mTglBtnScan.setEnabled(true);

                MainActivity.stopWaitDialog();
            }
            else if (BLE_DISCONNECTED_LL.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: DISCONNECTED_LL");

                mLayoutSelDev.setBackgroundColor(0xFFFBFFFF);
                mDeviceAddress = null;
                mDeviceName = null;

                mBtnDisconnect.setEnabled(false);
                mTglBtnScan.setEnabled(true);

                mTxtVwDeviceName.setText(getString(R.string.str_defaultValue));
                mTxtVwBdAddress.setText(getString(R.string.str_defaultValue));
                mTxtVwConnState.setText(getString(R.string.str_ChooseDevToConn));

                MainActivity.stopWaitDialog();
                Toast.makeText(getActivity(), R.string.str_notify_disconnected, Toast.LENGTH_SHORT).show();
            }
            else if (BONDING_START.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: BONDING_START");

                MainActivity.startWaitDialog();

                WaitForBondingThread waitBondingTh = new WaitForBondingThread(mSelectedDevice);
                waitBondingTh.start();
            }
            else if (BONDING_EXIT.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: BONDING_EXIT");

                MainActivity.stopWaitDialog();

                bleConnect(mSelectedDevice);
            }
            else if (BONDING_ERR_EXIT0.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: BONDING_ERR_EXIT0");

                MainActivity.stopWaitDialog();
            }
            else if (BONDING_ERR_EXIT1.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: BONDING_ERR_EXIT1  Count Over");

                MainActivity.stopWaitDialog();
                Toast.makeText(getActivity(), "3 times CANCEL", Toast.LENGTH_LONG).show();
            }
            else if (BONDING_ERR_EXIT2.equals(action)) {
                mCallbacks.cbAppendLog("[FrBLE]BC Rx: BONDING_ERR_EXIT2  Timeout");

                MainActivity.stopWaitDialog();
                Toast.makeText(getActivity(), "Timeout", Toast.LENGTH_LONG).show();
            }
        }
    };


    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(BONDING_START);
        intentFilter.addAction(BONDING_EXIT);
        intentFilter.addAction(BONDING_ERR_EXIT1);
        intentFilter.addAction(BONDING_ERR_EXIT2);
        return intentFilter;
    }


    private void broadcast(final String action) {

        final Intent intent = new Intent(action);
        getActivity().sendBroadcast(intent);
    }


    private class WaitForBondingThread extends Thread {

        static private final int BOND_CHECK_INTERVAL = 1000;
        static private final int BOND_CHECK_TIME_MAX = 30000;
        static private final int BONDING_REPEAT_COUNT_MAX = 3;
        BluetoothDevice mBtDevice;

        WaitForBondingThread (BluetoothDevice btDevice) {
            this.mBtDevice = btDevice;
        }

        public void run() {

            int bond;
            int repeatCount = 0;
            int elapsedTime = 0;
            boolean bonding = false;

            if (mBtDevice == null) {
                broadcast(BONDING_ERR_EXIT0);
                return;
            }

            if (!mBtDevice.createBond()) {
                mCallbacks.cbAppendLog("createBond is failed");
                broadcast(BONDING_ERR_EXIT0);
                return;
            }
            else {
                mCallbacks.cbAppendLog("Try createBond");
                repeatCount++;
            }

            do {
                MainActivity.threadSleep(BOND_CHECK_INTERVAL);

                bond = mBtDevice.getBondState();
                mCallbacks.cbAppendLog("Wait for bonding   BondState:" + bond);

                elapsedTime += BOND_CHECK_INTERVAL;

                if (bond == BluetoothDevice.BOND_BONDING) {

                    bonding = true;
                }
                else if (bond == BluetoothDevice.BOND_NONE) {

                    if (repeatCount >= BONDING_REPEAT_COUNT_MAX) {
                        broadcast(BONDING_ERR_EXIT1);
                        return;
                    }

                    if (elapsedTime >= BOND_CHECK_TIME_MAX) {
                        broadcast(BONDING_ERR_EXIT2);
                        return;
                    }

                    if (bonding) {
                        if (!mBtDevice.createBond()) {
                            mCallbacks.cbAppendLog("createBond is failed");
                            broadcast(BONDING_ERR_EXIT0);
                            return;
                        }
                        else {
                            mCallbacks.cbAppendLog("Try createBond");
                            repeatCount++;
                        }
                    }
                    bonding = false;
                }

            }
            while (bond != BluetoothDevice.BOND_BONDED);

            broadcast(BONDING_EXIT);
        }
    }

}

