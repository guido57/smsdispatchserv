package com.guido.smsdispatchserv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MyProfileAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ProfileItem> myDataset ;
    public boolean editmode;
    MenuItem miCancel;
    MenuItem miSave;
    MenuItem miEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editmode = false;

        // Create the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get data from SharedPrefs (if any) to Dataset
        myDataset = getMyDataset(getApplicationContext());
        // if empty or not containing mandatory items
        if(myDataset.size()==0
                || searchMyDataset(new ProfileItem(true,ItemType.ButtonAddFrom,".*Receive SMS From.*")).size()!=1
                || searchMyDataset(new ProfileItem(true,ItemType.ButtonAddTo,".*Send SMS To.*")).size()!=1
                || searchMyDataset(new ProfileItem(true,ItemType.ButtonAddFilter,".*Add a SMS text filter.*")).size()!=1
                )
            initMyDataset();

        // save myDataset to shared prefs
        saveMyDataset(myDataset, getApplicationContext());

        // Create the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new MyProfileAdapter(myDataset, getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu);

        miCancel = menu.findItem(R.id.action_profile_cancel);
        miSave = menu.findItem(R.id.action_profile_save);
        miEdit = menu.findItem(R.id.action_profile_edit);
        miCancel.setVisible(false);
        miSave.setVisible(false);
        miEdit.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                return true;
            case R.id.action_profile_edit:
                miCancel.setVisible(true);
                miSave.setVisible(true);
                miEdit.setVisible(false);
                mAdapter.EditMode = true;
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_profile_cancel:
                miCancel.setVisible(false);
                miSave.setVisible(false);
                miEdit.setVisible(true);
                mAdapter.EditMode = false;
                myDataset = getMyDataset(this); // reload previous data
                mAdapter.setDataset(myDataset);
                mAdapter.notifyDataSetChanged();
                return true;
            case R.id.action_profile_save:
                miCancel.setVisible(false);
                miSave.setVisible(false);
                miEdit.setVisible(true);
                mAdapter.EditMode = false;
                myDataset = mAdapter.getmDataset();
                saveMyDataset(myDataset,this); //save edited data
                mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void initMyDataset(){

        myDataset.clear();

        // create Button Add From:
        ProfileItem btnAddFrom = new ProfileItem();
        btnAddFrom.Text ="Receive SMS From (click to add):";
        btnAddFrom.Type = ItemType.ButtonAddFrom;
        myDataset.add(btnAddFrom);

        // create temporary item
        ProfileItem pi = new ProfileItem();
        pi.Enabled = true;
        pi.Text ="riga di testo";
        pi.Type = ItemType.From;
        myDataset.add(pi);

        // create Button Add Filter:
        ProfileItem btnFilter = new ProfileItem();
        btnFilter.Text ="Add a SMS text filter (click to add):";
        btnFilter.Type = ItemType.ButtonAddFilter;
        myDataset.add(btnFilter);

        // create Button Add To:
        ProfileItem btnAddTo = new ProfileItem();
        btnAddTo.Text ="Send SMS To (click to add):";
        btnAddTo.Type = ItemType.ButtonAddTo;
        myDataset.add(btnAddTo);
    }

    ArrayList<ProfileItem> searchMyDataset(ProfileItem piTemplate){
        // search inside myDataset
        ArrayList<ProfileItem> result = new ArrayList<ProfileItem>();

        for(ProfileItem pi: myDataset){
            if(pi.Type == piTemplate.Type &&
               pi.Text.matches(piTemplate.Text)
            )
                result.add(pi);
        }
        return result;
    }


    static ArrayList<ProfileItem> getMyDataset(Context context){

        String key = "ArrayList_ProfileItems_Key";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Gson gson = new Gson();
        String response=prefs.getString(key , "");

        ArrayList<ProfileItem> lstArrayList = gson.fromJson(response,
                new TypeToken<ArrayList<ProfileItem>>(){}.getType());

        if(lstArrayList == null)
            return new ArrayList<ProfileItem>();
        else
            return lstArrayList;
    }

    static void saveMyDataset(ArrayList<ProfileItem> alpi, Context context ){

        String key = "ArrayList_ProfileItems_Key";

        Gson gson = new Gson();

        SharedPreferences shref;
        SharedPreferences.Editor editor;
        shref = PreferenceManager.getDefaultSharedPreferences(context);

        String json = gson.toJson(alpi);

        editor = shref.edit();
        editor.remove(key).commit();
        editor.putString(key, json);
        editor.commit();
    }
}






