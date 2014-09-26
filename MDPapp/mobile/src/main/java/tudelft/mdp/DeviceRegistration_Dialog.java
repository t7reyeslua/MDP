package tudelft.mdp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import tudelft.mdp.deviceManager.NfcRegistrationAsyncTask;

/**
 * Dialog that is shown when a new NFC tag is detected and
 * a new device may be registered.
 */
public class DeviceRegistration_Dialog extends DialogFragment {

    private EditText mDeviceType;
    private EditText mDeviceDescription;
    private EditText mDeviceLocation;
    private String mNfcTag;
    private Dialog mDialog;

    private static final String TAG = "MDP-NewDeviceDialog";

    public void setNfcTag(String nfcTag) {
        mNfcTag = nfcTag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setTitle("Register new device");
        builder.setIcon(R.drawable.ic_action_dock_dark);


        builder.setView(inflater.inflate(R.layout.dialog_new_device, null))
                // Add action buttons
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Register new device tag
                        mDeviceType = (EditText) mDialog.findViewById(R.id.deviceType);
                        mDeviceDescription = (EditText) mDialog.findViewById(R.id.deviceDescription);
                        mDeviceLocation = (EditText) mDialog.findViewById(R.id.deviceLocation);

                        String mType = mDeviceType.getText().toString();
                        String mDescription = mDeviceDescription.getText().toString();
                        String mLocation = mDeviceLocation.getText().toString();

                        if (mType.isEmpty() || mDescription.isEmpty() || mLocation.isEmpty()) {
                            Toast.makeText(getActivity().getApplicationContext(), "Some fields are empty. Rescan the tag and try again.", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Registering the device", Toast.LENGTH_LONG)
                                    .show();
                            Log.i(TAG, "Registering the device");
                            new NfcRegistrationAsyncTask().execute(mNfcTag, mType, mDescription, mLocation, getActivity().getApplicationContext());

                        }

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });

        mDialog = builder.create();
        return mDialog;
    }
}
