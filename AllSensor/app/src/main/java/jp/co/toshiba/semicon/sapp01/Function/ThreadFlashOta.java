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

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import jp.co.toshiba.semicon.sapp01.BLE.BleService;
import jp.co.toshiba.semicon.sapp01.Main.MainActivity;

import static jp.co.toshiba.semicon.sapp01.BLE.BleServiceConstants.*;
import static jp.co.toshiba.semicon.sapp01.Function.FragmentOTA.OTA_ERROR_EXIT;


class ThreadFlashOta extends Thread {

    private static final byte ST_WAITING = 0x00;
    private static final byte ST_PROCESS1 = 0x01;
    static final byte ST_PROCESS2 = 0x02;
    static final byte ST_PROCESS3 = 0x03;
    static final byte ST_PROCESS4 = 0x04;
    static final byte ST_PROCESS5 = 0x05;
    static final byte ST_PROCESS6 = 0x06;
    static final byte ST_COMPLETE = 0x0E;
    static final byte ST_ERROR_EXIT = 0x0F;

    private static final String TAG = MainActivity.LOG_TAG;
    private static final byte TX_PROCESS_ANALYSIS = 0x00;
    private static final byte TX_PROCESS_WRITE = 0x01;
    private static final byte TX_PROCESS_CHECKSUM1_A = 0x02;
    private static final byte TX_PROCESS_CHECKSUM2_A = 0x03;
    private static final byte TX_PROCESS_CHECKSUM1_B = 0x04;
    private static final byte TX_PROCESS_CHECKSUM2_B = 0x05;
    private static final byte TX_PROCESS_END1 = 0x06;
    private static final byte TX_PROCESS_END2 = 0x07;
    private static final byte TX_PROCESS_FLAG_CHANGE = 0x08;
    private static final byte TX_PROCESS_COMPLETE = 0x0F;

    private static final byte ER_PROCESS_HEADER = 0x00;
    private static final byte ER_PROCESS_USER_APP0 = 0x01;
    private static final byte ER_PROCESS_USER_APP1 = 0x02;

    private static final byte FLASH_A_SIDE = 0;
    private static final byte FLASH_B_SIDE = 1;

    //Record type of Intel hex format
    private static final byte DATA = 0x00;
    private static final byte END_OF_FILE = 0x01;
    private static final byte EXTENDED_LINEAR_ADDRESS = 0x04;

    private static final byte ANALYSE_STATE_START = 0;
    private static final byte ANALYSE_STATE_RUNNING = 1;
    private static final byte ANALYSE_STATE_END = 2;

    private static int sChecksum;
    private static byte sOtaState;
    private static byte sWriteArea;

    private final Context mContext;
    private final String mStrFile;
    private final BleService mService;
    private RandomAccessFile mRandomAccessFile;
    private byte mTxProcess;
    private byte mErProcess;
    private int mDataSizeRamApp0;
    private int mDataSizeRamApp1;
    private long mChecksumRamApp0;
    private long mChecksumRamApp1;
    private int mFilePointer;
    private long mFileSize;
    private int mTargetAreaHeaderStartAddress;
    private int mTargetAreaApp0StartAddress;
    private int mTargetAreaApp1StartAddress;
    private int mRamPointer;
    private int mUpperAddress;
    private int mEraseAddress;
    private byte[] mRamBuffer;
    private int mRamBufferPointer;//Pointer of buffer for analysis
    private int mRamBufferPointer2;//Pointer of buffer for specifying data to send
    private byte mAnalysisState;

    private int mProgressCount1;
    private int mProgressCount2;

    private int RAM_APP0_TOP_ADDRESS;
    private int RAM_APP0_END_ADDRESS;
    private int RAM_APP1_TOP_ADDRESS;

    private int FLASH_A_SIDE_HEADER_ADDRESS;
    private int FLASH_B_SIDE_HEADER_ADDRESS;
    private int FLASH_A_SIDE_APP0_TOP_ADDRESS;
    private int FLASH_B_SIDE_APP0_TOP_ADDRESS;
    private int FLASH_APP0_SIZE;
    private int FLASH_A_SIDE_APP1_TOP_ADDRESS;
    private int FLASH_B_SIDE_APP1_TOP_ADDRESS;
    private int FLASH_APP1_SIZE;

    private int BOOT_AREA_FLAG_ADDRESS;
    private static final int FLAG_AREA_SIZE = 0xE00;


    ThreadFlashOta(Context context, String file) {

        mContext = context;
        mStrFile = file;
        sOtaState = ST_WAITING;

        mRandomAccessFile = null;

        mService = MainActivity.sBleService;
        if (mService == null) {
            broadcast(OTA_ERROR_EXIT);
        }
    }

    public void run() {

        setState(ST_PROCESS1);

        while (sOtaState != ST_COMPLETE && sOtaState != ST_ERROR_EXIT) {
            switch (sOtaState) {

                case ST_PROCESS1:
                    setState(ST_WAITING);
                    OtaFlashOpen();
                    break;
                case ST_PROCESS2:
                    setState(ST_WAITING);
                    OtaWriteProcessCheckFlag();
                    break;
                case ST_PROCESS3:
                    setState(ST_WAITING);
                    OtaWriteProcessStart();
                    break;
                case ST_PROCESS4:
                    setState(ST_WAITING);
                    OtaWriteProcessErase();
                    break;
                case ST_PROCESS5:
                    setState(ST_WAITING);
                    OtaWriteProcessMain();
                    break;
                case ST_PROCESS6:
                    setState(ST_WAITING);
                    OtaFlashClose();
                    break;
                case ST_WAITING:
                    MainActivity.threadSleep(10);
                    break;
                case ST_COMPLETE:
                case ST_ERROR_EXIT:
                    break;
                default:
                    Log.d(TAG, "OtaThread  unexpected status");
                    break;
            }

        }

        if (sOtaState == ST_COMPLETE) {
            MainActivity.setProgressDialog(99);
            broadcast(FragmentOTA.OTA_EXIT);
        }

        Log.d(TAG, "OtaThread  Stop");
    }


    void setState(byte newState) {
        sOtaState = newState;
    }

    void setWriteAreaFlag(byte[] data) {

        int tmp = ((((int) data[0] << 24) & 0xFF000000) | (((int) data[1] << 16) & 0x00FF0000) | (((int) data[2] << 8) & 0x0000FF00) | (((int) data[3]) & 0x000000FF));
        if (tmp == 0) {
            sWriteArea = FLASH_B_SIDE;
        } else if (tmp == 1) {
            sWriteArea = FLASH_A_SIDE;
        } else {
            sWriteArea = 0xF;   //error
        }

        setState(ST_PROCESS3);
    }

    void setCheckSum(byte[] data) {
        sChecksum = ((((int) data[0] << 24) & 0xFF000000) | (((int) data[1] << 16) & 0x00FF0000) | (((int) data[2] << 8) & 0x0000FF00) | (((int) data[3]) & 0x000000FF));
        setState(ST_PROCESS5);
    }

    void setRamAddress(int topApp0, int endApp0, int topApp1){
        RAM_APP0_TOP_ADDRESS = topApp0;
        RAM_APP0_END_ADDRESS = endApp0;
        RAM_APP1_TOP_ADDRESS = topApp1;
    }

    void setFlashHeaderAddress(int sideA, int sideB){
        FLASH_A_SIDE_HEADER_ADDRESS = sideA;
        FLASH_B_SIDE_HEADER_ADDRESS = sideB;
        BOOT_AREA_FLAG_ADDRESS = FLASH_B_SIDE_HEADER_ADDRESS + 0x200;
    }

    void setFlashUserApp0Address(int sideA, int sideB, int size){
        FLASH_A_SIDE_APP0_TOP_ADDRESS = sideA;
        FLASH_B_SIDE_APP0_TOP_ADDRESS = sideB;
        FLASH_APP0_SIZE = size;
    }

    void setFlashUserApp1Address(int sideA, int sideB, int size){
        FLASH_A_SIDE_APP1_TOP_ADDRESS = sideA;
        FLASH_B_SIDE_APP1_TOP_ADDRESS = sideB;
        FLASH_APP1_SIZE = size;
    }


    private void broadcast(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }


    private void broadcast(final String action, final String message) {
        final Intent intent = new Intent(action);
        intent.putExtra(FragmentOTA.OTA_ERROR_MSG, message);
        mContext.sendBroadcast(intent);
    }


    private void OtaFlashOpen() {

        BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLASH_OPEN);

        if (ch != null) {
            byte[] txBuff = new byte[]{0x01, 0x00, 0x00, 0x00, 0x00};
            ch.setValue(txBuff);
            mService.writeCharacteristic(ch);
        } else {
            setState(ST_ERROR_EXIT);
            broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
        }
    }

    private void OtaFlashClose() {

        BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLASH_CLOSE);

        if (ch != null) {
            byte[] txBuff = new byte[]{0x01};
            ch.setValue(txBuff);
            mService.writeCharacteristic(ch);
            MainActivity.setProgressDialog(100);
        } else {
            setState(ST_ERROR_EXIT);
            broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
        }
    }

    private void OtaWriteProcessCheckFlag() {

        BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLAG_READ1);

        if (ch != null) {
            byte txBuff[] = new byte[8];
            txBuff[0] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 24);
            txBuff[1] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 16);
            txBuff[2] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 8);
            txBuff[3] = (byte) (BOOT_AREA_FLAG_ADDRESS);
            txBuff[4] = (byte) ((FLAG_AREA_SIZE) >> 24);
            txBuff[5] = (byte) ((FLAG_AREA_SIZE) >> 16);
            txBuff[6] = (byte) ((FLAG_AREA_SIZE) >> 8);
            txBuff[7] = (byte) (FLAG_AREA_SIZE);

            ch.setValue(txBuff);
            mService.writeCharacteristic(ch);
            MainActivity.setProgressDialog(3);
        } else {
            setState(ST_ERROR_EXIT);
            broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
        }
    }


    private void OtaWriteProcessStart() {

        mAnalysisState = ANALYSE_STATE_START;
        mUpperAddress = RAM_APP0_TOP_ADDRESS >> 16;
        mRamBufferPointer = 0;
        mRamBufferPointer2 = 0;
        mProgressCount1 = 0;
        mProgressCount2 = 10;
        mTxProcess = TX_PROCESS_ANALYSIS;
        mErProcess = ER_PROCESS_HEADER;
        mDataSizeRamApp0 = 0;
        mDataSizeRamApp1 = 0;
        mChecksumRamApp0 = 0;
        mChecksumRamApp1 = 0;
        mFilePointer = 0;
        mFileSize = 0;

        if (sWriteArea == FLASH_A_SIDE) {
            mTargetAreaHeaderStartAddress = FLASH_A_SIDE_HEADER_ADDRESS;
            mTargetAreaApp0StartAddress = FLASH_A_SIDE_APP0_TOP_ADDRESS;
            mTargetAreaApp1StartAddress = FLASH_A_SIDE_APP1_TOP_ADDRESS;
        } else if (sWriteArea == FLASH_B_SIDE) {
            mTargetAreaHeaderStartAddress = FLASH_B_SIDE_HEADER_ADDRESS;
            mTargetAreaApp0StartAddress = FLASH_B_SIDE_APP0_TOP_ADDRESS;
            mTargetAreaApp1StartAddress = FLASH_B_SIDE_APP1_TOP_ADDRESS;
        } else {
            setState(ST_ERROR_EXIT);
            broadcast(OTA_ERROR_EXIT, "write area flag error");
            return;
        }

        setState(ST_PROCESS4);

    }

    private void OtaWriteProcessErase() {

        BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLASH_ERASE);

        if (ch != null) {
            byte[] txBuff = new byte[4];

            if (mErProcess == ER_PROCESS_HEADER) {
                txBuff[0] = (byte) (mTargetAreaHeaderStartAddress >> 24);
                txBuff[1] = (byte) (mTargetAreaHeaderStartAddress >> 16);
                txBuff[2] = (byte) (mTargetAreaHeaderStartAddress >> 8);
                txBuff[3] = (byte) mTargetAreaHeaderStartAddress;

                MainActivity.setProgressDialog(5);
                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);

                mErProcess = ER_PROCESS_USER_APP0;
                mEraseAddress = mTargetAreaApp0StartAddress;

            } else if (mErProcess == ER_PROCESS_USER_APP0) {
                txBuff[0] = (byte) (mEraseAddress >> 24);
                txBuff[1] = (byte) (mEraseAddress >> 16);
                txBuff[2] = (byte) (mEraseAddress >> 8);
                txBuff[3] = (byte) mEraseAddress;

                mEraseAddress += 0x1000;

                if (mEraseAddress > (mTargetAreaApp0StartAddress + FLASH_APP0_SIZE)) {
                    MainActivity.setProgressDialog(8);
                    setState(ST_PROCESS4);
                    mErProcess = ER_PROCESS_USER_APP1;
                    mEraseAddress = mTargetAreaApp1StartAddress;
                } else {
                    ch.setValue(txBuff);
                    mService.writeCharacteristic(ch);
                }
            } else if (mErProcess == ER_PROCESS_USER_APP1) {
                txBuff[0] = (byte) (mEraseAddress >> 24);
                txBuff[1] = (byte) (mEraseAddress >> 16);
                txBuff[2] = (byte) (mEraseAddress >> 8);
                txBuff[3] = (byte) mEraseAddress;

                mEraseAddress += 0x1000;

                if (mEraseAddress > (mTargetAreaApp1StartAddress + FLASH_APP1_SIZE)) {
                    MainActivity.setProgressDialog(9);
                    setState(ST_PROCESS5);
                } else {
                    ch.setValue(txBuff);
                    mService.writeCharacteristic(ch);
                }
            }


        } else {
            setState(ST_ERROR_EXIT);
            broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
        }
    }

    private void OtaWriteProcessMain() {

        if (mTxProcess == TX_PROCESS_ANALYSIS) {//Make 4k size blocks to write first

            mRamBuffer = new byte[4096];
            mRamBufferPointer = 0;
            mRamBufferPointer2 = 0;
            mAnalysisState = ANALYSE_STATE_START;
            mRamPointer = 0;

            while (mAnalysisState != ANALYSE_STATE_END) {

                String strReadLine = "";

                try {
                    mRandomAccessFile = new RandomAccessFile(mStrFile, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } // File open

                if (mFileSize == 0) {
                    try {
                        mFileSize = mRandomAccessFile.length();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Move the pointer of the file
                try {
                    mRandomAccessFile.seek(mFilePointer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Read one line
                try {
                    strReadLine = mRandomAccessFile.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    mRandomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int len = strReadLine.length();
                mFilePointer += len + 2; // length + CR(0x0D) + LF(0x0A)

                byte[] readBuff = OtaUtil.str2hex(strReadLine);

                if (!OtaUtil.isOk1LineChecksum(readBuff)) {
                    Log.d(TAG, "Hex File CheckSum Error!");
                    setState(ST_ERROR_EXIT);
                    broadcast(OTA_ERROR_EXIT, "Hex File CheckSum");
                    return;
                }


                assert readBuff != null;
                byte recordType = readBuff[3];

                if (recordType == DATA) {

                    if (mAnalysisState == ANALYSE_STATE_START) {//Set start address
                        mRamPointer = ((mUpperAddress << 16) & 0x00ff0000) | (((int) readBuff[1] << 8) & 0x0000ff00) | ((int) readBuff[2] & 0x000000ff);

                        if (mRamPointer < RAM_APP1_TOP_ADDRESS) {
                            //User App 0
                            for (byte i = 0; i < readBuff[0]; i++) {
                                mRamBuffer[i] = readBuff[4 + i];
                                int tmp = readBuff[4 + i];
                                mChecksumRamApp0 = mChecksumRamApp0 + (tmp & 0xFF);
                                mDataSizeRamApp0++;
                            }

                        } else {
                            //User App 1
                            for (byte i = 0; i < readBuff[0]; i++) {
                                mRamBuffer[i] = readBuff[4 + i];
                                int tmp = readBuff[4 + i];
                                mChecksumRamApp1 = mChecksumRamApp1 + (tmp & 0xFF);
                                mDataSizeRamApp1++;
                            }
                        }
                        mAnalysisState = ANALYSE_STATE_RUNNING;

                    } else {
                        mRamBufferPointer = ((mUpperAddress << 16) & 0x00ff0000) | (((int) readBuff[1] << 8) & 0x0000ff00) | ((int) readBuff[2] & 0x000000ff);
                        mRamBufferPointer = mRamBufferPointer - mRamPointer;
                        if (mRamPointer < RAM_APP1_TOP_ADDRESS) {
                            //User App 0
                            for (byte i = 0; i < readBuff[0]; i++) {
                                mRamBuffer[mRamBufferPointer] = readBuff[4 + i];
                                mRamBufferPointer++;
                                int tmp = readBuff[4 + i];
                                mChecksumRamApp0 = mChecksumRamApp0 + (tmp & 0xFF);
                                mDataSizeRamApp0++;
                            }
                        } else {
                            //User App 1
                            for (byte i = 0; i < readBuff[0]; i++) {
                                mRamBuffer[mRamBufferPointer] = readBuff[4 + i];
                                mRamBufferPointer++;
                                int tmp = readBuff[4 + i];
                                mChecksumRamApp1 = mChecksumRamApp1 + (tmp & 0xFF);
                                mDataSizeRamApp1++;
                            }
                        }
                    }

                    if ((4096 - mRamBufferPointer) < 16) {//If there is not 16 bytes remaining
                        mAnalysisState = ANALYSE_STATE_END;
                        mTxProcess = TX_PROCESS_WRITE;
                    }

                } else if (recordType == EXTENDED_LINEAR_ADDRESS) {
                    mUpperAddress = readBuff[5];
                    if ((readBuff[5] < (byte) (RAM_APP0_TOP_ADDRESS >> 16)) || (readBuff[5] > (byte) (RAM_APP0_END_ADDRESS >> 16))) {
                        mAnalysisState = ANALYSE_STATE_END;
                        mTxProcess = TX_PROCESS_WRITE;
                    }

                } else if (recordType == END_OF_FILE) {
                    mAnalysisState = ANALYSE_STATE_END;
                    if (mRamBufferPointer == 0) {
                        mTxProcess = TX_PROCESS_CHECKSUM1_A;
                    } else {
                        mTxProcess = TX_PROCESS_WRITE;
                    }
                    mFilePointer -= (len + 2);//Next, when returning to analysis, return EOF so that it reads first
                }

            }
        }


        if (mTxProcess == TX_PROCESS_WRITE) {

            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_WRITE);
            int flashAddress;   //(Top address of flash) + (Write address of RAM) - (Top address of RAM)

            if (ch != null) {
                if (mRamPointer < RAM_APP1_TOP_ADDRESS) {
                    //User App 0
                    flashAddress = mTargetAreaApp0StartAddress + mRamPointer - RAM_APP0_TOP_ADDRESS;
                } else {
                    //User App 1
                    flashAddress = mTargetAreaApp1StartAddress + mRamPointer - RAM_APP1_TOP_ADDRESS;
                }

                if ((mRamBufferPointer - mRamBufferPointer2) >= 0x10) {

                    byte txBuff[] = new byte[20];
                    flashAddress = flashAddress + mRamBufferPointer2;
                    if (checkOverflow(flashAddress)) {
                        txBuff[0] = (byte) (flashAddress >> 24);
                        txBuff[1] = (byte) (flashAddress >> 16);
                        txBuff[2] = (byte) (flashAddress >> 8);
                        txBuff[3] = (byte) flashAddress;
                    }

                    System.arraycopy(mRamBuffer, mRamBufferPointer2, txBuff, 4, 0x10);

                    mRamBufferPointer2 = mRamBufferPointer2 + 0x10;

                    if (mRamBufferPointer2 == mRamBufferPointer) {
                        mTxProcess = TX_PROCESS_ANALYSIS;
                    }

                    //progress bar
                    mProgressCount1++;
                    if (mProgressCount1 > (mFileSize / (45 * 80))) {
                        if (mProgressCount2 < 80) {
                            mProgressCount2++;
                            MainActivity.setProgressDialog(mProgressCount2);
                            mProgressCount1 = 0;
                        }
                    }

                    ch.setValue(txBuff);
                    mService.writeCharacteristic(ch);

                } else {

                    if (mRamBufferPointer2 != mRamBufferPointer) {
                        byte txBuff[] = new byte[4 + (mRamBufferPointer - mRamBufferPointer2)];
                        flashAddress = flashAddress + mRamBufferPointer2;
                        if (checkOverflow(flashAddress)) {
                            txBuff[0] = (byte) (flashAddress >> 24);
                            txBuff[1] = (byte) (flashAddress >> 16);
                            txBuff[2] = (byte) (flashAddress >> 8);
                            txBuff[3] = (byte) flashAddress;
                        }

                        System.arraycopy(mRamBuffer, mRamBufferPointer2, txBuff, 4, mRamBufferPointer - mRamBufferPointer2);

                        mRamBufferPointer2 = mRamBufferPointer;

                        ch.setValue(txBuff);
                        mService.writeCharacteristic(ch);
                    }
                    mTxProcess = TX_PROCESS_ANALYSIS;
                }
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }

        } else if (mTxProcess == TX_PROCESS_CHECKSUM1_A) {
            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_CHECKSUM1);

            if (ch != null) {
                byte txBuff[] = new byte[8];
                txBuff[0] = (byte) ((mTargetAreaApp0StartAddress) >> 24);
                txBuff[1] = (byte) ((mTargetAreaApp0StartAddress) >> 16);
                txBuff[2] = (byte) ((mTargetAreaApp0StartAddress) >> 8);
                txBuff[3] = (byte) mTargetAreaApp0StartAddress;
                txBuff[4] = (byte) (mDataSizeRamApp0 >> 24);
                txBuff[5] = (byte) (mDataSizeRamApp0 >> 16);
                txBuff[6] = (byte) (mDataSizeRamApp0 >> 8);
                txBuff[7] = (byte) mDataSizeRamApp0;

                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);
                MainActivity.setProgressDialog(80);
                mTxProcess = TX_PROCESS_CHECKSUM2_A;
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }

        } else if (mTxProcess == TX_PROCESS_CHECKSUM2_A) {
            if (sChecksum == mChecksumRamApp0) {
                mTxProcess = TX_PROCESS_CHECKSUM1_B;
                setState(ST_PROCESS5);
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "CheckSumA error");
            }

        } else if (mTxProcess == TX_PROCESS_CHECKSUM1_B) {
            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_CHECKSUM1);

            if (ch != null) {
                byte txBuff[] = new byte[8];
                txBuff[0] = (byte) (mTargetAreaApp1StartAddress >> 24);
                txBuff[1] = (byte) (mTargetAreaApp1StartAddress >> 16);
                txBuff[2] = (byte) (mTargetAreaApp1StartAddress >> 8);
                txBuff[3] = (byte) mTargetAreaApp1StartAddress;
                txBuff[4] = (byte) (mDataSizeRamApp1 >> 24);
                txBuff[5] = (byte) (mDataSizeRamApp1 >> 16);
                txBuff[6] = (byte) (mDataSizeRamApp1 >> 8);
                txBuff[7] = (byte) mDataSizeRamApp1;

                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);
                MainActivity.setProgressDialog(90);
                mTxProcess = TX_PROCESS_CHECKSUM2_B;
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }

        } else if (mTxProcess == TX_PROCESS_CHECKSUM2_B) {
            if (sChecksum == mChecksumRamApp1) {
                mTxProcess = TX_PROCESS_END1;
                setState(ST_PROCESS5);
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "CheckSumB error");
            }

        } else if (mTxProcess == TX_PROCESS_END1) {
            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_WRITE);

            if (ch != null) {
                byte txBuff[] = new byte[18];
                txBuff[0] = (byte) (mTargetAreaHeaderStartAddress >> 24);
                txBuff[1] = (byte) (mTargetAreaHeaderStartAddress >> 16);
                txBuff[2] = (byte) (mTargetAreaHeaderStartAddress >> 8);
                txBuff[3] = (byte) mTargetAreaHeaderStartAddress;
                txBuff[4] = (byte) (mDataSizeRamApp0 >> 24);
                txBuff[5] = (byte) (mDataSizeRamApp0 >> 16);
                txBuff[6] = (byte) (mDataSizeRamApp0 >> 8);
                txBuff[7] = (byte) mDataSizeRamApp0;
                txBuff[8] = (byte) (mChecksumRamApp0 >> 24);
                txBuff[9] = (byte) (mChecksumRamApp0 >> 16);
                txBuff[10] = (byte) (mChecksumRamApp0 >> 8);
                txBuff[11] = (byte) mChecksumRamApp0;
                txBuff[12] = 0x10;
                txBuff[13] = 0x48;
                txBuff[14] = 0x55;
                txBuff[15] = (byte) 0xAA;
                txBuff[16] = (byte) ((mTargetAreaApp0StartAddress / 0x1000) >> 8);
                txBuff[17] = (byte) (mTargetAreaApp0StartAddress / 0x1000);

                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);

                mTxProcess = TX_PROCESS_END2;
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }

        } else if (mTxProcess == TX_PROCESS_END2) {
            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_EXTERNAL_MEMORY_WRITE);

            if (ch != null) {
                byte txBuff[] = new byte[18];
                txBuff[0] = (byte) ((mTargetAreaHeaderStartAddress + 0x0010) >> 24);
                txBuff[1] = (byte) ((mTargetAreaHeaderStartAddress + 0x0010) >> 16);
                txBuff[2] = (byte) ((mTargetAreaHeaderStartAddress + 0x0010) >> 8);
                txBuff[3] = (byte) (mTargetAreaHeaderStartAddress + 0x0010);
                txBuff[4] = (byte) (mDataSizeRamApp1 >> 24);
                txBuff[5] = (byte) (mDataSizeRamApp1 >> 16);
                txBuff[6] = (byte) (mDataSizeRamApp1 >> 8);
                txBuff[7] = (byte) mDataSizeRamApp1;
                txBuff[8] = (byte) (mChecksumRamApp1 >> 24);
                txBuff[9] = (byte) (mChecksumRamApp1 >> 16);
                txBuff[10] = (byte) (mChecksumRamApp1 >> 8);
                txBuff[11] = (byte) mChecksumRamApp1;
                txBuff[12] = 0x10;
                txBuff[13] = 0x48;
                txBuff[14] = 0x55;
                txBuff[15] = (byte) 0xAA;

                txBuff[16] = (byte) ((mTargetAreaApp1StartAddress / 0x1000) >> 8);
                txBuff[17] = (byte) (mTargetAreaApp1StartAddress / 0x1000);

                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);

                mTxProcess = TX_PROCESS_FLAG_CHANGE;
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }

        } else if (mTxProcess == TX_PROCESS_FLAG_CHANGE) {
            BluetoothGattCharacteristic ch = mService.getCharacteristicByUuid(SERVICE_EXTERNAL_MEMORY_MAINTENANCE, CHARA_FLAG_CHANGE);

            if (ch != null) {
                byte txBuff[] = new byte[9];
                txBuff[0] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 24);
                txBuff[1] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 16);
                txBuff[2] = (byte) (BOOT_AREA_FLAG_ADDRESS >> 8);
                txBuff[3] = (byte) (BOOT_AREA_FLAG_ADDRESS);
                txBuff[4] = (byte) ((FLAG_AREA_SIZE) >> 24);
                txBuff[5] = (byte) ((FLAG_AREA_SIZE) >> 16);
                txBuff[6] = (byte) ((FLAG_AREA_SIZE) >> 8);
                txBuff[7] = (byte) (FLAG_AREA_SIZE);

                if (sWriteArea == FLASH_A_SIDE) {
                    txBuff[8] = 0x00;
                } else if (sWriteArea == FLASH_B_SIDE) {
                    txBuff[8] = 0x01;
                } else {
                    setState(ST_ERROR_EXIT);
                    broadcast(OTA_ERROR_EXIT, "flag change error");
                    return;
                }

                ch.setValue(txBuff);
                mService.writeCharacteristic(ch);
                MainActivity.setProgressDialog(95);
                mTxProcess = TX_PROCESS_COMPLETE;
            } else {
                setState(ST_ERROR_EXIT);
                broadcast(OTA_ERROR_EXIT, "getCharacteristic ");
            }
        }
    }

    private boolean checkOverflow(int address){

        if (sWriteArea == FLASH_A_SIDE) {
            if((address >= FLASH_A_SIDE_APP0_TOP_ADDRESS) &&
                    (address <= (FLASH_A_SIDE_APP0_TOP_ADDRESS + FLASH_APP0_SIZE)) ) {
                return true;
            }
            else if ( (address >= FLASH_A_SIDE_APP1_TOP_ADDRESS) &&
                    (address <= (FLASH_A_SIDE_APP1_TOP_ADDRESS + FLASH_APP1_SIZE)) ) {
                return true;
            }
        } else if (sWriteArea == FLASH_B_SIDE) {
            if((address >= FLASH_B_SIDE_APP0_TOP_ADDRESS) &&
                    (address <= (FLASH_B_SIDE_APP0_TOP_ADDRESS + FLASH_APP0_SIZE)) ) {
                return true;
            }
            else if ( (address >= FLASH_B_SIDE_APP1_TOP_ADDRESS) &&
                    (address <= (FLASH_B_SIDE_APP1_TOP_ADDRESS + FLASH_APP1_SIZE)) ) {
                return true;
            }
        }
        setState(ST_ERROR_EXIT);
        broadcast(OTA_ERROR_EXIT, "hex file Overflow");
        return false;
    }

}
