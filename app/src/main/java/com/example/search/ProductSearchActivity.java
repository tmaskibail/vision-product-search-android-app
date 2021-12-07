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

package com.example.search;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.search.model.ProductSearchResult;
import com.example.search.util.HttpSearchUtility;

import org.json.JSONException;

public class ProductSearchActivity extends AppCompatActivity {

    public static final String BITMAP_BYTES = "BITMAP_BYTES";
    private static final String TAG = "ProductSearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);

        byte[] bytes = getIntent().getByteArrayExtra(BITMAP_BYTES);
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        ImageView imageView = findViewById(R.id.search_image);
        imageView.setImageBitmap(imageBitmap);

        try {
            productSearch(imageBitmap);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void productSearch(Bitmap imageBitmap) throws JSONException {

        LinearLayout mainLayout = findViewById(R.id.linear_main);

        HttpSearchUtility httpSearchUtility = new HttpSearchUtility();
        httpSearchUtility.searchForProducts(imageBitmap, this).addOnSuccessListener(productSearchResults -> {
            for (ProductSearchResult productSearchResult : productSearchResults) {
                System.out.println(productSearchResult.toString());

                //TODO: Clean this mess and use dynamic layouts
                LinearLayout linearLayout = new LinearLayout(ProductSearchActivity.this);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                mainLayout.addView(linearLayout);

                ImageView productImageView = new ImageView(ProductSearchActivity.this);
                LinearLayout.LayoutParams productImageViewParams = new LinearLayout.LayoutParams(160, 200);
                productImageViewParams.weight = 0.0f;
                productImageViewParams.gravity = Gravity.LEFT;
                productImageView.setLayoutParams(productImageViewParams);
                productImageView.setAdjustViewBounds(true);
                //                productImageView.setImageResource(R.drawable.latte);
                //TODO: Change this to display images of each product
                Glide.with(ProductSearchActivity.this).load(productSearchResult.getImageUri()).into(productImageView);
                linearLayout.addView(productImageView);

                LinearLayout textLinearLayout = new LinearLayout(ProductSearchActivity.this);
                textLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
                textLinearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(textLinearLayout);

                TextView productNameTextView = new TextView(ProductSearchActivity.this);
                productNameTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
                productNameTextView.setText(productSearchResult.getName());
                productNameTextView.setPadding(0, 10, 0, 0);
                textLinearLayout.addView(productNameTextView);

                TextView labelsTextView = new TextView(ProductSearchActivity.this);
                labelsTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
                labelsTextView.setText(productSearchResult.getLabel());
                labelsTextView.setPadding(0, 10, 0, 0);
                textLinearLayout.addView(labelsTextView);

                TextView scoreTextView = new TextView(ProductSearchActivity.this);
                scoreTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0));
                scoreTextView.setText(String.valueOf(productSearchResult.getScore()));
                scoreTextView.setPadding(0, 10, 0, 0);
                textLinearLayout.addView(scoreTextView);
            }
        }).addOnFailureListener(e -> Toast.makeText(ProductSearchActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}