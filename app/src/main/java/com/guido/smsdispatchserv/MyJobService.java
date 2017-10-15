package com.guido.smsdispatchserv;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class MyJobService extends JobService {

    static int counter = 0;

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here

        // this is executed every second
        Log.i("in timer", "in timer ++++  "+ (counter++));

        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; // Answers the question: "Should this job be retried?"
    }
}