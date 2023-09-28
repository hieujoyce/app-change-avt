package com.hieujoyce.firstdemo.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hieujoyce.firstdemo.models.ResponseGetListAvt;
import com.hieujoyce.firstdemo.models.ResponseUploadImage;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface ApiService {
    public static final String URL = "https://upload-image-cloud.vercel.app/";
    Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd HH:mm:ss").create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(ApiService.class);

    @Multipart
    @POST("upload")
    Call<ResponseUploadImage> saveImage(@Part MultipartBody.Part image);
    @GET("list")
    Call<ResponseGetListAvt> getAllAvt();

    @GET("delete-image/{idImg}")
    Call<ResponseGetListAvt> deleteAvt(@Path("idImg") String idImg);
}
