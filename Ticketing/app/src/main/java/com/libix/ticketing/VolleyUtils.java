package com.libix.ticketing;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Consumer;

public class VolleyUtils {

    public static JsonArrayRequest jsonArrayGetRequest(String url, Consumer<? super JSONArray> onResponseCall){

        return new JsonArrayRequest(Request.Method.GET, url, null, response -> onResponseCall.accept(response), error -> {
            Log.d("URLGetRequest", "Unable to receive JSON response from server");
            Log.d("URLGetRequest", error.toString());
        });
    }

    public static JsonObjectRequest jsonObjectGetRequest(String url, Consumer<? super JSONObject> onResponseCall, Consumer<? super VolleyError> onErrorCall){

        return new JsonObjectRequest(Request.Method.GET, url, null, response -> onResponseCall.accept(response), error -> onErrorCall.accept(error));
    }

    public static JsonArrayRequest jsonArrayPostRequest(String url, JSONArray jsonArray, Consumer<? super JSONArray> onResponseCall, Consumer<? super VolleyError> onErrorCall){

        return new JsonArrayRequest(Request.Method.POST, url, jsonArray, response -> onResponseCall.accept(response), error -> onErrorCall.accept(error));
    }

    public static JsonObjectRequest jsonObjectPostRequest(String url, JSONObject jsonObject, Consumer<? super JSONObject> onResponseCall, Consumer<? super VolleyError> onErrorCall){

        return new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> onResponseCall.accept(response), error -> onErrorCall.accept(error));
    }
}
