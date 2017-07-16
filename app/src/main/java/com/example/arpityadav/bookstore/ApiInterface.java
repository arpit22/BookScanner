package com.example.arpityadav.bookstore;

import com.example.arpityadav.bookstore.Model.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Arpit Yadav on 7/12/2017.
 */

public interface ApiInterface {
    @GET("books/v1/volumes")
    Call<Example> getBook(@Query("q") String isbn, @Query("key") String key );
}
