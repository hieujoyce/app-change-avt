package com.hieujoyce.firstdemo.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.hieujoyce.firstdemo.R;
import com.hieujoyce.firstdemo.adapters.AvtListAdapter;
import com.hieujoyce.firstdemo.api.ApiService;
import com.hieujoyce.firstdemo.databinding.ActivityMainBinding;
import com.hieujoyce.firstdemo.models.ResponseGetListAvt;
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
    AvtListAdapter avtListAdapter;

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
        callGetAllAvt();
        binding.imgChangeAvt.setOnClickListener(view -> {
            avtListAdapter.setPo();
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
                        ApiService.apiService.getAllAvt().enqueue(new Callback<ResponseGetListAvt>() {
                            @Override
                            public void onResponse(Call<ResponseGetListAvt> call, Response<ResponseGetListAvt> response) {
                                ResponseGetListAvt res = response.body();
                                avtListAdapter.updateList(res.data);
                                binding.txtCoutAvt.setText("Ảnh (" + res.data.size() + ")");
                                mLoadingDialog.cancel();
                            }

                            @Override
                            public void onFailure(Call<ResponseGetListAvt> call, Throwable t) {
                                mLoadingDialog.cancel();
                                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ResponseUploadImage> call, Throwable t) {
                        mLoadingDialog.cancel();
                        Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        binding.imgSave.setOnClickListener(view -> {
            if(avtListAdapter.selectedPosition != -1) {
                DialogInterface.OnClickListener dialogImgSaveClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
                                String urli = avtListAdapter.data.get(avtListAdapter.selectedPosition).url;
                                Ed=sp.edit();
                                Ed.putString("avtUrl", urli);
                                Ed.commit();
                                Glide.with(MainActivity.this).load(urli).into(binding.imgAvt);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.dismiss();
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Bạn muốn chọn ảnh này làm ảnh đại diện?")
                        .setPositiveButton("Có", dialogImgSaveClickListener)
                        .setNegativeButton("Không", dialogImgSaveClickListener)
                        .show();
            }
        });

        binding.imgDelete.setOnClickListener(view -> {
            if(avtListAdapter.selectedPosition != -1) {
                DialogInterface.OnClickListener dialogImgSaveClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case DialogInterface.BUTTON_POSITIVE:
<<<<<<< HEAD
=======

>>>>>>> origin/master
                                callDeleteAvt(avtListAdapter.selectedPosition, avtListAdapter.data.get(avtListAdapter.selectedPosition).id);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.dismiss();
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Bạn muốn xóa ảnh này khỏi thư viện?")
                        .setPositiveButton("Có", dialogImgSaveClickListener)
                        .setNegativeButton("Không", dialogImgSaveClickListener)
                        .show();
            }
        });
    }

    private void callDeleteAvt(int po, String id) {
        mLoadingDialog.show();
        String urlj = sp.getString("avtUrl", null);
        String urli = avtListAdapter.data.get(avtListAdapter.selectedPosition).url;
        if(urli == urli) {
            Ed=sp.edit();
            Ed.putString("avtUrl", "");
            Ed.commit();
            Glide.with(MainActivity.this).load(R.drawable.default_avatar).into(binding.imgAvt);
        }
        ApiService.apiService.deleteAvt(id).enqueue(new Callback<ResponseGetListAvt>() {
            @Override
            public void onResponse(Call<ResponseGetListAvt> call, Response<ResponseGetListAvt> response) {
                binding.txtCoutAvt.setText("Ảnh (" + (avtListAdapter.data.size() - 1) + ")");
                avtListAdapter.removeAt(po);
                mLoadingDialog.cancel();
            }

            @Override
            public void onFailure(Call<ResponseGetListAvt> call, Throwable t) {
                mLoadingDialog.cancel();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void callGetAllAvt() {
        mLoadingDialog.show();
        ApiService.apiService.getAllAvt().enqueue(new Callback<ResponseGetListAvt>() {
            @Override
            public void onResponse(Call<ResponseGetListAvt> call, Response<ResponseGetListAvt> response) {
                ResponseGetListAvt res = response.body();
                //
                binding.listAvatar.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                binding.listAvatar.setHasFixedSize(true);
                avtListAdapter = new AvtListAdapter(MainActivity.this, res.data, binding);
                binding.listAvatar.setAdapter(avtListAdapter);
                binding.txtCoutAvt.setText("Ảnh (" + res.data.size() + ")");
                mLoadingDialog.cancel();
            }

            @Override
            public void onFailure(Call<ResponseGetListAvt> call, Throwable t) {
                mLoadingDialog.cancel();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
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