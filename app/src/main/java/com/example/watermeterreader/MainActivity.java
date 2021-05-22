package com.example.watermeterreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;



import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    public EditText mReading;
    public ImageView mImage;
    public EditText meterId;

    DatabaseManager db;

    private static final int camera_request_code = 200;
    private static final int storage_request_code = 400;
    private static final int image_from_gallery_code = 1000;
    private static final int image_from_camera_code = 1001;

    String[] cameraPermission;
    String[] storagePermission;

    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        mReading = findViewById(R.id.reading);
        mImage = findViewById(R.id.image);
        meterId = findViewById(R.id.meterId);

        //camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //database
        db = new DatabaseManager(this);

    }

    //actionbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle actionbar clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.addImage){
            displayImageImportDialog();
        }

        if(id == R.id.saveItem){
            try{
                int meterIdValue = parseInt(meterId.getText().toString());
                Float meterReadingValue = Float.parseFloat(mReading.getText().toString());

                Boolean checkSaveData = db.saveMeterData(meterIdValue, meterReadingValue);
                if (checkSaveData) {
                    Toast.makeText(MainActivity.this, "Details saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Sorry, Please try again!", Toast.LENGTH_SHORT).show();
                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, "Please, Fill all the details.", Toast.LENGTH_SHORT).show();
            }
        }

        if(id == R.id.deleteItem){
            int meterIdValue = parseInt(meterId.getText().toString());
            Boolean checkDeleteData = db.deleteMeterData(meterIdValue);
            if(checkDeleteData)
                Toast.makeText(MainActivity.this, "Details Deleted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Sorry, Please try again!", Toast.LENGTH_SHORT).show();
        }

        if(id == R.id.viewItem) {
            Cursor res = db.viewData();
            if (res.getCount() == 0) {
                Toast.makeText(MainActivity.this, "No details", Toast.LENGTH_SHORT).show();
            }
            else{
                StringBuffer strBuffer = new StringBuffer();
                while (res.moveToNext()) {
                    strBuffer.append("Meter ID :" + res.getString(0) + "\n");
                    strBuffer.append("Meter Reading :" + res.getString(1) + "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Meter Entries");
                builder.setMessage(strBuffer.toString());
                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayImageImportDialog() {
        //Item to display
        String[] items = {" Camera", " Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //set Title
        dialog.setTitle("Select from");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    //camera option selected
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        openCamera();
                    }
                }
                if(which == 1){
                    //gallery option selected
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        openGallery();
                    }
                }
            }
        });
        dialog.create().show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, image_from_gallery_code);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(camIntent,image_from_camera_code);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, storage_request_code);
    }

    private boolean checkStoragePermission() {
        boolean res = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, camera_request_code);
    }

    private boolean checkCameraPermission() {
        boolean res = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean res1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return res && res1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case camera_request_code:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageWriteAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageWriteAccepted){
                        openCamera();
                    }
                    else{
                        Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case storage_request_code:
                if(grantResults.length > 0) {
                    boolean storageWriteAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageWriteAccepted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == image_from_gallery_code) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == image_from_camera_code) {
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult res = CropImage.getActivityResult(data);
            Uri resUri = res.getUri();
            mImage.setImageURI(resUri);



            BitmapDrawable bitmapDrawable = (BitmapDrawable) mImage.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

            if (!recognizer.isOperational()) {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            } else {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = recognizer.detect(frame);
                StringBuilder strBld = new StringBuilder();
                for (int i = 0; i < items.size(); i++) {
                    TextBlock myItem = items.valueAt(i);
                    strBld.append(myItem.getValue());
                }
                mReading.setText(strBld.toString());
            }
        }
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

}