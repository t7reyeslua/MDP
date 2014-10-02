package tudelft.mdp.deviceManager;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import tudelft.mdp.R;

/**
 * Dialog that is shown when a new NFC tag is detected and
 * a new device may be registered.
 */
public class DeviceRegistrationAlertDialog extends DialogFragment {

    private AutoCompleteTextView mDeviceType;
    private EditText mDeviceDescription;
    private AutoCompleteTextView mDeviceLocation;
    private String mNfcTag;
    private Dialog mDialog;

    private static final String TAG = "MDP-NewDeviceDialog";

    public void setNfcTag(String nfcTag) {
        mNfcTag = nfcTag;
    }

    private static final String[] DEVICES = new String[] {
            "Television",
            "Washing machine",
            "Video Game console",
            "Hotplates",
            "Microwave",
            "Lights",
            "Computer",
            "Stereo",
            "Fridge",
            "Cooker",
            "Grill",
            "Vacuum cleaner"
    };

    private static final String[] LOCATIONS = new String[] {
            "Home", "Office"
    };


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_new_device, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle("Register new device");
        builder.setIcon(R.drawable.ic_action_dock_dark);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {


                        // Register new device tag
                        String mType = mDeviceType.getText().toString();
                        String mDescription = mDeviceDescription.getText().toString();
                        String mLocation = mDeviceLocation.getText().toString();

                        if (mType.isEmpty() || mLocation.isEmpty()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Some fields are empty. Rescan the tag and try again.", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Registering the device", Toast.LENGTH_LONG)
                                    .show();
                            Log.i(TAG, "Registering the device");
                            new DeviceRegistrationAsyncTask().execute(mNfcTag, mType, mDescription, mLocation, getActivity().getApplicationContext());

                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        mDialog = builder.create();


        mDeviceType = (AutoCompleteTextView) view.findViewById(R.id.deviceType);
        mDeviceDescription = (EditText) view.findViewById(R.id.deviceDescription);
        mDeviceLocation = (AutoCompleteTextView) view.findViewById(R.id.deviceLocation);

        mDeviceType.setThreshold(1);
        mDeviceLocation.setThreshold(1);

        ArrayAdapter<String> deviceAdapter   = new ArrayAdapter<String>(mDialog.getContext(), android.R.layout.simple_dropdown_item_1line, DEVICES);
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(mDialog.getContext(), android.R.layout.simple_dropdown_item_1line, LOCATIONS);

        mDeviceType.setAdapter(deviceAdapter);
        mDeviceLocation.setAdapter(locationAdapter);

        deviceAdapter.notifyDataSetChanged();
        locationAdapter.notifyDataSetChanged();

        return mDialog;
    }
}
