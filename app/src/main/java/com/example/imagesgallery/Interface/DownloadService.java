package com.example.imagesgallery.Interface;

import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DownloadService {
    @GET
    Call<ResponseBody> downloadFileFromUrl(@Url String fileUrl);
}
