package com.yvtechnologies.pdfreader.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.os.Bundle;

import com.yvtechnologies.pdfreader.R;

public class PdfSearchActivity extends AppCompatActivity {
    private AppCompatImageView iv_back;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pdf_search);
        initView();
    }

    private void initView() {

    }
}