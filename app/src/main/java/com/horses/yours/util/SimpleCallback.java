package com.horses.yours.util;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Brian Salvattore
 */
public class SimpleCallback<T> implements Callback<T> {

    @Override
    public void onResponse(Call<T> call, Response<T> response) {

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }
}
