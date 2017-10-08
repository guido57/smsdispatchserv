package com.guido.smsdispatchserv;

/**
 * Created by Guido on 26/08/2017.
 */

public class ProfileItem{
    boolean Enabled;
    int Type;
    String Text;

    ProfileItem(boolean enabled,int  itemtype, String text){
        Enabled = enabled;
        Type = itemtype;
        Text = text;
    }

    ProfileItem(){
        boolean Enabled = true;
        int Type = new ItemType().From; // From To Button Header Filter
        String Text = ""; // }
    }
}