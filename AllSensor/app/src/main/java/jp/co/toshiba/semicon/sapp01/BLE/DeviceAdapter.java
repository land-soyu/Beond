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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import jp.co.toshiba.semicon.sapp01.R;


class DeviceAdapter extends ArrayAdapter<ScannedDevice> {

    private final List<ScannedDevice> mList;
    private final LayoutInflater mInflater;
    private final int mResId;


    DeviceAdapter(Context context, List<ScannedDevice> objects) {
        super(context, R.layout.listitem_device, objects);
        mResId = R.layout.listitem_device;
        mList = objects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(mResId, null);
        }

        TextView name = (TextView) convertView.findViewById(R.id.device_name);
        TextView address = (TextView) convertView.findViewById(R.id.device_address);
        TextView rssi = (TextView) convertView.findViewById(R.id.device_rssi);
        ScannedDevice item = getItem(position);
        if (item != null) {
            name.setText(item.getDisplayName());
            address.setText(item.getDevice().getAddress());
            int rssiValue = item.getRssi();
            if (rssiValue != 0x7F) {
                rssi.setText(String.format("RSSI:%s", Integer.toString(item.getRssi())));
            } else {
                rssi.setText(R.string.str_Bonded); // WORKAROUND: Displaying Bonded device
            }
        }

        return convertView;
    }


    // add or update BluetoothDevice
    void update(BluetoothDevice newDevice, int rssi) {
        if ((newDevice == null) || (newDevice.getAddress() == null)) {
            return;
        }

        boolean contains = false;
        for (ScannedDevice device : mList) {
            if (newDevice.getAddress().equals(device.getDevice().getAddress())) {
                contains = true;
                device.setRssi(rssi); // update
                break;
            }
        }
        if (!contains) {
            // add new BluetoothDevice
            mList.add(new ScannedDevice(newDevice, rssi));
        }
        notifyDataSetChanged();
    }
}
