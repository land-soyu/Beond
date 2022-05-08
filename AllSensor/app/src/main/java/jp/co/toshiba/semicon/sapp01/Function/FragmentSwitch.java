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

package jp.co.toshiba.semicon.sapp01.Function;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import jp.co.toshiba.semicon.sapp01.BLE.BleService;
import jp.co.toshiba.semicon.sapp01.Main.MainActivity;
import jp.co.toshiba.semicon.sapp01.R;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentSwitch extends Fragment {

    public static FragmentSwitch newInstance(int sectionNumber) {
        FragmentSwitch fragment = new FragmentSwitch();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentSwitch() {
    }

    private Callbacks mCallbacks;
    private ToggleButton mTglBtnNotify;
    private ImageView mImgViewDipOff;
    private ImageView mImgViewDipOn;
    private ImageView mImgViewPushOff;
    private ImageView mImgViewPushOn;
    private boolean mIsNotifyON;

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

        mIsNotifyON = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_switch, container, false);

        mTglBtnNotify = (ToggleButton)rootView.findViewById(R.id.tglNotifyKey);
        mImgViewDipOff = (ImageView)rootView.findViewById(R.id.imageView_dipOff);
        mImgViewDipOn = (ImageView)rootView.findViewById(R.id.imageView_dipOn);
        mImgViewPushOff = (ImageView)rootView.findViewById(R.id.imageView_pushOff);
        mImgViewPushOn = (ImageView)rootView.findViewById(R.id.imageView_pushOn);

        mTglBtnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BleService service = MainActivity.sBleService;
                if (service != null) {
                    if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                        BluetoothGattCharacteristic ch = service.getCharacteristicByUuid(SERVICE_SAPPHIRE_ORIGINAL, CHARA_SWITCH);
                        if (ch == null) {
                            Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                            mTglBtnNotify.setChecked(false);
                        }
                        else {
                            if (((ToggleButton)v).isChecked()) {
                                mCallbacks.cbAppendLog("Enable Switch-Notification");
                                service.enableNotification(ch);
                            }
                            else {
                                mCallbacks.cbAppendLog("Disable Switch-Notification");
                                service.disableNotificationIndication(ch);
                                showKeycode(false, (byte) 0, (byte) 0);
                            }
                        }
                    }
                    else {
                        mCallbacks.cbAppendLog("[FrSWITCH]BLE Not connected");
                        mTglBtnNotify.setChecked(false);
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTglBtnNotify.setChecked(mIsNotifyON);

        final BleService service = MainActivity.sBleService;
        if (service != null) {
            if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                mTglBtnNotify.setEnabled(true);
            }
            else {
                mTglBtnNotify.setEnabled(false);
            }

            mImgViewDipOff.setVisibility(View.VISIBLE);
            mImgViewDipOn.setVisibility(View.INVISIBLE);
            mImgViewPushOff.setVisibility(View.VISIBLE);
            mImgViewPushOn.setVisibility(View.INVISIBLE);
        }

        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mIsNotifyON) {
            BleService service = MainActivity.sBleService;
            if (service != null) {
                BluetoothGattCharacteristic ch = service.getCharacteristicByUuid(SERVICE_SAPPHIRE_ORIGINAL, CHARA_SWITCH);
                mCallbacks.cbAppendLog("Disable Switch-Notification");
                try {
                    service.disableNotificationIndication(ch);
                } catch (NullPointerException ignored) {
                }
            }
            mTglBtnNotify.setEnabled(false);
            mIsNotifyON = false;
        }

        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    private void showKeycode(boolean pressRelease, byte pushKeycode, byte dipKeycode) {

        if (pressRelease) {

            if ( pushKeycode == 0x00 ){
                mImgViewPushOff.setVisibility(View.INVISIBLE);
                mImgViewPushOn.setVisibility(View.VISIBLE);
            }
            else if ( pushKeycode == 0x01 ){
                mImgViewPushOff.setVisibility(View.VISIBLE);
                mImgViewPushOn.setVisibility(View.INVISIBLE);
            }

            if ( dipKeycode == 0x00 ){
                mImgViewDipOff.setVisibility(View.INVISIBLE);
                mImgViewDipOn.setVisibility(View.VISIBLE);
            }
            else if ( dipKeycode == 0x01 ){
                mImgViewDipOff.setVisibility(View.VISIBLE);
                mImgViewDipOn.setVisibility(View.INVISIBLE);
            }
        }
        else {
            mImgViewDipOff.setVisibility(View.VISIBLE);
            mImgViewDipOn.setVisibility(View.INVISIBLE);
            mImgViewPushOff.setVisibility(View.VISIBLE);
            mImgViewPushOn.setVisibility(View.INVISIBLE);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: CONNECTED");

                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: DISCONNECTED");

                    mIsNotifyON = false;
                    mTglBtnNotify.setChecked(false);
                    mTglBtnNotify.setEnabled(false);
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: DISCONNECTED_LL");

                    mIsNotifyON = false;
                    mTglBtnNotify.setChecked(false);
                    mTglBtnNotify.setEnabled(false);
                    break;
                case BLE_SERVICE_DISCOVERED_SOS:
                    mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: SRV_DISCOVERED_SOS");
                    mTglBtnNotify.setEnabled(true);
                    break;
                case BLE_DESC_W_SWITCH: {

                    String hexString = intent.getStringExtra(EXTRA_DATA);
                    byte[] bytes = BleService.hexStringToBinary(hexString);
                    if ((bytes[0] == (byte) 0x01) && (bytes[1] == (byte) 0x00)) {
                        mIsNotifyON = true;
                        mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: DESC_W_SWITCH On");
                    } else {
                        mIsNotifyON = false;
                        mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: DESC_W_SWITCH Off");
                    }
                    mTglBtnNotify.setChecked(mIsNotifyON);
                    break;
                }
                case BLE_CHAR_N_SWITCH: {

                    String data = intent.getStringExtra(EXTRA_DATA);
                    byte[] bytes = BleService.hexStringToBinary(data);
                    mCallbacks.cbAppendLog("[FrSWITCH]BC Rx: CHAR_N_SWITCH  " + data);

                    showKeycode(true, bytes[0], bytes[1]);
                    break;
                }
            }
        }
    };


    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED_SOS);
        intentFilter.addAction(BLE_DESC_W_SWITCH);
        intentFilter.addAction(BLE_CHAR_N_SWITCH);
        return intentFilter;
    }
}

