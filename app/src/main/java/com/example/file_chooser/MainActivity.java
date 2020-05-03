package com.example.file_chooser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private Button btn;
    private TextView text;
    private ImageView img;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn=(Button)findViewById(R.id.btn);
        text=(TextView)findViewById(R.id.text);
        img=(ImageView)findViewById(R.id.img);
        progressDialog =new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please wait...");



        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
                return;
            }

        }
        enable_button();
    }

    private void enable_button() {

                btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File to Upload"),
                            10);
                } catch (android.content.ActivityNotFoundException ex) {

                    Toast.makeText(MainActivity.this, "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK) {


                    Uri uri = data.getData();

                    String path = FileChooser.getPath(getApplicationContext(),uri);
                    final File file=new File(path);
                    final String fpath=file.getAbsolutePath();
                    final String content_type= getMimeType(file.getPath());
                    //img.setImageURI(Uri.fromFile(file));
                    text.setText(file.getAbsolutePath());

                    Thread t=new Thread(new Runnable() {
                        @Override
                        public void run() {

                    OkHttpClient client = new OkHttpClient();
                    if(content_type!=null) {
                        progressDialog.show();
                        RequestBody file_body = RequestBody.create(MediaType.get(content_type), file);

                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("type", content_type)
                                .addFormDataPart("uploaded_file", fpath.substring(
                                        fpath.lastIndexOf("/") + 1), file_body)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://raykibul.com/upload/upload.php")
                                .post(requestBody)
                                .build();
                        try {
                            Response response = client.newCall(request).execute();
                            if (!response.isSuccessful() && response != null) {
                                throw new IOException("Error : " + response);
                            }
                            progressDialog.dismiss();
                            //Toast.makeText(MainActivity.this,"Done Uploading",Toast.LENGTH_SHORT);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                        }
                    });
                    t.start();


                }

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            enable_button();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},100);
            }
        }
    }

    private String getMimeType(String path) {
        String extension= MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }


}

