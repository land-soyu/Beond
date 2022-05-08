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

import java.util.UUID;


public final class BleServiceConstants {

    private BleServiceConstants(){
    }

    public static final String EXTRA_DATA          = "jp.co.toshiba.semicon.sapp01.EXTRA_DATA";
    public static final String BLE_CONNECTED       = "jp.co.toshiba.semicon.sapp01.BLE_CONNECTED";
    public static final String BLE_DISCONNECTED    = "jp.co.toshiba.semicon.sapp01.BLE_DISCONNECTED";
    public static final String BLE_DISCONNECTED_LL = "jp.co.toshiba.semicon.sapp01.BLE_DISCONNECTED_LL";
    
    // 0x2902: Client Characteristic Config
    static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // 0x1800: Generic Access Profile(GA)
    public static final String BLE_SERVICE_DISCOVERED_GA = "jp.co.toshiba.semicon.sapp01.BLE_SERVICE_DISCOVERED_GA";
    public static final String BLE_CHAR_R_GA_DEVICE_NAME = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_GA_DEVICE_NAME";
    public static final String BLE_CHAR_R_GA_APPEARANCE  = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_GA_APPEARANCE";
    public static final UUID SERVICE_GENERIC_ACCESS      = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_DEVICE_NAME           = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_APPEARANCE            = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");

    // 0x180A: Device Info Service(DI)
    public static final String BLE_SERVICE_DISCOVERED_DI       = "jp.co.toshiba.semicon.sapp01.BLE_SERVICE_DISCOVERED_DI";
    public static final String BLE_CHAR_R_DI_SW_REV            = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_DI_SW_REV";
    public static final String BLE_CHAR_R_DI_FW_REV            = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_DI_FW_REV";
    public static final String BLE_CHAR_R_DI_HW_REV            = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_DI_HW_REV";
    public static final String BLE_CHAR_R_DI_MANUFACTURER_NAME = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_R_DI_MANUFACTURER_NAME";
    public static final UUID SERVICE_DEVICE_INFO               = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_HARDWARE_REV                = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_SOFTWARE_REV                = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_FIRMWARE_REV                = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_MANUFACTURE_NAME            = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");

    // 0x180F: Battery Service(BAS)
    public static final String BLE_SERVICE_DISCOVERED_BAS = "jp.co.toshiba.semicon.sapp01.BLE_SERVICE_DISCOVERED_BAS";
    public static final String BLE_CHAR_N_BATTERY_LV      = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_N_BATTERY_LV";
    public static final String BLE_DESC_W_BATTERY_LV      = "jp.co.toshiba.semicon.sapp01.BLE_DESC_W_BATTERY_LV";
    public static final UUID SERVICE_BATTERY              = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARA_BATTERY_LV             = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    // CUSTOM: SAPP-01 Original Service(SOS)
    public static final String BLE_SERVICE_DISCOVERED_SOS = "jp.co.toshiba.semicon.sapp01.BLE_SERVICE_DISCOVERED_SOS";
    public static final String BLE_CHAR_W_LED             = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_W_LED";
    public static final String BLE_CHAR_W_CONSOLE         = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_W_CONSOLE";
    public static final String BLE_CHAR_N_SWITCH          = "jp.co.toshiba.semicon.sapp01.BLE_CHAR_N_SWITCH";
    public static final String BLE_DESC_W_SWITCH          = "jp.co.toshiba.semicon.sapp01.BLE_DESC_W_SWITCH";
    public static final UUID SERVICE_SAPPHIRE_ORIGINAL    = UUID.fromString("00005453-4220-7479-7065-204100000001");
    public static final UUID CHARA_LED_CONFIG             = UUID.fromString("00005453-4220-7479-7065-204100000201");
    public static final UUID CHARA_CONSOLE                = UUID.fromString("00005453-4220-7479-7065-204100000301");
    public static final UUID CHARA_SWITCH                 = UUID.fromString("00005453-4220-7479-7065-204100000601");

    // CUSTOM: External Memory Maintenance Service(EMM)
    public static final String BLE_SERVICE_DISCOVERED_EMM          = "jp.co.toshiba.semicon.sapp01.BLE_SERVICE_DISCOVERED_EMM";
    public static final String BLE_CHARA_FLASH_OPEN                = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLASH_OPEN";
    public static final String BLE_CHARA_FLASH_CLOSE               = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLASH_CLOSE";
    public static final String BLE_CHARA_EXTERNAL_MEMORY_READ1     = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_EXTERNAL_MEMORY_READ1";
    public static final String BLE_CHARA_EXTERNAL_MEMORY_READ2     = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_EXTERNAL_MEMORY_READ2";
    public static final String BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1 = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM1";
    public static final String BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2 = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_EXTERNAL_MEMORY_CHECKSUM2";
    public static final String BLE_CHARA_FLASH_ERASE               = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLASH_ERASE";
    public static final String BLE_CHARA_EXTERNAL_MEMORY_WRITE     = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_EXTERNAL_MEMORY_WRITE";
    public static final String BLE_CHARA_FLAG_READ1                = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLAG_READ1";
    public static final String BLE_CHARA_FLAG_READ2                = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLAG_READ2";
    public static final String BLE_CHARA_FLAG_CHANGE               = "jp.co.toshiba.semicon.sapp01.BLE_CHARA_FLAG_CHANGE";
    public static final UUID SERVICE_EXTERNAL_MEMORY_MAINTENANCE   = UUID.fromString("53616d70-6c65-4170-7044-656d6f010000");
    public static final UUID CHARA_FLASH_OPEN                      = UUID.fromString("53616d70-6c65-4170-7044-656d6f010001");
    public static final UUID CHARA_FLASH_CLOSE                     = UUID.fromString("53616d70-6c65-4170-7044-656d6f010002");
    public static final UUID CHARA_EXTERNAL_MEMORY_READ1           = UUID.fromString("53616d70-6c65-4170-7044-656d6f010003");
    public static final UUID CHARA_EXTERNAL_MEMORY_READ2           = UUID.fromString("53616d70-6c65-4170-7044-656d6f010004");
    public static final UUID CHARA_EXTERNAL_MEMORY_CHECKSUM1       = UUID.fromString("53616d70-6c65-4170-7044-656d6f010005");
    public static final UUID CHARA_EXTERNAL_MEMORY_CHECKSUM2       = UUID.fromString("53616d70-6c65-4170-7044-656d6f010006");
    public static final UUID CHARA_FLASH_ERASE                     = UUID.fromString("53616d70-6c65-4170-7044-656d6f010007");
    public static final UUID CHARA_EXTERNAL_MEMORY_WRITE           = UUID.fromString("53616d70-6c65-4170-7044-656d6f010008");
    public static final UUID CHARA_FLAG_READ1                      = UUID.fromString("53616d70-6c65-4170-7044-656d6f010009");
    public static final UUID CHARA_FLAG_READ2                      = UUID.fromString("53616d70-6c65-4170-7044-656d6f01000a");
    public static final UUID CHARA_FLAG_CHANGE                     = UUID.fromString("53616d70-6c65-4170-7044-656d6f01000b");
}
