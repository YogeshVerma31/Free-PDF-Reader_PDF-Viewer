package com.yvtechnologies.pdfreader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private SharedPreferences _prefs = null;
    private SharedPreferences.Editor _editor = null;

    public SharedPreference(Context context) {
        this._prefs = context.getSharedPreferences("PDFReader",
                Context.MODE_PRIVATE);
        this._editor = this._prefs.edit();
        this._editor.apply();
    }

    public void putUpload(boolean upload){
        _editor.putBoolean("upload", upload).apply();

    }

    public Boolean getUpload(){
        return _prefs.getBoolean("upload",false);
    }

    public void savePreferences(int count) {
        _editor.putInt("count", count).apply();
    }

    public void putLoad(boolean load){
        _editor.putBoolean("Load",load).apply();
    }

    public int getCount(){
        return _prefs.getInt("count",0);
    }

    public boolean getLoad(){
        return _prefs.getBoolean("Load",false);
    }
}
