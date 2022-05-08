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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.*;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.*;

import jp.co.toshiba.semicon.sapp01.BLE.*;
import jp.co.toshiba.semicon.sapp01.Main.*;
import jp.co.toshiba.semicon.sapp01.*;
import jp.co.toshiba.semicon.sapp01.adapter.MainSQLiteHelper;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;


public class FragmentADC extends Fragment {

    public static FragmentADC newInstance(int sectionNumber) {
        FragmentADC fragment = new FragmentADC();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentADC() {
    }

    private Callbacks mCallbacks;
    private ToggleButton mTglBtnNotify;
    private TextView tvTemp;
    private TextView tvPress;
    private boolean mIsNotifyON;
    private ListView listView;

    private LineChart chartP;
    private LineChart chartT;

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


        View rootView = inflater.inflate(R.layout.fragment_adc, container, false);
        context = this.getContext();

        mTglBtnNotify = (ToggleButton)rootView.findViewById(R.id.toggleNotifyTemp);
        tvTemp = (TextView)rootView.findViewById(R.id.tvTemp);
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
        chartT = (LineChart) rootView.findViewById(R.id.chart2);

        chartP.setDrawGridBackground(false);
        chartP.getDescription().setEnabled(false);
        chartP.setTouchEnabled(false);
        chartP.setDragEnabled(false);
        chartP.setScaleEnabled(false);
        chartP.setPinchZoom(false);
        chartP.setScaleXEnabled(false);


        chartT.setDrawGridBackground(false);
        chartT.getDescription().setEnabled(false);
        chartT.setTouchEnabled(false);
        chartT.setDragEnabled(false);
        chartT.setScaleEnabled(false);
        chartT.setPinchZoom(false);
        chartT.setScaleXEnabled(false);



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
        leftAxis.setAxisMaximum(2);
        leftAxis.setAxisMinimum(-2);
        leftAxis.setLabelCount(5, true);
        leftAxis.setSpaceTop(0f);

        //leftAxis.setAxisMaximum(100);
//        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

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



        XAxis xAxisT = chartT.getXAxis();
        xAxisT.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisT.setDrawGridLines(false);
        xAxisT.setGranularity(1f); // only intervals of 1 day
        xAxisT.setLabelCount(7);


        YAxis leftAxisT = chartT.getAxisLeft();  //y축 설정
        leftAxisT.setLabelCount(3, false);
        leftAxisT.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxisT.setAxisMaximum(40);
        leftAxisT.setSpaceTop(15f);

        //leftAxis.setAxisMaximum(100);
        leftAxisT.setAxisMinimum(-10); // this replaces setStartAtZero(true)

        YAxis rightAxisT = chartT.getAxisRight();
        rightAxisT .setDrawGridLines(false);
        rightAxisT .setDrawAxisLine(false);
        rightAxisT .setDrawLabels(false);

        // this replaces setStartAtZero(true)

        Legend lT = chartT.getLegend();
        lT.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        lT.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        lT.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        lT.setDrawInside(false);
        lT.setForm(Legend.LegendForm.SQUARE);
        lT.setFormSize(9f);
        lT.setTextSize(11f);
        lT.setXEntrySpace(4f);
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

//        if (mIsNotifyON) {
//            BleService service = MainActivity.sBleService;
//            if (service != null) {
//                BluetoothGattCharacteristic ch = service.getCharacteristicByUuid(SERVICE_BATTERY, CHARA_BATTERY_LV);
//                mCallbacks.cbAppendLog("Disable Battery-Notification");
//                try {
//                    service.disableNotificationIndication(ch);
//                } catch (NullPointerException ignored) {
//                }
//            }
//            mTglBtnNotify.setEnabled(false);
//            mIsNotifyON = false;
//        }
//
//        getActivity().unregisterReceiver(mBroadcastReceiver);
//        helper.deleteCheck(database);
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
                        double FULL_SCALE_REF = 16777216;
                        int raw_int = 0;
                        double raw_t = 0;
                        float Pressure_INH20 = 0, temp=0;
                        String temp_str, press_str;
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
                            case "DLV":
                                //** DLVR Sensor START
                                raw_int = ((hexdata_int[0] & 0x3f) << 8) | hexdata_int[1];
                                Log.e("!!!", "raw_int = "+raw_int);
//                            Pressure_INH20 = (float) (((raw_int-8192.00)/16384.00)*2.00);
                                Pressure_INH20 = (float) (((raw_int-8192.00)/16384.00)*2.00 *4);
                                Log.e("!!!", "Pressure_INH20 = "+Pressure_INH20);

                                raw_t = (double) ((hexdata_int[2] << 3) | ((hexdata_int[3] & 0b11100000) >> 5));
                                temp = (float) (raw_t * (200.0 / 2047.0) - 50.0);
                                // DLVR Sensor ENND**/
                                break;
                            case "DLH":
                                //** DLVR Sensor START
                                raw_int = hexdata_int[1] << 16 | hexdata_int[2] <<8 | hexdata_int[3];
//                            Pressure_INH20 = (float) (1.25 *(((raw_int-(0.5*FULL_SCALE_REF))/FULL_SCALE_REF)*2.00));
                                Pressure_INH20 = (float) (1.25 *(((raw_int-(0.5*FULL_SCALE_REF))/FULL_SCALE_REF)*2.00));
                                Log.e("!!!", "Pressure_INH20 = "+Pressure_INH20);

                                raw_t = (double) (hexdata_int[4] << 16 | hexdata_int[5] <<8 | hexdata_int[6]);
                                temp = (float) ((raw_t * 125.0 / FULL_SCALE_REF) - 40.0);
                                // DLVR Sensor ENND**/
                                break;
                            case "DLC":
                                //** DLVR Sensor START
                                raw_int = hexdata_int[1] << 16 | hexdata_int[2] <<8 | hexdata_int[3];
                                Pressure_INH20 = (float) (1.25 *(((raw_int-(0.1*FULL_SCALE_REF))/FULL_SCALE_REF)*2.00));
                                Log.e("!!!", "Pressure_INH20 = "+Pressure_INH20);

                                raw_t = (double) (hexdata_int[4] << 16 | hexdata_int[5] <<8 | hexdata_int[6]);
                                temp = (float) ((raw_t * 150.0 / FULL_SCALE_REF) - 40.0);
                                // DLVR Sensor ENND**/
                                break;
                        }

                        press_str = String.format("%.5f", Pressure_INH20);
                        temp_str = String.format("%.1f", temp);
                        tvPress.setText(press_str);
                        tvTemp.setText(temp_str);

                        if(Pressure_INH20 > 2 ){
                            helper.checkInsert(database,  "2" , temp_str, String.format("%tT", new Date()));
                        }else if(Pressure_INH20 < -2) {
                            helper.checkInsert(database,  "-2" , temp_str, String.format("%tT", new Date()));
                        }else{
                            helper.checkInsert(database,  press_str , temp_str, String.format("%tT", new Date()));
                        }

                        makeList(database);                    }
                    break;
            }
        }
    };

    public void makeList(SQLiteDatabase db) {

        ArrayList<Entry> entriesPress = new ArrayList<>();
        ArrayList<Entry> entriesTemp = new ArrayList<>();
        ArrayList<ILineDataSet> dataSetTemp = new ArrayList<ILineDataSet>();
        ArrayList<ILineDataSet> dataSetPress = new ArrayList<ILineDataSet>();

        Cursor ch = db.rawQuery("select * from table_check_list  order by etc  desc limit 10;", null);
        int i = 0 ;
        //TODO ch.getString(1) ==== Press
        //TODO ch.getString(2) ==== Temp

        while(ch.moveToNext()) {

            float press = Float.parseFloat(ch.getString(1));
            float temp = Float.parseFloat(ch.getString(2));

                                mCallbacks.cbAppendLog("ch.getString(3)  === " + ch.getString(3));

            entriesPress.add(new Entry(i ,press));
            entriesTemp.add(new Entry(i ,temp));

            i++;
        }

//        entriesPress.add(new Entry(0 ,7));
//        entriesPress.add(new Entry(1 ,12));
//        entriesPress.add(new Entry(2 ,-5.666f));
//        entriesPress.add(new Entry(3 ,32));
//        entriesPress.add(new Entry(4 ,25));
//        entriesPress.add(new Entry(5 ,30));
//        entriesPress.add(new Entry(6 ,-42));
//        entriesPress.add(new Entry(7 ,-14));
//        entriesPress.add(new Entry(8 ,5));
//        entriesPress.add(new Entry(9 ,10));
//        entriesTemp.add(new Entry(0 , 25.6f));
//        entriesTemp.add(new Entry(1 , 26));
//        entriesTemp.add(new Entry(2 , 28));
//        entriesTemp.add(new Entry(3 , 20));
//        entriesTemp.add(new Entry(4 , 29));
//        entriesTemp.add(new Entry(5 , 25));
//        entriesTemp.add(new Entry(6 , 24));
//        entriesTemp.add(new Entry(7 , 22));
//        entriesTemp.add(new Entry(8 , 20));
//        entriesTemp.add(new Entry(9 , 19));

        LineDataSet datasetP = new LineDataSet(entriesPress, getString(R.string.pressure));
        LineDataSet datasetT = new LineDataSet(entriesTemp, getString(R.string.temp));

        datasetP.setCircleColor(Color.parseColor("#2F9D27"));
        datasetT.setCircleColor(Color.parseColor("#FF0000"));
        datasetT.setColor(Color.parseColor("#FF0000"));
        datasetP.setColor(Color.parseColor("#2F9D27"));

        dataSetTemp.add(datasetT);
        dataSetPress.add(datasetP);



        LineData dataT = new LineData(dataSetTemp);
        LineData dataP = new LineData(dataSetPress);
        chartP.setData(dataP);
        chartP.invalidate();

        chartT.setData(dataT);
        chartT.invalidate();

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

