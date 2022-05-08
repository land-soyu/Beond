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

import android.annotation.*;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import jp.co.toshiba.semicon.sapp01.BLE.*;
import jp.co.toshiba.semicon.sapp01.Main.*;
import jp.co.toshiba.semicon.sapp01.*;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentConsole extends Fragment {

    public static FragmentConsole newInstance(int sectionNumber) {
        FragmentConsole fragment = new FragmentConsole();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentConsole() {
    }

    private Callbacks mCallbacks;
    private Button mBtnSendMsg;
    private TextView mTxtVwSendMsgResult;
    private EditText mEdTxtSendMsg;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioBtnTmp1;

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
        } else {
            mCallbacks = ((Callbacks) activity);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException("activity does not implemented Callbacks");
        } else {
            mCallbacks = ((Callbacks) context);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_console, container, false);

        mBtnSendMsg = (Button) rootView.findViewById(R.id.btn_SendMsg);
        mTxtVwSendMsgResult = (TextView) rootView.findViewById(R.id.txtViewMsgResult);
        mEdTxtSendMsg = (EditText) rootView.findViewById(R.id.editTxt_Message);
        mRadioGroup = (RadioGroup) rootView.findViewById(R.id.rdGroup_Msg);
        mRadioBtnTmp1 = (RadioButton) rootView.findViewById(R.id.rdBtn_ConsoleT1);

        mEdTxtSendMsg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRadioGroup.check(R.id.rdBtn_Msg_Edit);
                return false;
            }
        });

        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mEdTxtSendMsg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        mEdTxtSendMsg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                } else {
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        mBtnSendMsg.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                BleService service = MainActivity.sBleService;
                if (service != null) {
                    BluetoothGattCharacteristic ch = service.getCharacteristicByUuid(SERVICE_SAPPHIRE_ORIGINAL, CHARA_CONSOLE);
                    if (ch == null) {
                        Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                    } else {
                        String message;
                        int checkedId = mRadioGroup.getCheckedRadioButtonId();

                        if (checkedId == -1) {
                            return;
                        } else if (checkedId == R.id.rdBtn_Msg_Edit) {

                            try {
                                message = mEdTxtSendMsg.getText().toString();
                            } catch (NullPointerException e) {
                                Toast.makeText(getActivity(), "Could not get a string.", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (message.matches("^.*[^\\p{ASCII}].*")) {
                                Toast.makeText(getActivity(), "Maybe unsupported character is used.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else {
                            ViewGroup rootView = (ViewGroup) getActivity().findViewById(android.R.id.content);
                            message = (String) (((RadioButton) rootView.findViewById(checkedId)).getText());
                        }

                        mBtnSendMsg.setEnabled(false);

                        ch.setValue(message);
                        service.writeCharacteristic(ch);
                        mTxtVwSendMsgResult.setText("sending ...");
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mRadioBtnTmp1.setChecked(true);

        final BleService service = MainActivity.sBleService;
        if (service != null) {
            if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                mBtnSendMsg.setEnabled(true);
            } else {
                mBtnSendMsg.setEnabled(false);
            }
        } else {
            mBtnSendMsg.setEnabled(false);
        }
        mTxtVwSendMsgResult.setText(getString(R.string.str_defaultValue));

        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        mEdTxtSendMsg.getEditableText().clear();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrCons]BC Rx: CONNECTED");

                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrCons]BC Rx: DISCONNECTED");

                    mBtnSendMsg.setEnabled(false);
                    mTxtVwSendMsgResult.setText(getString(R.string.str_defaultValue));
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrCons]BC Rx: DISCONNECTED_LL");

                    mBtnSendMsg.setEnabled(false);
                    mTxtVwSendMsgResult.setText(getString(R.string.str_defaultValue));
                    break;
                case BLE_SERVICE_DISCOVERED_SOS:
                    mCallbacks.cbAppendLog("[FrCons]BC Rx: SRV_DISCOVERED_SOS");
                    mBtnSendMsg.setEnabled(true);
                    break;
                case BLE_CHAR_W_CONSOLE:
                    String msgAck = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwSendMsgResult.setText("ACK: " + msgAck);
                    mCallbacks.cbAppendLog("[FrCons]BC Rx: CHAR_W_MESSAGE  " + msgAck);

                    mBtnSendMsg.setEnabled(true);
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
        intentFilter.addAction(BLE_CHAR_W_CONSOLE);
        return intentFilter;
    }

}

