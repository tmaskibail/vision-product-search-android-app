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


import static com.example.search.ProductSearchActivity.BITMAP_BYTES;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;

public class ObjectDetectorActivity extends AppCompatActivity {
    private static final String TAG = "ObjectDetectorActivity";
    private final int ACTIVITY_SELECT_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detector);

        Button galleryButton = findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, ACTIVITY_SELECT_IMAGE);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && ACTIVITY_SELECT_IMAGE == requestCode) {
            byte[] bytes = getBytes(data);
            Intent intent = new Intent(this, ProductSearchActivity.class);
            intent.putExtra(BITMAP_BYTES, bytes);
            startActivity(intent);
        }
    }

    @NonNull
    private byte[] getBytes(Intent data) {
        Bitmap bitmap = uriToBitmap(data.getData());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private Bitmap uriToBitmap(Uri selectedFileUri) {
        Bitmap bitMap = null;
        try (ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedFileUri, "r")) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            bitMap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return bitMap;
    }
}