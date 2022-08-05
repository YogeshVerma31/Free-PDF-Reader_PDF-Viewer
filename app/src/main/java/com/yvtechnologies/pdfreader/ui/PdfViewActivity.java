package com.yvtechnologies.pdfreader.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.yvtechnologies.pdfreader.R;
import com.yvtechnologies.pdfreader.adapters.PdfDocumentAdapter;
import com.yvtechnologies.pdfreader.utils.Constants;
import com.yvtechnologies.pdfreader.utils.SharedPreference;
import com.yvtechnologies.pdfreader.utils.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PdfViewActivity extends AppCompatActivity {

    private Toolbar tlb_pdfview;
    private PDFView pdfView;
    private String path;
    private BottomSheetDialog pdfBottomSheetDialog;
    private boolean switchEnable = false;
    private InterstitialAd interstitialAd;
    private final String TAG = PdfViewActivity.class.getSimpleName();
    private SharedPreference sharedPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pdf_view);
        AudienceNetworkAds.initialize(this);
        initView();
        setSupportActionBar(tlb_pdfview);




        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }

        };

        Log.d( "onCreate: ",sharedPreference.getCount()+"");

        if (sharedPreference.getUpload()){
            int count = sharedPreference.getCount();
            if (count%5==0){
                count++;
                Log.d( "onCreate: ",count+"");
                sharedPreference.savePreferences(count);
                interstitialAd.loadAd(
                        interstitialAd.buildLoadAdConfig()
                                .withAdListener(interstitialAdListener)
                                .build());
                if (interstitialAd.isAdLoaded()){
                    interstitialAd.show();
                }

            }else{
                count++;
                Log.d( "onCreate: ",count+"");
                sharedPreference.savePreferences(count);
            }
        }else{
            sharedPreference.putUpload(true);
            Log.d( "onCreate: ","else");
            sharedPreference.savePreferences(0);
        }

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            if (getIntent().getData() != null) {
                path = getIntent().getDataString();
                setUpToolbar(getFilePathFromURI(this,getIntent().getData()));
                displayPdfwithUri(getIntent().getData());
            }
        } else {
            path = getIntent().getStringExtra(Constants.PDFPATH);
            displayPdfwithCheck(path);
        }


    }

    public static String getFilePathFromURI(Context context, Uri contentUri) {
        String fileName = new File(String.valueOf(contentUri)).getName();
        if (!TextUtils.isEmpty(fileName)) {
            File copyFile = new File( fileName+".jpg");
            return new File(copyFile.getAbsolutePath()).getName();
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.pdfactivity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_share:
                fileshareClick(path);
                break;
            case R.id.ic_menu_print:
                printPdf();
                break;
            case R.id.ic_menu_more:
                createMoreBottomSheet();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void fileshareClick(String path){
        Uri parse = Uri.parse(path);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("Pdf/*");
        intent.putExtra("android.intent.extra.STREAM", parse);
        startActivity(Intent.createChooser(intent, "Share Pdf..."));
        return;
    }

    private void displayPdfwithCheck(String path) {
        if (checkEncrypted(path)) {
            createDialog(path);
        } else {
            displayPdf(path, null,false,false);
        }
    }

    private void createDialog(String path) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_dailog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText edt_password = dialog.findViewById(R.id.edt_password);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel_dialog);
        TextView tv_ok = dialog.findViewById(R.id.tv_cancel_ok);
        dialog.show();
        tv_cancel.setOnClickListener(v -> dialog.dismiss());

        tv_ok.setOnClickListener(v -> {
            displayPdf(path, edt_password.getText().toString(),false,false);
            dialog.dismiss();
        });
    }

    private void printPdf(){
        PrintManager printManager=(PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(
                    this, path);

            printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
        }
         catch (Exception e)
        {

        }
    }

    private void createMoreBottomSheet(){

        pdfBottomSheetDialog = new BottomSheetDialog(PdfViewActivity.this,R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.item_details_pdfactivity,
                (ViewGroup)findViewById(R.id.cl_bottomsheet_pdf));
        pdfBottomSheetDialog.setContentView(sheetView);
        pdfBottomSheetDialog.show();

        LinearLayout ll_continuous_page;
        LinearLayout ll_page_by_page;
        LinearLayout ll_rename;
        LinearLayout ll_print;
        LinearLayout ll_delete;
        TextView title;
        TextView dateandsize;
        Switch nightmode_switch;

        ll_continuous_page = sheetView.findViewById(R.id.ll_continous_page);
        ll_page_by_page = sheetView.findViewById(R.id.ll_page_by_page);
        ll_rename = sheetView.findViewById(R.id.ll_rename_pdf);
        ll_print = sheetView.findViewById(R.id.ll_print_pdf);
        ll_delete = sheetView.findViewById(R.id.ll_delete_pdf);
        title = sheetView.findViewById(R.id.tv_title_pdfactivity);
        dateandsize = sheetView.findViewById(R.id.tv_date_size_pdfactivity);
        nightmode_switch = sheetView.findViewById(R.id.btn_night_mode);

        if (switchEnable){
            nightmode_switch.setChecked(true);
        }else {
            nightmode_switch.setChecked(false);
        }

        title.setText(new File(path).getName());
        dateandsize.setText(Utils.getFileLastModifiedTime(new File(path))+" -  "+Utils.getFileSize(new File(path),this));


        ll_continuous_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPdf(path,null,true,false);
                pdfBottomSheetDialog.dismiss();

            }
        });

        ll_page_by_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPdf(path,null,false,false);
                pdfBottomSheetDialog.dismiss();

            }
        });

        nightmode_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightmode_switch.isChecked()) {
                    switchEnable = true;
                    displayPdf(path, null, false, true);
                    pdfBottomSheetDialog.dismiss();
                }else{
                    switchEnable = false;
                    displayPdf(path, null, false, false);
                    pdfBottomSheetDialog.dismiss();

                }

            }
        });

        ll_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRenameDialog(path);
                pdfBottomSheetDialog.dismiss();
            }
        });

        ll_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printPdf();
                pdfBottomSheetDialog.dismiss();
            }
        });

        ll_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(path);
                file.delete();
                addMedia(file);
                Toast.makeText(PdfViewActivity.this, "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                pdfBottomSheetDialog.dismiss();
                finish();
            }
        });




    }

    private void createRenameDialog(String path){
        File file = new File(path);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.item_dailog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText edt_password = dialog.findViewById(R.id.edt_password);

        TextView tv_title = dialog.findViewById(R.id.appCompatTextView);
        tv_title.setText("Rename File");
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel_dialog);
        TextView tv_ok = dialog.findViewById(R.id.tv_cancel_ok);
        dialog.show();

        tv_cancel.setOnClickListener(v -> dialog.dismiss());

        tv_ok.setOnClickListener(v -> {
            String extention = file.getPath().substring(file.getAbsolutePath().lastIndexOf("."));
            File old = new File(path);
            String name = old.getParentFile().getAbsolutePath();
            String newpath = name + "/" +edt_password.getText().toString() + extention;
            File newfile = new File(newpath);

            Boolean rename = file.renameTo(newfile);

            if (rename){
                addMedia(newfile);
                setUpToolbar(edt_password+".pdf");
                Toast.makeText(PdfViewActivity.this, "Renamed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }else{
                Toast.makeText(PdfViewActivity.this, "Couldn't Rename", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMedia(File f) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(f));
        sendBroadcast(intent);
    }



    private Boolean checkEncrypted(String path) {
        Boolean isEncrypted = Boolean.FALSE;
        try {
            byte[] byteArray = new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                byteArray = Files.readAllBytes(Paths.get(path));
            }
            //Convert the binary bytes to String. Caution, it can result in loss of data. But for our purposes, we are simply interested in the String portion of the binary pdf data. So we should be fine.
            String pdfContent = new String(byteArray);
            int lastTrailerIndex = pdfContent.lastIndexOf("trailer");
            if (lastTrailerIndex >= 0 && lastTrailerIndex < pdfContent.length()) {
                String newString = pdfContent.substring(lastTrailerIndex, pdfContent.length());
                int firstEOFIndex = newString.indexOf("%%EOF");
                String trailer = newString.substring(0, firstEOFIndex);
                if (trailer.contains("/Encrypt"))
                    isEncrypted = Boolean.TRUE;
            }
        } catch (Exception e) {
            System.out.println(e);
            //Do nothing
        }
        return isEncrypted;
    }

    private void initView() {
        tlb_pdfview = findViewById(R.id.tlb_pdf_view);
        sharedPreference = new SharedPreference(this);
        pdfView = findViewById(R.id.pdfView);
        interstitialAd = new InterstitialAd(this, "500334011038263_521463825591948");
    }

    private void setUpToolbar(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void displayPdf(String path, String password,boolean swipehorizontal,boolean nightmode) {
        setUpToolbar(new File(path).getName());
        pdfView.fromFile(new File(path))
                .enableSwipe(true)
                .password(password)
                .swipeHorizontal(swipehorizontal)
                .nightMode(nightmode)
                .onError(t -> {
                    Toast.makeText(PdfViewActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    createDialog(path);
                })
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    private void displayPdfwithUri(Uri uri) {
        Log.d("Create: ", String.valueOf(uri));
        pdfView.fromUri(uri)
                .enableSwipe(true)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

}