package com.example.imagesgallery.Activity;

import static android.os.Environment.MEDIA_MOUNTED;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.R;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class DetailImageActivity extends AppCompatActivity {
    private int imagePosition=-1;
    private String imageLink="...";
    private long dateTaken =0; //="dd/mm/yyyy";
    private String imageExif="";
    Uri imageUri=null;
    TextView txtViewLink;
    TextView txtViewDate;
    TextView txtViewImageExif;
    //TextView txtViewTag;
    String extensionName = "null";
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_image);
        Bundle bundle = getIntent().getExtras();

        imagePosition = bundle.getInt("position");
        //Toast.makeText(getApplicationContext(),"Detail position: "+imagePosition,Toast.LENGTH_SHORT ).show();
        txtViewLink = (TextView) findViewById(R.id.txtViewLink);
        txtViewDate =(TextView)findViewById(R.id.txtViewDate);
        txtViewImageExif =(TextView)findViewById(R.id.txtViewExif);
        //txtViewTag = (TextView)findViewById(R.id.txtViewTag);
        loadImageInformation();
        txtViewLink.setText(imageLink);

        //Toast.makeText(this,"detail: "+dateTaken,Toast.LENGTH_LONG).show();
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        txtViewDate.setText(formattedDate);
        //getImageExif(imageUri);
        txtViewImageExif.setText(imageExif);

    }

    private void loadImageInformation()
    {
        boolean SDCard = Environment.getExternalStorageState().equals(MEDIA_MOUNTED);

        if (SDCard) {
            final String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN,MediaStore.Images.ImageColumns.ORIENTATION};
            final String order = MediaStore.Images.Media.DATE_TAKEN + " DESC";
            //Log.d("test","6");
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, order);

            cursor.moveToPosition(imagePosition);
            int columnIndexData = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int columnIndexDateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            //int orientationColumnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
            imageLink = cursor.getString(columnIndexData);
            dateTaken = cursor.getLong(columnIndexDateTaken);
            int columnIndexImageID=cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);
            long imageID = cursor.getLong(columnIndexImageID);
            imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,String.valueOf(imageID));
            //Toast.makeText(getApplicationContext(),imageUri.toString(),Toast.LENGTH_SHORT).show();
            getImageExif(imageLink);
            cursor.close();
            getImageExtensionName(imageLink);

        }
    }
    private void getImageExif(String imageLink) {
        ExifInterface exifInterface = null;
        File imageFile=null;
        try {
            exifInterface = new ExifInterface(imageLink);
            imageFile = new File(imageLink);

        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sb= new StringBuilder();
        if (imageFile!=null) {
            long imageSizeInBytes = imageFile.length();
            long imageSizeInKB = imageSizeInBytes / 1024;
            sb.append("Size: "+imageSizeInKB+" KB\n");
        }
        if (exifInterface!=null) {
            sb.append("Width x Length: "+exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
            sb.append("x"+exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
            sb.append("\nISO:"+exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS));
            sb.append("\nExposure time: "+exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
            sb.append("\nF-number: "+exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER));
            sb.append("\nFocal length: "+exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
            sb.append("\nCamera manufacturer: "+exifInterface.getAttribute(ExifInterface.TAG_MAKE));
            sb.append("\nCamera model: "+exifInterface.getAttribute(ExifInterface.TAG_MODEL));
            sb.append("\nAuthor: "+exifInterface.getAttribute(ExifInterface.TAG_ARTIST));
            sb.append("\nSubject location: "+exifInterface.getAttribute(ExifInterface.TAG_SUBJECT_LOCATION));
            sb.append("\nLatitude: "+ exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            sb.append("\nLongitude: "+ exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
            imageExif+=sb.toString();


                // Fallback to getting the extension from the file path


            /*imageExif+="x"+exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER);

            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_MAKE);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_ARTIST);
            imageExif+=" | "+exifInterface.getAttribute(ExifInterface.TAG_SUBJECT_LOCATION);
            imageExif+=" | "+ exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            imageExif+=" | "+ exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);*/
        } else {
            Toast.makeText(getApplicationContext(),"Fail to load exif of image",Toast.LENGTH_SHORT).show();
        }


    }
    private void getImageExtensionName(String imageLink) {
        File imageFile=null;
        try {

            imageFile = new File(imageLink);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (imageFile!=null) {
            int dotIndex = imageFile.getAbsolutePath().lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < imageLink.length() - 1) {
                extensionName = imageFile.getAbsolutePath().substring(dotIndex + 1);
            }
            Toast.makeText(getApplicationContext(), "Type: " + extensionName, Toast.LENGTH_LONG).show();
        }
    }


}
