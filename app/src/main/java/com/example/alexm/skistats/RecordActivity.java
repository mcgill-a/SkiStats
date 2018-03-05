package com.example.alexm.skistats;

import android.icu.text.AlphabeticIndex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class RecordActivity extends AppCompatActivity {


    private ImageButton recordImageButton;
    private ImageButton pauseImageButton;
    private ImageButton cancelImageButton;
    private ImageButton submitImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        recordImageButton = (ImageButton)findViewById(R.id.recordImageButton);
        pauseImageButton = (ImageButton)findViewById(R.id.pauseImageButton);
        cancelImageButton = (ImageButton)findViewById(R.id.cancelImageButton);
        submitImageButton = (ImageButton)findViewById(R.id.submitImageButton);

        recordImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
            }
        });

        pauseImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Paused Recording", Toast.LENGTH_SHORT).show();
            }
        });

        cancelImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Cancelled Recording", Toast.LENGTH_SHORT).show();
            }
        });

        submitImageButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Toast.makeText(RecordActivity.this, "Submitted Recording", Toast.LENGTH_SHORT).show();
            }
        });


    }
}
