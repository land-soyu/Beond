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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import jp.co.toshiba.semicon.sapp01.BLE.BleService;
import jp.co.toshiba.semicon.sapp01.Main.MainActivity;
import jp.co.toshiba.semicon.sapp01.R;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentDevice extends Fragment {

    public static FragmentDevice newInstance(int sectionNumber) {
        FragmentDevice fragment = new FragmentDevice();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentDevice() {
    }

    private Callbacks mCallbacks;
    private Button mBtnReadDeviceName;
    private Button mBtnReadAppearance;
    private Button mBtnReadSwRev;
    private Button mBtnReadFwRev;
    private Button mBtnReadHwRev;
    private Button mBtnReadManufacturerName;
    private TextView mTxtVwReadDeviceName;
    private TextView mTxtVwReadAppearance;
    private TextView mTxtVwReadSwRev;
    private TextView mTxtVwReadFwRev;
    private TextView mTxtVwReadHwRev;
    private TextView mTxtVwReadManufacturerName;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_device, container, false);

        mBtnReadDeviceName = (Button)rootView.findViewById(R.id.btn_ReadDeviceName);
        mBtnReadAppearance = (Button)rootView.findViewById(R.id.btn_ReadAppearance);
        mBtnReadSwRev = (Button)rootView.findViewById(R.id.btn_ReadSwRev);
        mBtnReadFwRev = (Button)rootView.findViewById(R.id.btn_ReadFwRev);
        mBtnReadHwRev = (Button)rootView.findViewById(R.id.btn_ReadHwRev);
        mBtnReadManufacturerName = (Button)rootView.findViewById(R.id.btn_ReadManufacturerName);

        mTxtVwReadDeviceName = (TextView)rootView.findViewById(R.id.txtVw_ReadDeviceName);
        mTxtVwReadAppearance = (TextView)rootView.findViewById(R.id.txtVw_ReadAppearance);
        mTxtVwReadSwRev = (TextView)rootView.findViewById(R.id.txtVw_ReadSwRev);
        mTxtVwReadFwRev = (TextView)rootView.findViewById(R.id.txtVw_ReadFwRev);
        mTxtVwReadHwRev = (TextView)rootView.findViewById(R.id.txtVw_ReadHwRev);
        mTxtVwReadManufacturerName = (TextView)rootView.findViewById(R.id.txtVw_ReadManufacturerName);

        mBtnReadDeviceName.setOnClickListener(btnReadXxxListener);
        mBtnReadAppearance.setOnClickListener(btnReadXxxListener);
        mBtnReadSwRev.setOnClickListener(btnReadXxxListener);
        mBtnReadFwRev.setOnClickListener(btnReadXxxListener);
        mBtnReadHwRev.setOnClickListener(btnReadXxxListener);
        mBtnReadManufacturerName.setOnClickListener(btnReadXxxListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final BleService service = MainActivity.sBleService;
        if (service != null) {
            if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                setReadButtonAll(true);
            }
            else {
                setReadButtonAll(false);
            }
        }
        else {
            setReadButtonAll(false);
        }

        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    private final View.OnClickListener btnReadXxxListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            BleService service = MainActivity.sBleService;

            if (service != null) {
                BluetoothGattCharacteristic ch;
                int id = v.getId();

                switch (id) {
                    case R.id.btn_ReadDeviceName:
                        ch = service.getCharacteristicByUuid(SERVICE_GENERIC_ACCESS, CHARA_DEVICE_NAME);
                        break;
                    case R.id.btn_ReadAppearance:
                        ch = service.getCharacteristicByUuid(SERVICE_GENERIC_ACCESS, CHARA_APPEARANCE);
                        break;
                    case R.id.btn_ReadSwRev:
                        ch = service.getCharacteristicByUuid(SERVICE_DEVICE_INFO, CHARA_SOFTWARE_REV);
                        break;
                    case R.id.btn_ReadFwRev:
                        ch = service.getCharacteristicByUuid(SERVICE_DEVICE_INFO, CHARA_FIRMWARE_REV);
                        break;
                    case R.id.btn_ReadHwRev:
                        ch = service.getCharacteristicByUuid(SERVICE_DEVICE_INFO, CHARA_HARDWARE_REV);
                        break;
                    case R.id.btn_ReadManufacturerName:
                        ch = service.getCharacteristicByUuid(SERVICE_DEVICE_INFO, CHARA_MANUFACTURE_NAME);
                        break;
                    default:
                        ch = null;
                        break;
                }

                if (ch == null) {
                    Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                }
                else {
                    setReadButtonAll(false);
                    mCallbacks.cbAppendLog("Read characteristic request");
                    service.readCharacteristic(ch);
                }
            }
            else {
                mCallbacks.cbAppendLog("Read chara: BLE service Not work");
            }
        }
    });


    private void setReadButtonEnableGAP() {

        mBtnReadDeviceName.setEnabled(true);
        mBtnReadAppearance.setEnabled(true);
    }
    private void setReadButtonEnableDIS() {

        mBtnReadSwRev.setEnabled(true);
        mBtnReadFwRev.setEnabled(true);
        mBtnReadHwRev.setEnabled(true);
        mBtnReadManufacturerName.setEnabled(true);
    }
    private void setReadButtonAll(boolean EnableDisable) {

        mBtnReadDeviceName.setEnabled(EnableDisable);
        mBtnReadAppearance.setEnabled(EnableDisable);
        mBtnReadSwRev.setEnabled(EnableDisable);
        mBtnReadFwRev.setEnabled(EnableDisable);
        mBtnReadHwRev.setEnabled(EnableDisable);
        mBtnReadManufacturerName.setEnabled(EnableDisable);
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CONNECTED");

                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: DISCONNECTED");

                    setReadButtonAll(false);
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: DISCONNECTED_LL");

                    setReadButtonAll(false);
                    break;
                case BLE_SERVICE_DISCOVERED_GA:
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: SRV_DISCOVERED_GA");
                    setReadButtonEnableGAP();
                    break;
                case BLE_SERVICE_DISCOVERED_DI:
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: SRV_DISCOVERED_DI");
                    setReadButtonEnableDIS();
                    break;
                case BLE_CHAR_R_GA_DEVICE_NAME:

                    String name = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadDeviceName.setText(name);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_GA_DEVICE_NAME  " + name);

                    setReadButtonAll(true);
                    break;
                case BLE_CHAR_R_GA_APPEARANCE:

                    String appear = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadAppearance.setText(appear);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_GA_APPEARANCE  " + appear);

                    setReadButtonAll(true);
                    break;
                case BLE_CHAR_R_DI_SW_REV:

                    String swRev = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadSwRev.setText(swRev);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_DI_SW_REV  " + swRev);

                    setReadButtonAll(true);
                    break;
                case BLE_CHAR_R_DI_FW_REV:

                    String fmRev = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadFwRev.setText(fmRev);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_DI_FW_REV  " + fmRev);

                    setReadButtonAll(true);
                    break;
                case BLE_CHAR_R_DI_HW_REV:

                    String hwRev = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadHwRev.setText(hwRev);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_DI_HW_REV  " + hwRev);

                    setReadButtonAll(true);
                    break;
                case BLE_CHAR_R_DI_MANUFACTURER_NAME:

                    String manufacturerName = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadManufacturerName.setText(manufacturerName);
                    mCallbacks.cbAppendLog("[FrDI]BC Rx: CHAR_R_DI_MANUFACTURER_NAME  " + manufacturerName);

                    setReadButtonAll(true);
                    break;
            }
        }
    };


    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED_GA);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED_DI);
        intentFilter.addAction(BLE_CHAR_R_GA_DEVICE_NAME);
        intentFilter.addAction(BLE_CHAR_R_GA_APPEARANCE);
        intentFilter.addAction(BLE_CHAR_R_DI_SW_REV);
        intentFilter.addAction(BLE_CHAR_R_DI_FW_REV);
        intentFilter.addAction(BLE_CHAR_R_DI_HW_REV);
        intentFilter.addAction(BLE_CHAR_R_DI_MANUFACTURER_NAME);
        return intentFilter;
    }
}

