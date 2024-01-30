package com.example.blockapp;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("getAllBlackListApps")
    Call<BlackListResponse> getBlackListApps();
}

