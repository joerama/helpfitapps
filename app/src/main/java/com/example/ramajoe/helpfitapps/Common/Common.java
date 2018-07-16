package com.example.ramajoe.helpfitapps.Common;

import com.example.ramajoe.helpfitapps.Model.User;


/**
 * Created by Diyan on 11/02/2018.
 */

public class Common {
    public static final int PICK_IMAGE_REQUEST = 9999; //rikues avatar di editProfileActivity.java
    public static String uid = "";
    //
    public static User currentUser;


    //Untuk auto login
    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    /*public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }*/
}
