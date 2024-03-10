package com.example.imagesgallery.Activity;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.R;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

public class ScanQRCodeActivity extends AppCompatActivity {
    Button btnCopyToClipBoard;
    Button btnReturn;
    TextView txtViewQRResult;
    Uri imageUri;
    String imagePath=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);
        imagePath = getIntent().getStringExtra("ImagePath");
        imageUri = getImageUriFromPath(imagePath);
        btnCopyToClipBoard = (Button) findViewById(R.id.btnCopyToClipBoardQR);
        btnReturn = (Button) findViewById(R.id.btnReturnQR);
        txtViewQRResult = (TextView) findViewById(R.id.txtViewQRResult);
        //scanQRCode();
        String result = detectBarCode(BitmapFactory.decodeFile(imagePath));
        if (result!=null) {
            txtViewQRResult.setText(result);
        }
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public Uri getImageUriFromPath(String imagePath) {
        Uri imageUri = null;
        String[] projection = { MediaStore.Images.Media._ID };
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = { imagePath };
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            long imageId = cursor.getLong(columnIndex);
            imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
            cursor.close();
        }
        return imageUri;
    }
    public String detectBarCode(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        Reader reader = new QRCodeReader();
        try {
            Result result = reader.decode(new BinaryBitmap(new HybridBinarizer(source)));
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (ChecksumException e) {
            e.printStackTrace();
            return null;
        } catch (FormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void scanQRCode() {
        try {
             InputStream imageStream = getContentResolver().openInputStream(imageUri);
             Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            try {
                Bitmap bMap = selectedImage;

                String contents = null;

                int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];

                bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());



                LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);

                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));



                Reader reader = new MultiFormatReader();

                Result result = reader.decode(bitmap);

                contents = result.getText();

                Toast.makeText(getApplicationContext(),contents, Toast.LENGTH_LONG).show();
                txtViewQRResult.setText(contents);
            }
            catch (Exception e){

                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
