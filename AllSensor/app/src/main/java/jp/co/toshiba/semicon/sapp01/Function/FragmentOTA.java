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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;

import jp.co.toshiba.semicon.sapp01.BLE.BleService;
import jp.co.toshiba.semicon.sapp01.Main.MainActivity;
import jp.co.toshiba.semicon.sapp01.R;

import static android.content.ContentValues.TAG;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_EXTERNAL_MEMORY_READ1;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_EXTERNAL_MEMORY_READ2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_EXTERNAL_MEMORY_WRITE;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLAG_CHANGE;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLAG_READ1;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLAG_READ2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLASH_CLOSE;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLASH_ERASE;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHARA_FLASH_OPEN;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CHAR_R_DI_HW_REV;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_CONNECTED;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_DISCONNECTED;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_DISCONNECTED_LL;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.BLE_SERVICE_DISCOVERED_EMM;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.CHARA_EXTERNAL_MEMORY_CHECKSUM2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.CHARA_EXTERNAL_MEMORY_READ2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.CHARA_FLAG_READ2;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.CHARA_HARDWARE_REV;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.EXTRA_DATA;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.SERVICE_DEVICE_INFO;
import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.SERVICE_EXTERNAL_MEMORY_MAINTENANCE;


public class FragmentOTA extends Fragment {

    public static FragmentOTA newInstance(int sectionNumber) {
        FragmentOTA fragment = new FragmentOTA();
        Bundle args = new Bundle();
        args.putInt(MainActivity.ARG_SECTION_NUM, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentOTA() {
    }

    static final String OTA_ERROR_MSG = "jp.co.toshiba.semicon.sapp01.OTA_ERROR_MSG";
    static final String OTA_ERROR_EXIT = "jp.co.toshiba.semicon.sapp01.OTA_ERROR_EXIT";
    static final String OTA_EXIT = "jp.co.toshiba.semicon.sapp01.OTA_EXIT";

    private static final byte OTA_TARGET_DEVICE_TC35678 = 1;
    private static final byte OTA_TARGET_DEVICE_TC35679 = 2;
    private static final byte OTA_TARGET_DEVICE_TC3567A = 3;
    private static final byte OTA_TARGET_DEVICE_TC3567C = 5;
    private static final byte OTA_TARGET_DEVICE_TC3567D = 6;

    private static final byte OTA_TARGET_MEMORY_FLASH = 1;
    private static final byte OTA_TARGET_MEMORY_E2PROM = 2;

    private static final int CHOOSE_FILE_CODE = 1048;
    private static final long STAND_TIME = 3000;
    private static long sClickTime;

    private byte mOtaDevice;
    private byte mOtaMemory;

    private Context mContext;
    private Callbacks mCallbacks;
    private BleService mService;
    private String mStrSelectFile = "";

    private TextView mTxtVwReadHardRev;
    private TextView mTxtVwSelectFile;
    private TextView mTxtVwOtaConnectStatus;
    private TextView mTxtVwOtaResult;
    private Button mBtnOtaStart;
    private Button mBtnSetFile;
    private Button mBtnReadHwRev;
    private Button mBtnDisconnect;
    private ThreadFlashOta mThreadFlashOta;
    private ThreadE2promOta mThreadE2promOta;

    private File[] fileList = null;
    private String[] fileNameList = null;

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
            mContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException("activity does not implemented Callbacks");
        } else {
            mCallbacks = ((Callbacks) context);
            mContext = context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_ota, container, false);

        mBtnReadHwRev = (Button) rootView.findViewById(R.id.btn_ReadHwRevOta);
        mBtnSetFile = (Button) rootView.findViewById(R.id.btn_SetFile);
        mBtnOtaStart = (Button) rootView.findViewById(R.id.btn_Start_Flash);
        mBtnDisconnect = (Button) rootView.findViewById(R.id.btn_DisconnectOta);
        mBtnSetFile.setEnabled(false);
        mBtnOtaStart.setEnabled(false);
        mBtnDisconnect.setEnabled(false);

        mTxtVwReadHardRev = (TextView) rootView.findViewById(R.id.txtVw_ReadHwRevOta);
        mTxtVwSelectFile = (TextView) rootView.findViewById(R.id.txtVw_SelectFile);
        mTxtVwOtaResult = (TextView) rootView.findViewById(R.id.txtVw_otaResult_Flash);
        mTxtVwOtaConnectStatus = (TextView) rootView.findViewById(R.id.txtVw_otaStatus);

        // Get file list
        File externalStorage = Environment.getExternalStorageDirectory();
        String filePath = externalStorage.getAbsolutePath() + "/Download/";
        fileList = new File(filePath).listFiles();
        fileNameList = new String[fileList.length];
        for(int i = 0; i < fileList.length; i++){
            fileNameList[i] = fileList[i].getName();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        init();
        getActivity().registerReceiver(mBroadcastReceiver, makeIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    public void onStop(){
        super.onStop();
        killOtaThread();
    }


    private void setViewDisconnected() {

        mTxtVwReadHardRev.setText(getString(R.string.str_defaultValue));
        mStrSelectFile = getString(R.string.str_SetFileComment);
        mTxtVwSelectFile.setText(mStrSelectFile);
        mTxtVwOtaResult.setText(getString(R.string.str_defaultValue));
        mTxtVwOtaConnectStatus.setText(R.string.str_Disconnected);
        mBtnReadHwRev.setEnabled(false);
        mBtnSetFile.setEnabled(false);
        mBtnOtaStart.setEnabled(false);
    }

    private void init() {

        mService = MainActivity.sBleService;
        if (mService != null) {
            if (mService.getStatus() == BluetoothProfile.STATE_CONNECTED) {
                mBtnReadHwRev.setEnabled(true);
                mTxtVwOtaConnectStatus.setText(R.string.str_Connected);
            } else {
                mBtnReadHwRev.setEnabled(false);
            }
        } else {
            mBtnReadHwRev.setEnabled(false);
        }
        mBtnReadHwRev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BluetoothGattCharacteristic chara;
                chara = mService.getCharacteristicByUuid(SERVICE_DEVICE_INFO, CHARA_HARDWARE_REV);

                if (chara == null) {
                    Toast.makeText(getActivity(), R.string.str_CharaNotFound, Toast.LENGTH_SHORT).show();
                } else {
                    mCallbacks.cbAppendLog("Read characteristic request");
                    mService.readCharacteristic(chara);
                }
            }
        });


        mBtnSetFile.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {

            //    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //    intent.setType("*/*");

            //    try {
            //        startActivityForResult(intent, CHOOSE_FILE_CODE);
            //    } catch (ActivityNotFoundException e) {
            //        Toast.makeText(getActivity(), "Please install an application as file manager.", Toast.LENGTH_LONG).show();
            //    }
            //}
            @Override
            public void onClick(View v) {
                // Single Choice Dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle("Select new FW hex file")
                        .setItems(fileNameList, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Get File Information
                                File file = fileList[which];
                                try {
                                    try {
                                        mStrSelectFile = java.net.URLDecoder.decode(file.getPath(), "utf-8");
                                    } catch (NullPointerException e) {
                                        Toast.makeText(getActivity(), "File Path Error", Toast.LENGTH_SHORT).show();
                                        mStrSelectFile = getString(R.string.str_SetFileComment);
                                    }

                                    mCallbacks.cbAppendLog("[FILE] " + mStrSelectFile);

                                    if (checkFilenamePath(mStrSelectFile)) {
                                        mBtnOtaStart.setEnabled(true);
                                    } else {
                                        mCallbacks.cbAppendLog(getString(R.string.str_InvalidFilePath));
                                        Toast.makeText(getActivity(), R.string.str_InvalidFilePath, Toast.LENGTH_SHORT).show();

                                        // Return to default
                                        mStrSelectFile = getString(R.string.str_SetFileComment);
                                    }

                                    mTxtVwSelectFile.setText(mStrSelectFile);
                                } catch (UnsupportedEncodingException e) {
                                    mCallbacks.cbAppendLog("UnsupportedEncodingException");
                                }
                            }
                        }).show();

            }
        });


        mBtnOtaStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!oneClickEvent()) {
                    return;
                }

                if (mStrSelectFile.equals(getString(R.string.str_SetFileComment))) {
                    Toast.makeText(getActivity(), R.string.str_SetFileComment, Toast.LENGTH_SHORT).show();
                    return;
                }

                startOtaThread();
                MainActivity.startProgressDialog();
            }
        });

        mBtnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.cbAppendLog("disconnect request");

                if (MainActivity.sBleService.disconnectReq()) {
                    MainActivity.startWaitDialog();
                } else {
                    mCallbacks.cbAppendLog("disconnect request failed");
                }
            }
        });
    }

    private static boolean oneClickEvent() {
        long time = System.currentTimeMillis();
        if (time - sClickTime < STAND_TIME) {
            return false;
        }
        sClickTime = time;
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getFilePath(Uri uri) {
        Cursor cursor = null;
        String path = "";
        String id = DocumentsContract.getDocumentId(uri);
        String[] split = id.split(":");
        String[] projection = {MediaStore.Files.FileColumns.DATA};

        if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {// ExternalStorageProvider
            String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                path = Environment.getExternalStorageDirectory() + "/" + split[1];
            } else {
                path = "/storage/" + type + "/" + split[1];
            }
        } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {// DownloadsProvider
            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            try {
                cursor = mContext.getContentResolver().query(contentUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {// MediaProvider
            Uri contentUri = MediaStore.Files.getContentUri("external");

            try {
                cursor = mContext.getContentResolver().query(contentUri, projection, "_id=?", new String[]{split[1]}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }

        return path;
    }


    private boolean checkFilenamePath(String fullPath) {

        if (fullPath.contains(":")) {
            mCallbacks.cbAppendLog("path include COLON");
            return false;
        }

        if (fullPath.endsWith(".hex")) {
            return true;
        } else if (fullPath.endsWith(".HEX")) {
            return true;
        } else {
            mCallbacks.cbAppendLog("filename is not Intel HEX");
            return false;
        }

    }

    private void startOtaThread(){

        if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
            mThreadFlashOta = new ThreadFlashOta(getActivity().getApplicationContext(), mStrSelectFile);
        } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
            mThreadE2promOta = new ThreadE2promOta(getActivity().getApplicationContext(), mStrSelectFile);
        }

        switch (mOtaDevice)
        {
            case OTA_TARGET_DEVICE_TC35678:
                mThreadFlashOta.setRamAddress(0x80C000, 0x814BFF, 0x824000);
                mThreadFlashOta.setFlashHeaderAddress(0x10000, 0x20000);
                mThreadFlashOta.setFlashUserApp0Address(0x11000, 0x21000, 0x0C000);
                mThreadFlashOta.setFlashUserApp1Address(0x1D000, 0x2D000, 0x02000);
                break;
            case OTA_TARGET_DEVICE_TC35679:
                mThreadE2promOta.setRamAddress(0x80C000, 0x814BFF, 0x824000);
                break;
            case OTA_TARGET_DEVICE_TC3567A:
                mThreadFlashOta.setRamAddress(0x800000, 0x827FFF, 0x828000);
                mThreadFlashOta.setFlashHeaderAddress(0x04000, 0x05000);
                mThreadFlashOta.setFlashUserApp0Address(0x06000, 0x12000, 0x0C000);
                mThreadFlashOta.setFlashUserApp1Address(0x1E000, 0x1F000, 0x01000);
                break;
            case OTA_TARGET_DEVICE_TC3567C:
                mThreadFlashOta.setRamAddress(0x813000, 0x81FC5B, 0x81FC5C);
                mThreadFlashOta.setFlashHeaderAddress(0x04000, 0x05000);
                mThreadFlashOta.setFlashUserApp0Address(0x06000, 0x12000, 0x0C000);
                mThreadFlashOta.setFlashUserApp1Address(0x1E000, 0x1F000, 0x01000);
                break;
            case OTA_TARGET_DEVICE_TC3567D:
                mThreadE2promOta.setRamAddress(0x813000, 0x81FC5B, 0x81FC5C);
                break;
            default:
                break;
        }

        if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
            mThreadFlashOta.start();
        } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
            mThreadE2promOta.start();
        }

    }

    private void killOtaThread(){
        if ((mOtaMemory == OTA_TARGET_MEMORY_FLASH) && mThreadFlashOta != null) {
            mThreadFlashOta.setState(ThreadFlashOta.ST_ERROR_EXIT);
        } else if ((mOtaMemory == OTA_TARGET_MEMORY_E2PROM) && mThreadE2promOta != null) {
            mThreadE2promOta.setState(ThreadE2promOta.ST_ERROR_EXIT);
        }
    }

    private boolean setHardwareVer(String version) {
        boolean ret = true;

        if (version.contains("TC35678")) {
            mOtaMemory = OTA_TARGET_MEMORY_FLASH;
            mOtaDevice = OTA_TARGET_DEVICE_TC35678;
        } else if (version.contains("TC35679")) {
            mOtaMemory = OTA_TARGET_MEMORY_E2PROM;
            mOtaDevice = OTA_TARGET_DEVICE_TC35679;
        } else if (version.contains("TC3567A")) {
            mOtaMemory = OTA_TARGET_MEMORY_FLASH;
            mOtaDevice = OTA_TARGET_DEVICE_TC3567A;
        } else if (version.contains("TC3567C")) {
            mOtaMemory = OTA_TARGET_MEMORY_FLASH;
            mOtaDevice = OTA_TARGET_DEVICE_TC3567C;
        } else if (version.contains("TC3567D")) {
            mOtaMemory = OTA_TARGET_MEMORY_E2PROM;
            mOtaDevice = OTA_TARGET_DEVICE_TC3567D;
        } else {
            mOtaMemory = 0;
            mOtaDevice = 0;
            ret = false;
        }

        return ret;
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            byte[] data;
            BluetoothGattCharacteristic chara;
            switch (action) {
                case BLE_CONNECTED:
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: CONNECTED");
                    MainActivity.stopWaitDialog();
                    break;
                case BLE_DISCONNECTED:
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: DISCONNECTED");
                    MainActivity.stopProgressDialog();
                    MainActivity.stopWaitDialog();
                    setViewDisconnected();
                    break;
                case BLE_DISCONNECTED_LL:
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: DISCONNECTED_LL");
                    MainActivity.stopProgressDialog();
                    MainActivity.stopWaitDialog();
                    MainActivity.stopProgressDialog();
                    MainActivity.stopWaitDialog();
                    Toast.makeText(getActivity(), R.string.str_notify_disconnected, Toast.LENGTH_SHORT).show();
                    setViewDisconnected();
                    killOtaThread();
                    break;
                case OTA_ERROR_EXIT:
                    String str = intent.getStringExtra(OTA_ERROR_MSG);
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: OTA_ERROR_EXIT  " + str);
                    MainActivity.stopProgressDialog();
                    mTxtVwOtaResult.setText("Error");
                    Toast.makeText(getActivity(), "Error  " + str, Toast.LENGTH_LONG).show();
                    break;
                case OTA_EXIT:
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: OTA_EXIT");
                    MainActivity.stopProgressDialog();
                    mTxtVwOtaResult.setText("completed");
                    Toast.makeText(getActivity(), getString(R.string.str_PlzDisconnectRestart), Toast.LENGTH_LONG).show();
                    break;
                case BLE_SERVICE_DISCOVERED_EMM:
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: SRV_DISCOVERED_EMM");
                    mBtnReadHwRev.setEnabled(true);
                    break;
                case BLE_CHAR_R_DI_HW_REV: {
                    String hardwareVer = intent.getStringExtra(EXTRA_DATA);
                    mTxtVwReadHardRev.setText(hardwareVer);
                    if (setHardwareVer(hardwareVer)) {
                        mBtnSetFile.setEnabled(true);
                    } else {
                        Toast.makeText(getActivity(), "Hardware revision mismatch", Toast.LENGTH_SHORT).show();
                    }
                    mCallbacks.cbAppendLog("[FrOTA]BC Rx: CHAR_R_DI_HW_REV  " + hardwareVer);
                    break;
                }
                case BLE_CHARA_FLASH_OPEN:
                    MainActivity.setProgressDialog(5);
                    mThreadFlashOta.setState(ThreadFlashOta.ST_PROCESS2);
                    break;
                case BLE_CHARA_FLASH_CLOSE:
                    mThreadFlashOta.setState(ThreadFlashOta.ST_COMPLETE);
                    MainActivity.stopWaitDialog();
                    mBtnDisconnect.setEnabled(true);
                    mBtnOtaStart.setEnabled(false);
                    mBtnSetFile.setEnabled(false);
                    mBtnReadHwRev.setEnabled(false);
                    Toast.makeText(getActivity(), getString(R.string.str_PlzDisconnectRestart), Toast.LENGTH_LONG).show();
                    break;
                case BLE_CHARA_EXTERNAL_MEMORY_READ1:
                    chara = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_READ2);
                    if (chara != null) {
                        mService.readCharacteristic(chara);
                    } else {
                        if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                            mThreadFlashOta.setState(ThreadFlashOta.ST_ERROR_EXIT);
                        } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                            mThreadE2promOta.setState(ThreadE2promOta.ST_ERROR_EXIT);
                        }
                    }
                    break;
                case BLE_CHARA_EXTERNAL_MEMORY_READ2:
                    break;
                case BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1:
                    chara = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_CHECKSUM2);
                    if (chara != null) {
                        mService.readCharacteristic(chara);
                    } else {
                        if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                            mThreadFlashOta.setState(ThreadFlashOta.ST_ERROR_EXIT);
                        } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                            mThreadE2promOta.setState(ThreadE2promOta.ST_ERROR_EXIT);
                        }
                    }
                    break;
                case BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2:
                    data = intent.getByteArrayExtra(EXTRA_DATA);
                    if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                        mThreadFlashOta.setCheckSum(data);
                    } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                        mThreadE2promOta.setCheckSum(data);
                    }
                    break;
                case BLE_CHARA_FLASH_ERASE:
                    mThreadFlashOta.setState(ThreadFlashOta.ST_PROCESS4);
                    break;
                case BLE_CHARA_EXTERNAL_MEMORY_WRITE:
                    if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                        mThreadFlashOta.setState(ThreadFlashOta.ST_PROCESS5);
                    } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                        mThreadE2promOta.setState(ThreadE2promOta.ST_PROCESS3);
                    }
                    break;
                case BLE_CHARA_FLAG_READ1:
                    chara = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLAG_READ2);
                    if (chara != null) {
                        mService.readCharacteristic(chara);
                    } else {
                        if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                            mThreadFlashOta.setState(ThreadFlashOta.ST_ERROR_EXIT);
                        } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                            mThreadE2promOta.setState(ThreadE2promOta.ST_ERROR_EXIT);
                        }
                    }
                    break;
                case BLE_CHARA_FLAG_READ2:
                    if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                        data = intent.getByteArrayExtra(EXTRA_DATA);
                        mThreadFlashOta.setWriteAreaFlag(data);
                        mThreadFlashOta.setState(ThreadFlashOta.ST_PROCESS3);
                    } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                        data = intent.getByteArrayExtra(EXTRA_DATA);
                        mThreadE2promOta.setWriteAreaFlag(data);
                        mThreadE2promOta.setState(ThreadE2promOta.ST_PROCESS2);
                    }
                    break;
                case BLE_CHARA_FLAG_CHANGE:
                    if (mOtaMemory == OTA_TARGET_MEMORY_FLASH) {
                        mThreadFlashOta.setState(ThreadFlashOta.ST_PROCESS6);
                    } else if (mOtaMemory == OTA_TARGET_MEMORY_E2PROM) {
                        mThreadE2promOta.setState(ThreadE2promOta.ST_COMPLETE);
                        MainActivity.stopWaitDialog();
                        mBtnDisconnect.setEnabled(true);
                        mBtnOtaStart.setEnabled(false);
                        mBtnSetFile.setEnabled(false);
                        mBtnReadHwRev.setEnabled(false);
                        Toast.makeText(getActivity(), getString(R.string.str_PlzDisconnectRestart), Toast.LENGTH_LONG).show();
                    }
                    break;
            }

        }
    };

    private static IntentFilter makeIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BLE_CONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED);
        intentFilter.addAction(BLE_DISCONNECTED_LL);
        intentFilter.addAction(OTA_EXIT);
        intentFilter.addAction(OTA_ERROR_EXIT);

        intentFilter.addAction(BLE_CHAR_R_DI_HW_REV);

        intentFilter.addAction(BLE_SERVICE_DISCOVERED_EMM);
        intentFilter.addAction(BLE_CHARA_FLASH_OPEN);
        intentFilter.addAction(BLE_CHARA_FLASH_CLOSE);
        intentFilter.addAction(BLE_CHARA_EXTERNAL_MEMORY_READ1);
        intentFilter.addAction(BLE_CHARA_EXTERNAL_MEMORY_READ2);
        intentFilter.addAction(BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1);
        intentFilter.addAction(BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2);
        intentFilter.addAction(BLE_CHARA_FLASH_ERASE);
        intentFilter.addAction(BLE_CHARA_EXTERNAL_MEMORY_WRITE);
        intentFilter.addAction(BLE_CHARA_FLAG_READ1);
        intentFilter.addAction(BLE_CHARA_FLAG_READ2);
        intentFilter.addAction(BLE_CHARA_FLAG_CHANGE);

        return intentFilter;
    }

}

