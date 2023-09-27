package com.hieujoyce.firstdemo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.hieujoyce.firstdemo.R;
import com.hieujoyce.firstdemo.api.ApiService;
import com.hieujoyce.firstdemo.databinding.ActivityMainBinding;
import com.hieujoyce.firstdemo.utils.LoadingDialog;
import com.hieujoyce.firstdemo.utils.RealPathUtil;
import com.hieujoyce.firstdemo.models.ResponseUploadImage;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Uri mUri;
    private LoadingDialog mLoadingDialog;
    SharedPreferences sp;
    SharedPreferences.Editor Ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doing();
    }

    ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) return;
                        Uri uri = data.getData();
                        mUri = uri;
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            binding.imgAvt.setImageBitmap(bitmap);
                            binding.btnSave.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            Log.e("Hieu Joyce", e.getMessage());
                        }
                    }
                }
            }
    );

    private void doing() {
        sp = getSharedPreferences("Avatar", MODE_PRIVATE);
        String avtUrl = sp.getString("avtUrl", null);
        if(avtUrl == null || avtUrl == "") {
            binding.imgAvt.setImageResource(R.drawable.default_avatar);
        } else {
            Glide.with(MainActivity.this).load(avtUrl).into(binding.imgAvt);
        }
        mLoadingDialog = new LoadingDialog(this);
        binding.imgChangeAvt.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                handleChoseImage();
                return;
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                handleChoseImage();
            } else {
                String[] pers = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(pers, 10);
            }
        });

        binding.btnSave.setOnClickListener(view -> {
            if (mUri != null) {
                mLoadingDialog.show();
                String pathImg = RealPathUtil.getRealPath(this, mUri);
                File f = new File(pathImg);
                RequestBody requestBodyAvt = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                MultipartBody.Part multipartBodyAvt = MultipartBody.Part.createFormData("image", f.getName(), requestBodyAvt);
                ApiService.apiService.saveImage(multipartBodyAvt).enqueue(new Callback<ResponseUploadImage>() {
                    @Override
                    public void onResponse(Call<ResponseUploadImage> call, Response<ResponseUploadImage> response) {
                        ResponseUploadImage res = response.body();
                        if (res.url != null && res.url != "") {
                            mUri = null;
                            Glide.with(MainActivity.this).load(res.url).into(binding.imgAvt);
                            binding.btnSave.setVisibility(View.GONE);
                            Ed=sp.edit();
                            Ed.putString("avtUrl", res.url);
                            Ed.commit();
                        }
                        mLoadingDialog.cancel();
                    }

                    @Override
                    public void onFailure(Call<ResponseUploadImage> call, Throwable t) {
                        mLoadingDialog.cancel();
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleChoseImage();
        }
    }

    private void handleChoseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }


}