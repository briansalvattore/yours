package com.horses.yours.business.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * @author Brian Salvattore
 */
@SuppressWarnings("WeakerAccess")
public interface ApiService {

    @GET
    Call<ResponseBody> downloadFile(
            @Url String url
    );
}
