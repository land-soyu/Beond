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

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import jp.co.toshiba.semicon.sapp01.R;


public class NumPickDialogLED extends DialogFragment {

    private static final String NUM_PICK_DIALOG_TITLE = "title";
    private static final String NUM_PICK_DIALOG_DATA = "data";
    private static final String[] FREQUENCY_VALUES = new String[] {"256", "512", "1024", "2048", "4096", "8192", "16384"};
    private static int sFrequencyArrayNumber;
    private static int sDutyCycle;
    private static int sRhythmPattern;

    public NumPickDialogLED(){
    }

    public static NumPickDialogLED newInstance(String title, int data) {
        NumPickDialogLED fragment = new NumPickDialogLED();
        Bundle args = new Bundle();
        args.putString(NUM_PICK_DIALOG_TITLE, title);
        args.putInt(NUM_PICK_DIALOG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    interface DialogListener {
        void onPositiveButtonClick(String title, int value);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)getActivity().findViewById(android.R.id.content);

        final String title = getArguments().getString(NUM_PICK_DIALOG_TITLE);
        int data = getArguments().getInt(NUM_PICK_DIALOG_DATA);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if ("Frequency".equals(title)) {

            View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_numpick_led_freq, rootView, false);
            final NumberPicker numPick = (NumberPicker) view.findViewById(R.id.numPickerFreq);
            numPick.setMaxValue(FREQUENCY_VALUES.length - 1);
            numPick.setMinValue(0);
            numPick.setDisplayedValues(FREQUENCY_VALUES);

            for (String FrequencyValue : FREQUENCY_VALUES) {
                if (data == Integer.parseInt(FrequencyValue)) {
                    sFrequencyArrayNumber = data;
                }
            }
            numPick.setValue(sFrequencyArrayNumber);

            numPick.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            builder.setTitle(title);
            builder.setView(view);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    sFrequencyArrayNumber = numPick.getValue();
                    DialogListener mListener =(DialogListener) getTargetFragment();
                    mListener.onPositiveButtonClick(title, Integer.parseInt(FREQUENCY_VALUES[numPick.getValue()]));
                }
            });
        }
        else if ("Duty Cycle".equals(title)) {

            View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_numpick_led_duty, rootView, false);
            final NumberPicker numPick10 = (NumberPicker) view.findViewById(R.id.numPickerDuty10);
            numPick10.setMaxValue(9);
            numPick10.setMinValue(0);
            final NumberPicker numPick01 = (NumberPicker) view.findViewById(R.id.numPickerDuty01);
            numPick01.setMaxValue(9);
            numPick01.setMinValue(0);

            for (int i = 0; i < 100; i++) {
                if (data == i) {
                    sDutyCycle = data;
                }
            }
            numPick10.setValue(sDutyCycle / 10);
            numPick01.setValue(sDutyCycle % 10);

            numPick10.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            numPick01.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

            builder.setTitle(title);
            builder.setView(view);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    sDutyCycle = (numPick10.getValue() * 10) + (numPick01.getValue());
                    DialogListener mListener = (DialogListener) getTargetFragment();
                    mListener.onPositiveButtonClick(title, sDutyCycle);
                }
            });
        }
        else if ("Rhythm Pattern".equals(title)) {

            View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_numpick_led_rhythm, rootView, false);
            final TextView txtVwRhythmHex = (TextView)view.findViewById(R.id.txtViewRhythmHex);
            final int[] buttonID = new int[] {R.id.toggleBitOnOff00, R.id.toggleBitOnOff01, R.id.toggleBitOnOff02,
                                              R.id.toggleBitOnOff03, R.id.toggleBitOnOff04, R.id.toggleBitOnOff05,
                                              R.id.toggleBitOnOff06, R.id.toggleBitOnOff07, R.id.toggleBitOnOff08,
                                              R.id.toggleBitOnOff09, R.id.toggleBitOnOff10 ,R.id.toggleBitOnOff11,
                                              R.id.toggleBitOnOff12, R.id.toggleBitOnOff13, R.id.toggleBitOnOff14,
                                              R.id.toggleBitOnOff15, R.id.toggleBitOnOff16, R.id.toggleBitOnOff17,
                                              R.id.toggleBitOnOff18, R.id.toggleBitOnOff19};

            sRhythmPattern = data;

            for (int i = 0; i < buttonID.length; i++){
                ToggleButton pushedButton = ((ToggleButton)view.findViewById(buttonID[i]));
                pushedButton.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        for (int i = 0; i < buttonID.length; i++){
                            if (buttonView.getId() == buttonID[i]){
                                sRhythmPattern = isChecked  ? (sRhythmPattern | (1 << i)) : (sRhythmPattern - (1 << i));
                                break;
                            }
                        }
                        txtVwRhythmHex.setText(String.format("0x%06X", sRhythmPattern));
                    }
                });
                if ( ((sRhythmPattern >> i) & 0x01) == 0x01 ){
                    pushedButton.setChecked(true);
                }
            }

            builder.setTitle(title);
            builder.setView(view);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    sRhythmPattern = Integer.decode(txtVwRhythmHex.getText().toString());
                    DialogListener mListener =(DialogListener) getTargetFragment();
                    mListener.onPositiveButtonClick(title, sRhythmPattern);
                }
            });
        }
        return builder.create();
    }
}
