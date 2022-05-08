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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Date;

import jp.co.toshiba.semicon.sapp01.BLE.BleService;
import jp.co.toshiba.semicon.sapp01.Main.MainActivity;
import jp.co.toshiba.semicon.sapp01.R;
import jp.co.toshiba.semicon.sapp01.adapter.MainSQLiteHelper;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHAR_N_BATTERY_LV;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CONNECTED;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_DESC_W_BATTERY_LV;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_DISCONNECTED;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_DISCONNECTED_LL;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_SERVICE_DISCOVERED_BAS;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.CHARA_BATTERY_LV;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.EXTRA_DATA;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.SERVICE_BATTERY;


public class FragmentFlow extends Fragment {

    public static FragmentFlow newInstance(int sectionNumber) {
        FragmentFlow fragment = new FragmentFlow();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentFlow() {
    }

    private Callbacks mCallbacks;
    private ToggleButton mTglBtnNotify;
    private TextView tvPress;
    private boolean mIsNotifyON;

    private LineChart chartP;

    SQLiteDatabase database;
    MainSQLiteHelper helper;
    Context context;

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


        View rootView = inflater.inflate(R.layout.fragment_flow, container, false);
        context = this.getContext();

        mTglBtnNotify = (ToggleButton)rootView.findViewById(R.id.toggleNotifyFlow);
        tvPress = (TextView)rootView.findViewById(R.id.tvPress);

        mTglBtnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BleService service = MainActivity.sBleService;
                if (service != null) {
                    if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                        BluetoothGattCharacteristic ch = service.getCharacteristicByUuid(SERVICE_BATTERY, CHARA_BATTERY_LV);
                        if (ch == null) {
                            Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                            mTglBtnNotify.setChecked(false);
                        }
                        else {
                            if (((ToggleButton)v).isChecked()) {
                                mCallbacks.cbAppendLog("Enable Battery-Notification");
                                service.enableNotification(ch);
                            }
                            else {
                                mCallbacks.cbAppendLog("Disable Battery-Notification");
                                service.disableNotificationIndication(ch);
                            }
                        }
                    }
                    else {
                        mCallbacks.cbAppendLog("[FrADC]BLE Not connected");
                        mTglBtnNotify.setChecked(false);
                    }
                }
            }
        });

        chartP = (LineChart) rootView.findViewById(R.id.chart1);
        chartP.setDrawGridBackground(false);
        chartP.getDescription().setEnabled(false);
        chartP.setTouchEnabled(false);
        chartP.setDragEnabled(false);
        chartP.setScaleEnabled(false);
        chartP.setPinchZoom(false);
        chartP.setScaleXEnabled(false);

        helper = new MainSQLiteHelper(context);
        database = helper.getWritableDatabase();
        settingChart();

        return rootView;
    }

    public void settingChart(){
        XAxis xAxis = chartP.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);


        YAxis leftAxis = chartP.getAxisLeft();  //y축 설정
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMaximum(100);
        leftAxis.setAxisMinimum(-100);
        leftAxis.setLabelCount(5, true);
        leftAxis.setSpaceTop(0f);

        YAxis rightAxis = chartP.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);

        // this replaces setStartAtZero(true)
        Legend l = chartP.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
    }

    @Override
    public void onResume() {
        super.onResume();

        if ( !mIsNotifyON ) {
            mTglBtnNotify.setChecked(mIsNotifyON);

            final BleService service = MainActivity.sBleService;
            if (service != null) {
                if (service.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                    mTglBtnNotify.setEnabled(true);
                }
                else {
                    mTglBtnNotify.setEnabled(false);
                }
            }

            getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
        } else {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrADC]BC Rx: CONNECTED");

                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrADC]BC Rx: DISCONNECTED");

                    mIsNotifyON = false;
                    mTglBtnNotify.setChecked(false);
                    mTglBtnNotify.setEnabled(false);
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrADC]BC Rx: DISCONNECTED_LL");

                    mIsNotifyON = false;
                    mTglBtnNotify.setChecked(false);
                    mTglBtnNotify.setEnabled(false);
                    break;
                case BLE_SERVICE_DISCOVERED_BAS:
                    mCallbacks.cbAppendLog("[FrADC]BC Rx: SRV_DISCOVERED_BAS");
                    mTglBtnNotify.setEnabled(true);
                    break;
                case BLE_DESC_W_BATTERY_LV:

                    String hexString = intent.getStringExtra(EXTRA_DATA);
                    byte[] bytes = BleService.hexStringToBinary(hexString);
                    if ((bytes[0] == (byte) 0x01) && (bytes[1] == (byte) 0x00)) {
                        mIsNotifyON = true;
                        mCallbacks.cbAppendLog("[FrADC]BC Rx: DESC_W_BATTERY_LV On");
                    } else {
                        mIsNotifyON = false;
                        mCallbacks.cbAppendLog("[FrADC]BC Rx: DESC_W_BATTERY_LV Off");
                    }
                    mTglBtnNotify.setChecked(mIsNotifyON);
                    break;
                case BLE_CHAR_N_BATTERY_LV:

                    String hexData = intent.getStringExtra(EXTRA_DATA);
                    Log.e("!!!", "==============   hexData = "+hexData);

                    if ( mTglBtnNotify.isChecked() ) {
                        int raw_int = 0;
                        float Pressure_INH20 = 0;
                        String press_str;
                        int [] hexdata_int = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                        hexdata_int[0] = Integer.parseInt(hexData.substring(0, 2), 16);
                        hexdata_int[1] = Integer.parseInt(hexData.substring(2, 4), 16);
                        hexdata_int[2] = Integer.parseInt(hexData.substring(4, 6), 16);
                        hexdata_int[3] = Integer.parseInt(hexData.substring(6, 8), 16);
                        hexdata_int[4] = Integer.parseInt(hexData.substring(8, 10), 16);
                        hexdata_int[5] = Integer.parseInt(hexData.substring(10, 12), 16);
                        hexdata_int[6] = Integer.parseInt(hexData.substring(12, 14), 16);
                        hexdata_int[7] = Integer.parseInt(hexData.substring(14, 16), 16);
                        hexdata_int[8] = Integer.parseInt(hexData.substring(16, 18), 16);
                        hexdata_int[9] = Integer.parseInt(hexData.substring(18, hexData.length()), 16);
                        String sensor_flag = String.valueOf((char)hexdata_int[7])+String.valueOf((char)hexdata_int[8])+String.valueOf((char)hexdata_int[9]);
                        Log.e("!!!", "sensor_flag = "+sensor_flag);

                        switch (sensor_flag) {
                            case "ADS":
                                //** ADS Sensor START

                                raw_int = (hexdata_int[0] * 256) + hexdata_int[1];
                                if (raw_int > 32767) {
                                    raw_int -= 65535;
                                }

                                Pressure_INH20 = raw_int;
                                // ADS Sensor ENND**/
                                break;
                        }

                        press_str = String.format("%f", Pressure_INH20);
                        tvPress.setText(press_str);

                        if(Pressure_INH20 > 100 ){
                            helper.checkInsert(database,  "100" , "0", String.format("%tT", new Date()));
                        }else if(Pressure_INH20 < -100) {
                            helper.checkInsert(database,  "-100" , "0", String.format("%tT", new Date()));
                        }else{
                            helper.checkInsert(database,  press_str , "0", String.format("%tT", new Date()));
                        }

                        makeList(database);                    }
                    break;
            }
        }
    };

    public void makeList(SQLiteDatabase db) {

        ArrayList<Entry> entriesPress = new ArrayList<>();
        ArrayList<ILineDataSet> dataSetPress = new ArrayList<ILineDataSet>();

        Cursor ch = db.rawQuery("select * from table_check_list  order by etc  desc limit 10;", null);
        int i = 0 ;
        //TODO ch.getString(1) ==== Press
        //TODO ch.getString(2) ==== Temp

        while(ch.moveToNext()) {
            float press = Float.parseFloat(ch.getString(1));
            mCallbacks.cbAppendLog("ch.getString(3)  === " + ch.getString(3));
            entriesPress.add(new Entry(i ,press));
            i++;
        }

        LineDataSet datasetP = new LineDataSet(entriesPress, getString(R.string.flow));
        datasetP.setCircleColor(Color.parseColor("#3186c6"));
        datasetP.setColor(Color.parseColor("#3186c6"));

        dataSetPress.add(datasetP);

        LineData dataP = new LineData(dataSetPress);
        chartP.setData(dataP);
        chartP.invalidate();
    }


    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(BLE_SERVICE_DISCOVERED_BAS);
        intentFilter.addAction(BLE_DESC_W_BATTERY_LV);
        intentFilter.addAction(BLE_CHAR_N_BATTERY_LV);
        return intentFilter;
    }





}

