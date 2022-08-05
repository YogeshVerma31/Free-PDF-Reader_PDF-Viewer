package com.yvtechnologies.pdfreader.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yvtechnologies.pdfreader.R;
import com.yvtechnologies.pdfreader.adapters.PdfListAdapter;
import com.yvtechnologies.pdfreader.listeners.MainRecyclerClick;
import com.yvtechnologies.pdfreader.utils.Constants;
import com.yvtechnologies.pdfreader.utils.SharedPreference;
import com.yvtechnologies.pdfreader.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.yvtechnologies.pdfreader.utils.Utils.RootDirectoryPDFReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MainRecyclerClick {

    private RecyclerView rv_main;
    private Toolbar tlb_main;
    private BottomSheetDialog pdfBottomSheetDialog;
    private LinearLayout item_empty_main;

    private Boolean menuVisiblity=false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private PdfListAdapter pdfListAdapter;

    private AdView adView;
    private LinearLayout adContainer;
    private InterstitialAd interstitialAd;
    private final String TAG = MainActivity.class.getSimpleName();

    private SharedPreference sharedPreference;

    private Executor executorService;

    ArrayList<File> fileListdata = new ArrayList<>();

    private Handler handler = new Handler();
    InterstitialAdListener interstitialAdListener;


    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        AudienceNetworkAds.initialize(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        initView();
        setSupportActionBar(tlb_main);
        setUpToolbar();
        executorService = Executors.newSingleThreadExecutor();

        check();

        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
// Ad error callback
                Toast.makeText(
                        MainActivity.this,
                        "Error: " + adError.getErrorMessage(),
                        Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onAdLoaded(Ad ad) {
// Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
// Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
// Ad impression logged callback
            }
        };

// Request an ad
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());

        interstitialAdListener = new InterstitialAdListener() {
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

        interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.ic_menu_search);
         SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Your PDF Here");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
        return true;
    }

    private void filter(String text) {
        ArrayList<File> filteredList = new ArrayList<>();

        for (File item : fileListdata) {
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        pdfListAdapter.filteredList(filteredList);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_atoz:
                sortListByNameAtoZ();
                break;
            case R.id.menu_ztoa:
                sortListByNameZtoA();
                break;
            case R.id.menu_ascending:
                sortListBySizeAscending();
                break;
            case R.id.menu_desending:
                sortListBySizeDescending();
                break;
            case R.id.menu_bydate:
                sortListByDate();
                break;
            case R.id.ic_menu_more:
                createMoreBottomSheet();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(0,menuVisiblity);
        return super.onPrepareOptionsMenu(menu);
    }

    private void sortListByDate(){
        Collections.sort(pdfListAdapter.fileList, (o1, o2) -> Utils.getFileLastModifiedTime(o2).compareTo(Utils.getFileLastModifiedTime(o1)));
        pdfListAdapter.notifyDataSetChanged();
    }

    private void sortListByNameAtoZ(){
        Collections.sort(pdfListAdapter.fileList, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        pdfListAdapter.notifyDataSetChanged();
    }

    private void sortListByNameZtoA(){
        Collections.sort(pdfListAdapter.fileList, (o1, o2) -> o2.getName().compareTo(o1.getName()));
        pdfListAdapter.notifyDataSetChanged();
    }

    private void sortListBySizeDescending(){
        Collections.sort(pdfListAdapter.fileList, (o1, o2) -> (int)(o2.length()-o1.length()));
        pdfListAdapter.notifyDataSetChanged();
    }

    private void sortListBySizeAscending(){
        Collections.sort(pdfListAdapter.fileList, (o1, o2) -> (int)(o1.length()-o2.length()));
        pdfListAdapter.notifyDataSetChanged();
    }

    private void initView() {
        tlb_main = findViewById(R.id.tlb_main);
        rv_main = findViewById(R.id.rv_main);
        item_empty_main = findViewById(R.id.item_empty_main);

        sharedPreference = new SharedPreference(this);

        adContainer=(LinearLayout) findViewById(R.id.banner_container);
        interstitialAd = new InterstitialAd(this, "500334011038263_521463825591948");
        adView = new AdView(this, "500334011038263_500709701000694", AdSize.BANNER_HEIGHT_50);
        adContainer.addView(adView);
        adView.loadAd();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        sharedPreference.putLoad(false);
        super.onDestroy();
    }

    private void setUpToolbar() {
        getSupportActionBar().setTitle("PDF Reader");
    }

    private void setAdapter() {
        executorService.execute(() -> {
            fileListdata = getDocumentData();
            runOnUiThread(() -> {
                if(fileListdata.size()<=0) {
                    item_empty_main.setVisibility(View.VISIBLE);
                    menuVisiblity = false;
                    invalidateOptionsMenu();
                }else {
                    menuVisiblity = true;
                    invalidateOptionsMenu();
                    item_empty_main.setVisibility(View.GONE);
                    pdfListAdapter = new PdfListAdapter(fileListdata, getApplicationContext(), this);
                    sortListByDate();
                    rv_main.setVisibility(View.VISIBLE);
                    rv_main.setAdapter(pdfListAdapter);
                    pdfListAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    public void check() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]), 0);
        } else {
            setAdapter();
        }
    }


    public ArrayList<File> getDocumentData() {

        ArrayList<File> fileList = new ArrayList<>();
        String[] columns = new String[]{MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA};

        String select = "(_data LIKE '%.pdf')";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"), columns, select, null, null);

        int columnIndexOrThrow_DATA = 0;
        if (cursor != null) {
            columnIndexOrThrow_DATA = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(columnIndexOrThrow_DATA);
                fileList.add(new File(path));
            }
        }
        cursor.close();
        return fileList;
    }

    private void fileshareClick(int position){
        Uri parse = Uri.parse(fileListdata.get(position).getPath());
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SEND");
        intent.setType("Pdf/*");
        intent.putExtra("android.intent.extra.STREAM", parse);
        startActivity(Intent.createChooser(intent, "Share Pdf..."));
        return;
    }

    public static void shareAppClick(Activity activity){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Pdf Reader App");
            String shareMessage = "\nLet me recommend you this application for View Pdf Efficiently and Effectively\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + activity.getPackageName() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            Log.d( "shareAppClick: ",e.getMessage());
        }
    }


    public static void ratingDialog(Context context) {
        Intent i3 = new Intent(Intent.ACTION_VIEW, Uri
                .parse("market://details?id=" + context.getPackageName()));
        i3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i3);
    }

    public static void sendFeedBack(Activity activity){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jiyogi9758@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, Constants.PDFFEEDBACK);
        intent.setType("message/rfc822");
        intent.setPackage("com.google.android.gm");
        activity.startActivity(Intent.createChooser(intent, "Select email"));
    }

    private void createDialog(int position){
        File file = new File(fileListdata.get(position).getPath());

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
            File old = new File(fileListdata.get(position).getAbsolutePath());
            String name = old.getParentFile().getAbsolutePath();
            String newpath = name + "/" +edt_password.getText().toString() + extention;
            File newfile = new File(newpath);

            Boolean rename = file.renameTo(newfile);

            if (rename){
                fileListdata.set(position,newfile);
                pdfListAdapter.notifyItemChanged(position);
                addMedia(newfile);
                Toast.makeText(MainActivity.this, "Renamed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }else{
                Toast.makeText(MainActivity.this, "Couldn't Rename", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMedia(File f) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(f));
        sendBroadcast(intent);
    }

    private void createMoreBottomSheet(){

        pdfBottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.item_app_details_bottomsheet,
                (ViewGroup)findViewById(R.id.cl_app_bottomsheet));
        pdfBottomSheetDialog.setContentView(sheetView);
        pdfBottomSheetDialog.show();

        LinearLayout ll_share_app;
        LinearLayout ll_rateus;
        LinearLayout ll_sendfeedback;
        LinearLayout ll_privacypolicy;

        ll_share_app = sheetView.findViewById(R.id.ll_share_main);
        ll_rateus = sheetView.findViewById(R.id.ll_rateus);
        ll_sendfeedback = sheetView.findViewById(R.id.ll_send_feedback_main);
        ll_privacypolicy = sheetView.findViewById(R.id.ll_privacy_policy);

        ll_rateus.setOnClickListener(v -> ratingDialog(getApplication()));

        ll_share_app.setOnClickListener(v -> shareAppClick(MainActivity.this));

        ll_sendfeedback.setOnClickListener(v -> sendFeedBack(MainActivity.this));

        ll_privacypolicy.setOnClickListener(v -> {
            onBrowseClick();
        });
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != 0) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            Log.d("check:", "called");
            setAdapter();
        } else {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(int position) {

        if(sharedPreference.getLoad()){
            Log.d(TAG, "onItemClick: called");
        }else{
            showAdWithDelay();
            Log.d(TAG, "onItemClick: buttoncalled");

            interstitialAd.loadAd(
                    interstitialAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build());
            sharedPreference.putLoad(true);

            if (interstitialAd.isAdLoaded()) {
                interstitialAd.show();
            }
        }

        Intent intent = new Intent(MainActivity.this,PdfViewActivity.class);
        intent.putExtra(Constants.PDFPATH,pdfListAdapter.fileList.get(position).getPath());
        startActivity(intent);

    }

    @Override
    public void onEditClick(int position) {

        TextView tv_titile;
        TextView tv_dateandsize;
        LinearLayout ll_share;
        LinearLayout ll_rename;
        LinearLayout ll_delete;
        LinearLayout ll_duplicate;

        pdfBottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetTheme);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.item_pdf_details_bottomsheet,
                (ViewGroup) findViewById(R.id.cl_bottomsheet));
        tv_titile = sheetView.findViewById(R.id.tv_title_pdf);
        tv_dateandsize = sheetView.findViewById(R.id.tv_date_size_pdf);
        ll_share = sheetView.findViewById(R.id.ll_send_feedback_main);
        ll_rename = sheetView.findViewById(R.id.ll_rename_pdf_main);
        ll_delete = sheetView.findViewById(R.id.ll_delete_pdf_main);

        tv_titile.setText(fileListdata.get(position).getName());
        tv_dateandsize.setText(Utils.getFileLastModifiedTime(
                new File(fileListdata.get(position).getPath())) + " - " +
                Utils.getFileSize(new File(fileListdata.get(position).getPath()), this));

        pdfBottomSheetDialog.setContentView(sheetView);
        pdfBottomSheetDialog.show();

        ll_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileshareClick(position);
                pdfBottomSheetDialog.dismiss();
            }
        });

        ll_rename.setOnClickListener(v -> {
            createDialog(position);
            pdfBottomSheetDialog.dismiss();
        });

        ll_delete.setOnClickListener(v -> {
            File file = new File(fileListdata.get(position).getPath());
            file.delete();
            fileListdata.remove(position);
            pdfListAdapter.notifyItemRemoved(position);
            addMedia(file);
            Toast.makeText(MainActivity.this, "Deleted SuccessFully", Toast.LENGTH_SHORT).show();
            pdfBottomSheetDialog.dismiss();
        });
    }

    private void onBrowseClick() {
        Log.d( "onBrowseClick: ","click");
        String url = "https://freepdfreader.blogspot.com/2021/07/privacy-policy-body-font-family.html";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showAdWithDelay() {

        handler.postDelayed(new Runnable() {
            public void run() {
                sharedPreference.putLoad(false);
            }
        }, 1000 * 60 * 2);// Show the ad after 15 minutes
    }
}