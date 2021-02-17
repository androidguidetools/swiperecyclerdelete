package com.startupinfosystem.swipedeleterecyclerview.api;


import com.startupinfosystem.swipedeleterecyclerview.model.MenuModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    @GET("json/menu.json")
    Call<ResponseBody> getMenu();
}
