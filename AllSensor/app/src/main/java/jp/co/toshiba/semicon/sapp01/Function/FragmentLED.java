/*
 * COPYRIGHT (C) 2017
 * TOSHIBA ELECTRONIC DEVICES & STORAGE CORPORATION
 * ALL RIGHTS RESERVED
 *
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". TOSHIBA
 * ELECTRONIC DEVICES & STORAGE CORPORATION MAKES NO OTHER WARRANTY OF ANY
 * KIND, WHETHER EXPRESS, IMPLIED OR, STATUTORY AND DISCLAIMS ANY AND ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY, SATISFACTORY QUALITY, NON
 * INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. TOSHIBA ELECTRONIC
 * DEVICES & STORAGE CORPORATION RESERVES THE RIGHT TO INCORPORATE
 * MODIFICATIONS TO THE SOURCE CODE IN LATER REVISIONS OF IT, AND TO MAKE
 * IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR THE PRODUCTS OR
 * TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * TOSHIBA ELECTRONIC DEVICES & STORAGE CORPORATION SHALL NOT BE LIABLE FOR
 * ANY DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR
 * USE OF THE SOURCE CODE OR ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO,
 * LOST REVENUES, DATA OR PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR
 * CONSEQUENTIAL NATURE, PUNITIVE DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS
 * ARISING OUT OF OR IN CONNECTION WITH THIS AGREEMENT, OR BEING UNUSABLE,
 * EVEN IF ADVISED OF THE POSSIBILITY OR PROBABILITY OF SUCH DAMAGES AND
 * WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON WARRANTY, CONTRACT, TORT,
 * NEGLIGENCE OR OTHERWISE.
 */

package jp.co.toshiba.semicon.sapp01.Function;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;

import jp.co.toshiba.semicon.sapp01.BLE.*;
import jp.co.toshiba.semicon.sapp01.Main.*;
import jp.co.toshiba.semicon.sapp01.*;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentLED extends Fragment {

    public static FragmentLED newInstance(int sectionNumber) {
        FragmentLED fragment = new FragmentLED();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentLED() {
    }

    private Callbacks mCallbacks;
    private Button mBtnLEDOn;
    private Button mBtnLEDOff;
    private int mFrequency;
    private int mDutyCycle;
    private int mRhythmPattern;
    private static final int[] TEMPLATE1 = { 512, 90, 0x0FFFFF };

    private boolean led_flag = false;
    private ImageView img_led;
    private TextView text_touch;


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
        mFrequency = TEMPLATE1[0];
        mDutyCycle = TEMPLATE1[1];
        mRhythmPattern = TEMPLATE1[2];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_led, container, false);

        mBtnLEDOn = (Button)rootView.findViewById(R.id.btnLEDOn);
        mBtnLEDOff = (Button)rootView.findViewById(R.id.btnLEDOff);

        mBtnLEDOn.setOnClickListener(btnLEDOnOffListener);
        mBtnLEDOff.setOnClickListener(btnLEDOnOffListener);

        img_led = (ImageView)rootView.findViewById(R.id.img_led);
        img_led.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ( led_flag ) {
                    mBtnLEDOff.performClick();
                    led_flag = false;
                } else {
                    mBtnLEDOn.performClick();
                    led_flag = true;
                }
                return false;
            }
        });
        text_touch = (TextView)rootView.findViewById(R.id.text_touch);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final BleService service = MainActivity.sBleService;
        if (service != null) {
            if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                mBtnLEDOn.setEnabled(true);
                mBtnLEDOff.setEnabled(true);
                img_led.setEnabled(true);
                text_touch.setVisibility(View.VISIBLE);
            }
            else {
                mBtnLEDOn.setEnabled(false);
                mBtnLEDOff.setEnabled(false);
                img_led.setEnabled(false);
                text_touch.setVisibility(View.INVISIBLE);
            }
        }
        else {
            mBtnLEDOn.setEnabled(false);
            mBtnLEDOff.setEnabled(false);
            img_led.setEnabled(false);
            text_touch.setVisibility(View.INVISIBLE);
        }

        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private final View.OnClickListener btnLEDOnOffListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            BleService service = MainActivity.sBleService;

            if (service != null) {
                BluetoothGattCharacteristic ch =service.getCharacteristicByUuid(SERVICE_SAPPHIRE_ORIGINAL, CHARA_LED_CONFIG);
                if (ch == null) {
                    Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                }
                else {

                    int id = v.getId();
                    switch (id) {
                        case R.id.btnLEDOn: {
//                            byte[] txBuff = new byte[7];
//                            txBuff[0] = (byte) 0x01;
//
//                            txBuff[1] = (byte) ((mFrequency >> 8) & 0x000000FF);
//                            txBuff[2] = (byte) (mFrequency & 0x000000FF);
//
//                            txBuff[3] = (byte) mDutyCycle;
//
//                            txBuff[4] = (byte) ((mRhythmPattern >> 16) & 0x000000FF);
//                            txBuff[5] = (byte) ((mRhythmPattern >> 8) & 0x000000FF);
//                            txBuff[6] = (byte) (mRhythmPattern & 0x000000FF);

                            byte txBuff[] = {0x01};
                            ch.setValue(txBuff);

                            break;
                        }
                        case R.id.btnLEDOff: {
                            byte txBuff[] = {0x00};
                            ch.setValue(txBuff);
                            break;
                        }
                        default:
                            ch = null;
                            break;
                    }

                    mBtnLEDOn.setEnabled(false);
                    mBtnLEDOff.setEnabled(false);
                    img_led.setEnabled(false);
                    text_touch.setVisibility(View.INVISIBLE);
                    service.writeCharacteristic(ch);
                }
            }
            else {
                mCallbacks.cbAppendLog("[FrLED]BLE service Not work");
            }
        }
    });


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrLED]BC Rx: CONNECTED");

                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrLED]BC Rx: DISCONNECTED");

                    mBtnLEDOn.setEnabled(false);
                    mBtnLEDOff.setEnabled(false);
                    img_led.setEnabled(false);
                    text_touch.setVisibility(View.INVISIBLE);
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrLED]BC Rx: DISCONNECTED_LL");

                    mBtnLEDOn.setEnabled(false);
                    mBtnLEDOff.setEnabled(false);
                    img_led.setEnabled(false);
                    text_touch.setVisibility(View.INVISIBLE);
                    break;
                case BLE_SERVICE_DISCOVERED_SOS:
                    mCallbacks.cbAppendLog("[FrLED]BC Rx: SRV_DISCOVERED_SOS");
                    mBtnLEDOn.setEnabled(true);
                    mBtnLEDOff.setEnabled(true);
                    img_led.setEnabled(true);
                    mBtnLEDOff.performClick();
                    text_touch.setVisibility(View.VISIBLE);
                    img_led.setImageResource(R.drawable.led_off);
                    break;
                case BLE_CHAR_W_LED:
                    mCallbacks.cbAppendLog("[FrLED]BC Rx: CHAR_W_LED  " + intent.getStringExtra(EXTRA_DATA));

                    mBtnLEDOn.setEnabled(true);
                    mBtnLEDOff.setEnabled(true);
                    img_led.setEnabled(true);
                    if ( led_flag ) {
                        img_led.setImageResource(R.drawable.led_on);
                    } else {
                        img_led.setImageResource(R.drawable.led_off);
                    }
                    text_touch.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };


    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED_SOS);
        intentFilter.addAction(BLE_CHAR_W_LED);
        return intentFilter;
    }

}

