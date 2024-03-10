package  com.example.imagesgallery.Activity;


import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.Adapter.TagAdapter;
import com.example.imagesgallery.R;
import com.example.imagesgallery.ml.MobilenetV110224Quant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;

public class EditImageTagActivity extends AppCompatActivity {
    Button btnAddTag;
    Button btnAutoAddTag;

    ListView listView;
    MainActivity mainActivity;
    TagAdapter tagAdapter;
    ArrayList<String> arrayListTag;
    String imagePath ="";
    String[] labels;
    Bitmap bitmap;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image_tag);

        imagePath = getIntent().getStringExtra("ImagePath");
        bitmap = BitmapFactory.decodeFile(imagePath);
        labels = new String[1001];

        int count=0;
        BufferedReader bufferedReader=null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line = bufferedReader.readLine();

            while (line!=null) {
                labels[count] =line;
                count++;
                line = bufferedReader.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnAddTag = (Button) findViewById(R.id.btnAddTag);
        btnAutoAddTag = (Button) findViewById(R.id.btnAutoAddTag);



        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTagDialog();
            }
        });

        btnAutoAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objectDetection();
            }
        });

        listView = (ListView) findViewById(R.id.listViewTags);
        arrayListTag = new ArrayList<>();
        getAllTagOfImage();
        tagAdapter = new TagAdapter(EditImageTagActivity.this,R.layout.custom_tag_view,arrayListTag, imagePath);
        listView.setAdapter(tagAdapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (tagAdapter!=null) {
            tagAdapter.notifyDataSetChanged();
        }

    }


    private void getAllTagOfImage() {
        if (arrayListTag==null) {
            return;
        }
        arrayListTag.clear();
        String[] args = {imagePath};
        Cursor cursor = mainActivity.db.rawQuery("SELECT * FROM Image_Tag WHERE Image_Path = ?",args);
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
                int tagColumnIndex = cursor.getColumnIndex("Tag");
                String tagValue = cursor.getString(tagColumnIndex);
                arrayListTag.add(tagValue);
        }
    }

    Dialog addTagDialog;
    Button btnAddTagDialog;
    Button btnCancelAddTagDialog;
    EditText editTextAddTag;

    private void showAddTagDialog() {
        addTagDialog = new Dialog(EditImageTagActivity.this);
        addTagDialog.setContentView(R.layout.add_tag_dialog);

        addTagDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addTagDialog.getWindow().setGravity(Gravity.CENTER);

        btnAddTagDialog = (Button) addTagDialog.findViewById(R.id.buttonAddTagDialog);
        btnCancelAddTagDialog = (Button) addTagDialog.findViewById(R.id.buttonCancelAddTagDialog);
        editTextAddTag = (EditText) addTagDialog.findViewById(R.id.editTextAddTag);

        btnAddTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tagValue = editTextAddTag.getText().toString();
                addTag(tagValue);
            }
        });

        btnCancelAddTagDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTagDialog.dismiss();
            }
        });

        addTagDialog.show();

    }

    private void addTag(String tagValue) {
        ContentValues values = new ContentValues();
        values.put("Image_Path",imagePath);
        values.put("Tag",tagValue);
        mainActivity.db.insert("Image_Tag",null,values);
        //arrayListTag.add(tagValue);
        tagAdapter.add(tagValue);
        tagAdapter.notifyDataSetChanged();
        addTagDialog.dismiss();

    }

    private void objectDetection() {
        try {
            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(EditImageTagActivity.this);

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{
                    1,224,224,3
            }, DataType.UINT8);

            bitmap = Bitmap.createScaledBitmap(bitmap, 224,224, true);
            inputFeature0.loadBuffer(TensorImage.fromBitmap(bitmap).getBuffer());


            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);

            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            int resultIndex = getMax(outputFeature0.getFloatArray());
            String result = labels[resultIndex];
            /*Toast.makeText(
                    EditImageTagActivity.this,
                    result,
                    Toast.LENGTH_SHORT
            ).show();*/

            addTag(result);

            model.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getMax(float[] arr) {
        int max=0;

        for (int i=0; i<arr.length; i++) {
            if (arr[i] >arr[max]) {
                max = i;
            }
        }

        return max;
    }
}
