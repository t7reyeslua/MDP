package tudelft.mdp;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;


public class Main_Activity extends GoogleLoginManager {

    private static final String TAG = "MDP-Main";

    private String[] mMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ExpandableListView mDrawerListExpandable;

    private ArrayList<String> groupItem = new ArrayList<String>();
    private ArrayList<Object> childItem = new ArrayList<Object>();
    private TextView mUsername;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mUsername = (TextView) findViewById(R.id.username);

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
        Log.i(TAG, "onConnected");


        // Retrieve some profile information to personalize our app for the user.
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        mUsername.setText(String.format(
                getResources().getString(R.string.signed_in_as),
                currentUser.getDisplayName()));


        // Indicate that the sign in process is complete.
        mSignInProgress = STATE_DEFAULT;
    }

    @Override
    public void onSignedOut() {
        //Return to login screen
        Intent intent = new Intent(this, Login_Activity.class);
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
        //mMenuTitles = getResources().getStringArray(R.array.menu_array);

        setGroupData();
        setChildGroupData();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListExpandable = (ExpandableListView) findViewById(R.id.left_drawer_exp);
        mDrawerListExpandable.setAdapter(new ExpandableListAdapter(this, groupItem, childItem));


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
            //selectItem(groupPosition,childPosition);
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
            //selectItem(groupPosition, -1);
            return true;
        }
    }

    private void setGroupData() {
        groupItem.add("Dashboard");
        groupItem.add("Activity Monitor");
        groupItem.add("Location Tracker");
        groupItem.add("Utilities");
    }


    private void setChildGroupData() {
        /**
         * Add Data For Dashboard
         */
        ArrayList<String> child = new ArrayList<String>();
        childItem.add(child);

        /**
         * Add Data For Activity Monitor
         */
        child = new ArrayList<String>();
        /*child.add("Training");
        child.add("Confusion Matrix");
        child.add("Timeline");*/
        childItem.add(child);
        /**
         * Add Data For Location Tracker
         */
        child = new ArrayList<String>();
        /*child.add("Fingerprinting");
        child.add("Locator");*/
        childItem.add(child);
        /**
         * Add Data For Utilities
         */
        child = new ArrayList<String>();
        /*child.add("Sensors List");
        child.add("Network RSSI");
        child.add("Step Counter");
        child.add("Orientation");
        child.add("DB manager");*/
        childItem.add(child);
    }

}
