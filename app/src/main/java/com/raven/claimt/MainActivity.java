package com.raven.claimt;

import static android.Manifest.permission.CAMERA;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.zxing.Result;
import com.raven.claimt.databinding.ActivityMainBinding;
import com.raven.claimt.databinding.ViewQrScannerBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.fab.setOnClickListener(view -> {
            if (checkPermission()) {
                scan_qr(view);
            } else {
                requestPermission();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void scan_qr(View view) {
        CodeScanner mCodeScanner;
        ViewQrScannerBinding scannerBinding;
        Dialog dialog = new Dialog(this);
        scannerBinding = ViewQrScannerBinding.inflate(LayoutInflater.from(this));
        dialog.setCanceledOnTouchOutside(false);
        MaterialButton cancel = scannerBinding.cancel;
        cancel.setVisibility(View.GONE);
        MaterialButton next = scannerBinding.next;
        scannerBinding.qrTitle.setText("Scan");
        scannerBinding.qrMessage.setText("...");
        mCodeScanner = new CodeScanner(this, scannerBinding.scannerView);
        mCodeScanner.startPreview();
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scannerBinding.qrMessage.setText(result.getText());
                    }
                });
            }
        });
        scannerBinding.scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.75);
        dialog.setContentView(scannerBinding.getRoot());
        dialog.getWindow().setLayout(width, height);
        dialog.show();
        next.setOnClickListener(v15 -> {
            // camera will stop scanning.
            mCodeScanner.releaseResources();
            dialog.dismiss();
        });
        cancel.setOnClickListener(v15 -> {
            // camera will stop scanning.
            mCodeScanner.releaseResources();
            dialog.dismiss();
        });
    }
    private boolean checkPermission() {
        // Permission is not granted
        return ContextCompat.checkSelfPermission(getApplicationContext(),
                CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Permission Granted", Toast.LENGTH_SHORT).show();

                // main logic
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        showMessageOKCancel("You need to allow access permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermission();
                                        }
                                    }
                                });
                    }
                }
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}