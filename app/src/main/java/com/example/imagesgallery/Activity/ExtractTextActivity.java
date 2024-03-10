package  com.example.imagesgallery.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.imagesgallery.R;

public class ExtractTextActivity extends AppCompatActivity {
    Button btnCopyToClipBoard;
    Button btnReturn;
    TextView txtViewExtractedMsg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extract_text_activity);

        btnReturn = (Button) findViewById(R.id.btnReturn);
        btnCopyToClipBoard = (Button) findViewById(R.id.btnCopyToClipBoard);
        txtViewExtractedMsg = (TextView) findViewById(R.id.txtViewExtractedMsg);

        String extractedText = getIntent().getStringExtra("extracted text");
        txtViewExtractedMsg.setText(extractedText);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
            }
        });

        btnCopyToClipBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scannedText = txtViewExtractedMsg.getText().toString();
                copyToClipBoard(scannedText);
            }
        });

    }

    private void copyToClipBoard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("scanned text", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(ExtractTextActivity.this, "Copied to Clipboard", Toast.LENGTH_LONG).show();
    }
}