package com.merakiphi.indoorlocator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anuragmaravi on 19/02/17.
 */

public class FingerprintActivity extends AppCompatActivity {
    private static final String TAG = FingerprintActivity.class.getSimpleName();
    public static String URL_REGISTER = "http://indoorlocator.pe.hu/fingerprint.php";
    public static int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    private Button buttonSendData;
    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);

            }
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        buttonSendData = (Button) findViewById(R.id.buttonSendData);

        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
//                sendData();
            }
        });

    }

    private void scanWifi() {
        WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = w.getScanResults();
        Log.i("Scan Results: ", String.valueOf(scanResults));
        for (int i = 0; i < scanResults.size(); i++) {
            Log.i(TAG, "BSSID: " + scanResults.get(i).BSSID);
            Log.i(TAG, "SSID: " + scanResults.get(i).SSID);
            Log.i(TAG, "RSSI: " + scanResults.get(i).level);
        }

    }

    private void sendData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        pDialog.setMessage("Sending Data...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response.toString());
                Toast.makeText(FingerprintActivity.this, response, Toast.LENGTH_SHORT).show();
                hideDialog();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                hideDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to fingerprint url
                Map<String, String> params = new HashMap<String, String>();
                params.put("reference_point", "3");
                params.put("rssi_1", "-10");
                params.put("rssi_2", "-12");
                params.put("rssi_3", "-12");
                params.put("rssi_4", "-12");
                return params;
            }

        };
        queue.add(strReq);


    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
