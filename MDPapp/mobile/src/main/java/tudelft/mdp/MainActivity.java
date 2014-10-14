package tudelft.mdp;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import tudelft.mdp.dashboard.DashboardFragment;
import tudelft.mdp.deviceManager.DeviceDetectionAsyncTask;
import tudelft.mdp.deviceManager.DeviceManagerFragment;
import tudelft.mdp.enums.NavigationDrawer;
import tudelft.mdp.enums.UserPreferences;
import tudelft.mdp.gcm.GcmRegistrationAsyncTask;
import tudelft.mdp.locationTracker.LocationCalibrationFragment;
import tudelft.mdp.locationTracker.LocationDetectionService;
import tudelft.mdp.locationTracker.LocationFingerprintFragment;
import tudelft.mdp.locationTracker.LocationHistoryFragment;
import tudelft.mdp.ui.ExpandableListAdapter;
import tudelft.mdp.utilities.SensorViewerFragment;


public class MainActivity extends GoogleLoginManager implements ServiceConnection{

    private static final String TAG = "MDP-Main";
    public static final String MIME_TEXT_PLAIN = "text/plain";

    private String[] mMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ExpandableListView mDrawerListExpandable;
    private ExpandableListAdapter mDrawerListAdapter;

    private ArrayList<String> groupItem = new ArrayList<String>();
    private ArrayList<Object> childItem = new ArrayList<Object>();
    private TextView mUsernamePic;
    private ImageView imgProfilePic;
    private NfcAdapter mNfcAdapter;
    private String mUsername;

    private Person currentUser;

    private Messenger mServiceMessengerLocation = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mUsername = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(UserPreferences.USERNAME, null);
        if (mUsername == null) {
            showLoginScreen();
        }

        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        mGoogleApiClient = buildGoogleApiClient();

        if (!mGoogleApiClient.isConnected()){
            login_signin();
        }

        configureActionBar();
        initDrawer();

        verifyNFCenabled();

        automaticBinding();

        new GcmRegistrationAsyncTask().execute(this);

        selectItem(NavigationDrawer.DASHBOARD, -1);


    }



    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getActionBar() != null)
            getActionBar().setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        switch (item.getItemId()) {
            case R.id.action_settings:
                return false;
            case R.id.action_signout:
                login_signout();
                return true;
            case R.id.action_revoke:
                login_revoke();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Reaching onConnected means we consider the user signed in.
        Log.i(TAG, "onConnected to Google Play Services");


        // Retrieve some profile information to personalize our app for the user.
        currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);



        getProfileInformation();


        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;
    }

    @Override
    public void onSignedOut() {
        //Return to login screen
        showLoginScreen();
    }

    private void showLoginScreen(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void configureActionBar(){
        // enable ActionBar app icon to behave as action to toggle nav drawer
        if(getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerListExpandable);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void initDrawer() {
        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getResources().getStringArray(R.array.menu_array);

        setGroupData();
        setChildGroupData();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListAdapter = new ExpandableListAdapter(this, groupItem, childItem);
        mDrawerListExpandable = (ExpandableListView) findViewById(R.id.left_drawer_exp);
        mDrawerListExpandable.setAdapter(mDrawerListAdapter);


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerListExpandable.setOnGroupClickListener(new ExpDrawerGroupClickListener());
        mDrawerListExpandable.setOnChildClickListener(new ExpDrawerChildClickListener());


        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


    }

    private class ExpDrawerChildClickListener implements ExpandableListView.OnChildClickListener {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v,
                int groupPosition, int childPosition, long id) {
           /* Toast.makeText(getApplicationContext(), groupPosition + "" + childPosition +" Clicked On Child" + v.getTag(),
                    Toast.LENGTH_SHORT).show();*/
            selectItem(groupPosition,childPosition);
            return true;
        }
    }

    private class ExpDrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {
        @Override
        public boolean onGroupClick(ExpandableListView parent, View v,
                int groupPosition, long id) {

            if (parent.isGroupExpanded(groupPosition)){
                parent.collapseGroup(groupPosition);
            }else {
                parent.expandGroup(groupPosition, true);
            }
            selectItem(groupPosition, -1);
            return true;
        }
    }

    private void setGroupData() {
        groupItem.add("Profile Picture");
        groupItem.add("Dashboard");
        groupItem.add("Activity Monitor");
        groupItem.add("Location Tracker");
        groupItem.add("Device Manager");
        groupItem.add("Utilities");
    }


    private void setChildGroupData() {
        /**
         * Add Data For Dashboard
         */
        ArrayList<String> child = new ArrayList<String>();
        childItem.add(child);

        /**
         * Add Data For Dashboard
         */
        childItem.add(child);

        /**
         * Add Data For Activity Monitor
         */
        child = new ArrayList<String>();
        child.add("Activity History");
        /*child.add("Training");
        child.add("Confusion Matrix");
        child.add("Timeline");*/
        childItem.add(child);
        /**
         * Add Data For Location Tracker
         */
        child = new ArrayList<String>();
        child.add("Location History");
        child.add("Fingerprinting");
        child.add("Calibration");
        child.add("Locator");
        childItem.add(child);
        /**
         * Add Data For Device Manager
         */
        child = new ArrayList<String>();
        /*child.add("Fingerprinting");
        child.add("Locator");*/
        childItem.add(child);
        /**
         * Add Data For Utilities
         */
        child = new ArrayList<String>();
        child.add("Sensors recorder");
        /*child.add("Network RSSI");
        child.add("Step Counter");
        child.add("Orientation");
        child.add("DB manager");*/
        childItem.add(child);
    }



    private int findPosition(int groupPosition, int childPosition){
        int position = 0;
        if (childPosition == -1){
            position = groupPosition;
        } else {
            if (groupPosition == NavigationDrawer.ACTIVITYMONITOR){
                switch (childPosition){
                    default:
                        break;
                }
            }else if (groupPosition == NavigationDrawer.LOCATIONTRACKER){
                switch (childPosition){
                    case 0:
                        position = NavigationDrawer.LOCATIONHISTORY;
                        break;
                    case 1:
                        position = NavigationDrawer.LOCATIONTFINGERPRINTING;
                        break;
                    case 2:
                        position = NavigationDrawer.LOCATIONTCALIBRATION;
                        break;
                    case 3:
                        position = NavigationDrawer.LOCATOR;
                        break;
                    default:
                        break;
                }
            } else if (groupPosition == NavigationDrawer.DEVICEMANAGER){
                switch (childPosition){
                    default:
                        break;
                }
            } else if (groupPosition == NavigationDrawer.UTILITIES){
                switch (childPosition){
                    case 0:
                        position = NavigationDrawer.SENSORSVIEWER;
                        break;
                    default:
                        break;
                }
            }
        }

        return position;
    }


    private void selectItem(int groupPosition, int childPosition) {

        // Create a new fragment and specify the planet to show based on position
        Fragment fragment;
        FragmentManager fragmentManager = getFragmentManager();
        Bundle args;

        int position = findPosition(groupPosition,childPosition);

        // Insert the fragment by replacing any existing fragment
        switch (position) {
            case NavigationDrawer.DASHBOARD:
                fragment =  new DashboardFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_dashboard")
                        .commit();
                break;
            case NavigationDrawer.ACTIVITYMONITOR:
                break;
            case NavigationDrawer.LOCATIONHISTORY:
                fragment =  new LocationHistoryFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_locHistoryManager")
                        .commit();
                break;
            case NavigationDrawer.LOCATIONTFINGERPRINTING:
                fragment =  new LocationFingerprintFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_locFingerprint")
                        .commit();
                break;
            case NavigationDrawer.LOCATIONTCALIBRATION:
                fragment =  new LocationCalibrationFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_locCalibration")
                        .commit();
                break;
            case NavigationDrawer.DEVICEMANAGER:
                fragment =  new DeviceManagerFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_deviceManager")
                        .commit();
                break;
            case NavigationDrawer.SENSORSVIEWER:
                fragment =  new SensorViewerFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, "id_sensorsviewer")
                        .commit();
                break;
            default:
                break;
        }

        fragmentManager.executePendingTransactions();
        if ((mDrawerListExpandable.getExpandableListAdapter().getChildrenCount(groupPosition) == 0) || (childPosition > -1)) {
            setTitle(mMenuTitles[groupPosition]);
            mDrawerLayout.closeDrawer(mDrawerListExpandable);
        }


    }

    /**
     * Fetching user's information name, email, profile pic
     * */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                currentUser = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentUser.getDisplayName();
                String personPhotoUrl = currentUser.getImage().getUrl();
                String personGooglePlusProfile = currentUser.getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                String personId = currentUser.getId();

                Log.e(TAG, "Name: " + personName + ", Id: " + personId +  ", plusProfile: "
                        + personGooglePlusProfile + ", email: " + email
                        + ", Image: " + personPhotoUrl);


                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERNAME, personName).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERID, personId).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERMAIL, email).commit();
                PreferenceManager.getDefaultSharedPreferences(this).edit().putString(
                        UserPreferences.USERPIC, personPhotoUrl).commit();

                mUsernamePic = (TextView) findViewById(R.id.textViewUser);
                mUsernamePic.setText(" " + personName + " ");
                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                personPhotoUrl = personPhotoUrl.substring(0,
                        personPhotoUrl.length() - 2)
                        + 560;

                imgProfilePic = (ImageView) findViewById(R.id.imgProfile);
                new LoadProfileImage(imgProfilePic).execute(personPhotoUrl);

                mDrawerListAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * NFC related methods.
     *
     * When our app is already opened and we attach the tag again, the app is opened a second time
     * instead of delivering the tag directly. This is not our intended behavior.
     * We bypass the problem by using a Foreground Dispatch.
     */

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */

        Log.d(TAG, "OnResume");
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */

        Log.d(TAG, "OnPause");
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        automaticUnbinding();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        Log.d(TAG, "OnNewIntent");
        handleIntent(intent);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public void verifyNFCenabled(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC is disabled.", Toast.LENGTH_LONG).show();
        }
        handleIntent(getIntent());
    }


    private void handleIntent(Intent intent) {

        Log.d(TAG, "handleIntent");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Log.d(TAG, action);
            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Log.d(TAG, action);
            String nfcUID = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            //mUsername.append("\nNFC TAG: " + nfcUID);
            if (!mGoogleApiClient.isConnected()){
                login_signin();
            }



            new DeviceDetectionAsyncTask().execute(this, nfcUID, mUsername, getFragmentManager());
            Vibrator v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
            v.vibrate(500);

            Log.d(TAG, nfcUID);
        }
    }

    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     *
     */
    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays
                        .equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {

            /*
            if (result != null) {
                mUsername.append("\nRead content: " + result);
            }
            */
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }


    }

    /* Location Service Routines*/
    private void automaticBinding() {
        startServiceLocation();
    }

    private void automaticUnbinding() {
        stopServiceLocation();
    }

    public void startServiceLocation(){

        Log.i(TAG, "Location Service: START");
        Intent intent = new Intent(this, LocationDetectionService.class);
        this.startService(intent);
    }

    public void stopServiceLocation(){

        Log.i(TAG, "Location Service: STOP");
        this.stopService(new Intent(this, LocationDetectionService.class));
    }



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i(TAG, "Location Service: onServiceConnected");
        mServiceMessengerLocation = new Messenger(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.i(TAG, "Location Service: onServiceDisconnected");
        if (mServiceMessengerLocation != null) {
            mServiceMessengerLocation = null;
        }
    }

}
