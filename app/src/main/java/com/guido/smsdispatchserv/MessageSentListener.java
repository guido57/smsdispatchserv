package com.guido.smsdispatchserv;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Guido on 02/09/2017.
 */


public class MessageSentListener extends BroadcastReceiver {

    SharedPreferences prefs;



    @Override
    public void onReceive(Context context, Intent intent) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int resultCode = this.getResultCode();
        boolean successfullySent = resultCode == Activity.RESULT_OK;
        if(successfullySent)
            addListLog("Success! Sent: " + intent.getAction());

        //That boolean up there indicates the status of the message
        context.unregisterReceiver(this);
        //Notice how I get the app context again here and unregister this broadcast
        //receiver to clear it from the system since it won't be used again
    }

    public void addListLog(String addlog) {
        // get current date time
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
        String currentDateandTime = sdf.format(currentTime);
        // Retrieve the log values stored in shared preferences "prefs"
        Gson gson = new Gson();
        String jsonText = prefs.getString("myLogStrings", "");
        ArrayList<String> logStrings = gson.fromJson(jsonText, new TypeToken<ArrayList<String>>(){}.getType());
        if(logStrings == null)
            logStrings = new ArrayList<String>();
        // add the new entry "addLog"
        logStrings.add(0, currentDateandTime + " " + addlog);
        jsonText = gson.toJson(logStrings);
        // save to Preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("myLogStrings", jsonText);
        editor.commit();
    }

}
