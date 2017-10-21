package com.guido.smsdispatchserv;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Activity mActivity;

    static MenuItem mMenuItemCounter;

    final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;
    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 2;
    final int MY_PERMISSIONS_READ_PHONE_STATE = 3;

    final static String JobServiceTAG = "com.guido.smsdispatchserver.my-unique-tag";

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listLogItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> listLogAdapter;

    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //show the icon in the action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        mActivity = this;
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        // get previusly stored log strings
        retrieveListLogItems();

        // Create list view
        ListView listLog = (ListView) findViewById(R.id.listview_log);
        listLogAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listLogItems);
        listLog.setAdapter(listLogAdapter);

        // register the preference listenere
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(spChanged);

        CheckSMSPermission();

        // start SensorService if not started yet
  //      SensorService mSensorService = new SensorService(this);
  //      Intent mServiceIntent = new Intent(this, mSensorService.getClass());
  //      if (!isMyServiceRunning(mSensorService.getClass())) {
  //          startService(mServiceIntent);
  //      }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // got here after onCreate and onStart or coming from Settings (where the JobService suld be set Active or Inactive
        StartStopJobService();
    }

    static FirebaseJobDispatcher  dispatcher;

    public static void StartTheJob(Context context){

        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(MyJobService.class)
                // uniquely identifies the job
                .setTag(JobServiceTAG)
                // one-off job
                .setRecurring(true)
                // don't persist past a device reboot
                //.setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setLifetime(Lifetime.FOREVER)
                // start between 0 and 60 seconds from now
                //.setTrigger(Trigger.executionWindow(0, 60))
                .setTrigger(Trigger.executionWindow(1,1))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                // .setConstraints(
                // only run on an unmetered network
                // Constraint.ON_UNMETERED_NETWORK
                // only run when the device is charging
                // ,Constraint.DEVICE_CHARGING
                // )
                //.setExtras(myExtrasBundle)
                .build();

        dispatcher.schedule(myJob);
    }

    public static void StopTheJob(Context context){

        // Create a new dispatcher using the Google Play driver.
        if(dispatcher==null)
            dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        dispatcher.cancel(JobServiceTAG);
    }


/*
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mMenuItemCounter = menu.findItem(R.id.action_counter);
        //now mMenuItemCounter is created and must be updated
        StartStopJobService();
        return true;
    }

    public void StartStopJobService(){

        // can do this after mMenuItemCounter creation only
        boolean keepaliveservice_active = prefs.getBoolean("pref_keepalive_service_active", true);
        if(keepaliveservice_active){
            Log.i(SMSBroadcastReceiver.class.getSimpleName(), "Called ater boot!!!!");
            // context.startService(new Intent(context, SensorService.class));
            StartTheJob(getApplicationContext());
            if(mMenuItemCounter != null )
                mMenuItemCounter.setTitle("Active");
        }else{
            StopTheJob(getApplicationContext());
            if(mMenuItemCounter != null )
                mMenuItemCounter.setTitle("Inactive");
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else
 */
         if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener spChanged = new
            SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    // a preference has been changed

                    // read log strings
                    retrieveListLogItems();
                    // update the adapter
                    listLogAdapter.clear();
                    listLogAdapter.addAll(listLogItems);
                    listLogAdapter.notifyDataSetChanged();
                }
            };

    public void retrieveListLogItems(){
        // Retrieve the values
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String jsonText = prefs.getString("myLogStrings", null);
        listLogItems = gson.fromJson(jsonText, new TypeToken<ArrayList<String>>(){}.getType());  //EDIT: gso to gson
        if(listLogItems == null)
            listLogItems = new ArrayList<String>();
    }

    public void saveListLogItems(){
        Gson gson = new Gson();
        String jsonText = gson.toJson(listLogItems);
        // save to Preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("myLogStrings", jsonText);
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.M)
    void CheckSMSPermission(){
        if(    ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED

          ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECEIVE_SMS)) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("This is an explanation");
                builder1.setMessage("Try again to request the permission to RECEIVE SMS.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECEIVE_SMS},
                                        MY_PERMISSIONS_REQUEST_RECEIVE_SMS);                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();


            } else {

                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                        MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_SMS)) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("This is an explanation");
                builder1.setMessage("Try again to request the permission to READ SMS.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECEIVE_SMS},
                                        MY_PERMISSIONS_REQUEST_READ_SMS);                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();


            } else {

                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_READ_SMS);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.SEND_SMS)) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("This is an explanation");
                builder1.setMessage("Try again to request the permission to SEND SMS.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                                        MY_PERMISSIONS_REQUEST_SEND_SMS);
                            }
                        });

                        builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,Manifest.permission.READ_PHONE_STATE)) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("This is an explanation");
                builder1.setMessage("Try again to request the permission to READ PHONE STATE.");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(
                                        mActivity,new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_READ_PHONE_STATE);
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_READ_PHONE_STATE);
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            case MY_PERMISSIONS_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
