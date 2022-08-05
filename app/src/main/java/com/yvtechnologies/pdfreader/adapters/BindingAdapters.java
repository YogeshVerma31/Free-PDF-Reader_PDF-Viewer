package com.yvtechnologies.pdfreader.adapters;


import android.text.format.Formatter;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;


import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class BindingAdapters {
    @BindingAdapter("android:loadText")
    public static void setText(TextView textView, File file){
        Date lastModeified =new Date(file.lastModified());
        String stringDate = DateFormat.getDateInstance().format(lastModeified);
        textView.setText(stringDate+" - "+ Formatter.formatShortFileSize(textView.getContext(),file.length()));
    }

}
