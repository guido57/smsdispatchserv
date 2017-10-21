package com.guido.smsdispatchserv;

import android.util.Log;
import android.view.MenuItem;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MyJobService extends JobService {

    static int counter = 0;

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here

        // this is executed by filrebase jobdispatcher every 5 seconds or so
        Log.i("in timer", "in timer ++++  "+ (counter++));
        // update the counter on the MainActivity, if it exixts!
        if(MainActivity.mMenuItemCounter != null){
            String cstr = Integer.toString(counter);
            MainActivity.mMenuItemCounter.setTitle(cstr);
        }
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}