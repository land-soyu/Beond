/**
 * COPYRIGHT (C) 2017
 * TOSHIBA CORPORATION STORAGE & ELECTRONIC DEVICES SOLUTIONS COMPANY
 * ALL RIGHTS RESERVED
 * <p>
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". TOSHIBA
 * CORPORATION MAKES NO OTHER WARRANTY OF ANY KIND, WHETHER EXPRESS, IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. TOSHIBA CORPORATION
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 * <p>
 * TOSHIBA CORPORATION SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 */

package jp.co.toshiba.semicon.sapp01.Main;

import android.*;
import android.annotation.*;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.Configuration;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.*;
import android.support.v4.content.*;
import android.support.v4.view.*;
import android.support.v7.app.*;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import jp.co.toshiba.semicon.sapp01.BLE.*;
import jp.co.toshiba.semicon.sapp01.Function.*;
import jp.co.toshiba.semicon.sapp01.R;


public class MainActivity extends AppCompatActivity
        implements FragmentBLE.Callbacks,
        FragmentDevice.Callbacks,
        FragmentADC.Callbacks,
        FragmentFlow.Callbacks,
        FragmentLED.Callbacks,
        FragmentSwitch.Callbacks,
        FragmentConsole.Callbacks,
        FragmentOTA.Callbacks {

    public static final String ARG_SECTION_NUM = "section_number";
    public static final String LOG_TAG = "SAPP-01";
    private static final int REQUEST_ENABLE_BT = 1048;

    public static BleService sBleService;
    public static ProgressBar sProgressBar;
    private static ProgressDialog sWaitDialog;
    private static ProgressDialog sProgressDialog;
    private boolean service_bind = false;
    private boolean main_start = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(jp.co.toshiba.semicon.sapp01.R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // BLE service
        Intent serviceIntent = new Intent(this, BleService.class);
        bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE);
        // Progress Bar
        sProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Dialog
        sProgressDialog = new ProgressDialog(this);
        sProgressDialog.setTitle("Toshiba BLE OTA");
        sProgressDialog.setMessage("now processing ...");
        sProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        sProgressDialog.setMax(100);
        sProgressDialog.setCancelable(false);

        // Dialog
        sWaitDialog = new ProgressDialog(this);
        sWaitDialog.setMessage("please wait");
        sWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        sWaitDialog.setCancelable(false);

        // Authority
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         unbindService(mServiceConnection);

        sBleService = null;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return FragmentBLE.newInstance(position + 1);
                case 1:
                    return FragmentADC.newInstance(position + 1);
                case 2:
                    return FragmentLED.newInstance(position + 1);
                case 3:
                    return FragmentFlow.newInstance(position + 1);
//                case 3:
//                    return FragmentDevice.newInstance(position + 1);
//                case 4:
//                    return FragmentConsole.newInstance(position + 1);
//                case 5:
//                    return FragmentSwitch.newInstance(position + 1);
//                case 6:
//                    return FragmentOTA.newInstance(position + 1);
                default:
                    break;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;            // total pages.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "BLE";
                case 1:
                    return "PRESSURE";
                case 2:
                    return "LED";
                case 3:
                    return "Flow";
//                case 3:
//                    return "Device Info";
//                case 4:
//                    return "Console";
//                case 5:
//                    return "Switch";
//                case 6:
//                    return "OTA";
            }
            return null;
        }
    }

    @Override
    public void cbAppendLog(String text) {
        Log.d(LOG_TAG, text);
    }


    public static void startProgressDialog() {
        sProgressDialog.show();
        sProgressDialog.setProgress(1);
        //noinspection ConstantConditions
        sProgressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    public static void setProgressDialog(int progress) {
        sProgressDialog.setProgress(progress);
    }


    public static void stopProgressDialog() {
        //noinspection ConstantConditions
        sProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sProgressDialog.dismiss();
    }


    public static void startWaitDialog() {
        sWaitDialog.show();
    }


    public static void stopWaitDialog() {
        sWaitDialog.dismiss();
    }

    public static void threadSleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentname, IBinder service) {
            sBleService = ((BleService.LocalBinder) service).getService();

            if (!sBleService.init()) {
                finish();
            }

            BluetoothAdapter btAdapter = sBleService.getBtAdapter();
            if (btAdapter != null) {
                if (!btAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentname) {
            sBleService = null;
        }
    };


    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(String permission) {
        // If the authority is not allowed, to request.
        int permissionCheck = ContextCompat.checkSelfPermission(getBaseContext(), permission);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, 1);
        }
    }

}
