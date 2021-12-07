/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.search.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.search.model.ProductLabel;
import com.example.search.model.ProductSearchResponse;
import com.example.search.model.ProductSearchResult;
import com.example.search.model.Result;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HttpSearchUtility {
    private static final String TAG = "HttpSearchUtility";
    private static final int VISION_API_PRODUCT_MAX_RESULT = 5;
    private static final String VISION_API_URL = "https://vision.googleapis.com/v1";

    private static final String VISION_API_PROJECT_ID = "";
    private static final String VISION_API_LOCATION_ID = "europe-west1";
    private static final String VISION_API_PRODUCT_SET_ID = "";
    private static final String VISION_API_KEY = "";

    private static String getRequestJsonBody(String base64Image) {
        return "{  \"requests\": [{\"image\": {\"content\": \"" + base64Image + "\" },\"features\": [{ \"type\": \"PRODUCT_SEARCH\",\"maxResults\": " + VISION_API_PRODUCT_MAX_RESULT + "}], \"imageContext\": { \"productSearchParams\": {\"productSet\": \"projects/" + VISION_API_PROJECT_ID + "/locations/" + VISION_API_LOCATION_ID + "/productSets/" + VISION_API_PRODUCT_SET_ID + "\",\"productCategories\": [\"homegoods-v2\"]}}}]}";
    }

    private static List<ProductSearchResult> extractProductResult(String responseString) {
        Log.i(TAG, responseString);
        List<ProductSearchResult> searchResults = new ArrayList<>();

        //TODO: chnage this to a Java model
        if (!responseString.contains("error")) {
            // Extract all but image URI from the response string
            ProductSearchResponse productSearchResponse = new Gson().fromJson(responseString, ProductSearchResponse.class);

            for (com.example.search.model.Response response : productSearchResponse.getResponses()) {
                List<Result> results = response.getProductSearchResults().getResults();
                for (Result result : results) {
                    ProductSearchResult p = new ProductSearchResult();
                    p.setScore(result.getScore());
                    p.setImageId(result.getImage());
                    p.setName(result.getProduct().getDisplayName());
                    p.setLabel(extractLabels(result.getProduct().getProductLabels()));

                    Log.d(TAG, p.toString());
                    searchResults.add(p);
                }
            }
        }
        return searchResults;
    }

    private static String extractLabels(List<ProductLabel> productLabels) {
        StringBuilder sb = new StringBuilder();
        for (ProductLabel label : productLabels) {
            sb.append(label.getKey()).append(" - ").append(label.getValue());
        }
        return sb.toString();
    }


    private static String convertToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public Task<List<ProductSearchResult>> searchForProducts(Bitmap imageBitmap, Context context) throws JSONException {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        TaskCompletionSource<List<ProductSearchResult>> apiSource = new TaskCompletionSource<>();
        Task<List<ProductSearchResult>> apiTask = apiSource.getTask();

        String url = VISION_API_URL + "/images:annotate?key=" + VISION_API_KEY;
        Log.i(TAG, url);
        requestQueue.add(new JsonObjectRequest(
                Request.Method.POST,
                url,
                new JSONObject(getRequestJsonBody(convertToBase64(imageBitmap))),
                response -> {
                    // Parse the API JSON response to a list of ProductSearchResult object
                    List<ProductSearchResult> matchingProducts = extractProductResult(response.toString());

                    List<Task<ProductSearchResult>> fetchReferenceImageTasks = matchingProducts.stream()
                            .map(getUri(requestQueue))
                            .collect(Collectors.toList());

                    Tasks.whenAllComplete(fetchReferenceImageTasks)
                            .addOnSuccessListener(tasks -> apiSource.setResult(matchingProducts))
                            .addOnFailureListener(apiSource::setException);
                },
                error -> {
                    Log.e(TAG, error.toString());
                    apiSource.setException(error);
                }
        ).setShouldCache(false));

        return apiTask;
    }

    @NonNull
    private Function<ProductSearchResult, Task<ProductSearchResult>> getUri(RequestQueue requestQueue) {
        return searchResult -> {

            TaskCompletionSource<ProductSearchResult> source = new TaskCompletionSource<>();
            Task<ProductSearchResult> task = source.getTask();

            StringRequest stringRequest = new CustomStringRequest(
                    Request.Method.GET,
                    VISION_API_URL + "/" + searchResult.getImageId() + "?key=" + VISION_API_KEY,
                    response1 -> {
                        try {
                            Log.d(TAG, response1);
                            JSONObject responseJson = new JSONObject(response1);
                            String gcsUri = responseJson.getString("uri");
                            Log.i(TAG, gcsUri);
                            String httpUri = gcsUri.replace("gs://", "https://storage.googleapis.com/");
                            searchResult.setImageUri(httpUri);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        source.setResult(searchResult);
                    },
                    error -> {
                        Log.e(TAG, error.toString());
                        source.setException(error);
                    }
            );

            requestQueue.add(stringRequest);

            return task;
        };
    }
}
