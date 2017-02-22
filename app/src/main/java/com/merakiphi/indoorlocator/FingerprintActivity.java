package com.merakiphi.indoorlocator;

import android.Manifest;
import android.app.Dialog;
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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anuragmaravi on 19/02/17.
 */

public class FingerprintActivity extends AppCompatActivity {
    private static final String TAG = FingerprintActivity.class.getSimpleName();
    public static int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    public static String URL_REGISTER = "http://indoorlocator.pe.hu/fingerprint.php";


    //Working BSSIDs (for now only 4 APs are used for testing later it may be changed to a dynamic number)
    public static String BSSID_1 = "90:cd:b6:99:75:c9";
    public static String BSSID_2 = "90:cd:b6:99:75:c9";
    public static String BSSID_3 = "90:cd:b6:99:75:c9";
    public static String BSSID_4 = "90:cd:b6:99:75:c9";


    private TextView textViewAccessPoints, textViewCurrentRP;
    private EditText editTextRP;

    private int referencePointCount, referencePoint;


    private Button buttonSendData, buttonReferencePoint;
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
        textViewAccessPoints = (TextView) findViewById(R.id.textViewAccessPoints);
        textViewCurrentRP = (TextView) findViewById(R.id.textViewCurrentRP);
        editTextRP = (EditText) findViewById(R.id.editTextRP);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        buttonReferencePoint = (Button) findViewById(R.id.buttonReferencePoint);
        buttonReferencePoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    referencePointCount = Integer.parseInt(editTextRP.getText().toString().trim());
                    showRadioButtonDialog(referencePointCount);
                }catch (NumberFormatException n){
                    Toast.makeText(FingerprintActivity.this, "Please enter the Number of reference points (Integer)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSendData = (Button) findViewById(R.id.buttonSendData);
        buttonSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWifi();
            }
        });



    }

    /**
     * Scan all the available WiFi
     */
    private void scanWifi() {
        WifiManager w = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = w.getScanResults();
        Log.i("Scan Results: ", String.valueOf(scanResults));
        selectBssid(scanResults);
    }

    /**
     * Selecting the needed Access Points and sending only the data for those APs
     */
    private void selectBssid(List<ScanResult> scanResults) {
        int rssi_1 = 0, rssi_2 = 0, rssi_3 = 0, rssi_4 = 0;
        String ssids = "";
        for (int i = 0; i < scanResults.size(); i++) {
            Log.i(TAG, "SSID: " + scanResults.get(i).SSID);
            Log.i(TAG, "BSSID: " + scanResults.get(i).BSSID);
            Log.i(TAG, "RSSI: " + scanResults.get(i).level);

            if(scanResults.get(i).BSSID.equals(BSSID_1)){
                rssi_1 = scanResults.get(i).level;
                ssids += "AP1: " + scanResults.get(i).SSID + "\n";
            }
            if(scanResults.get(i).BSSID.equals(BSSID_2)){
                rssi_2 = scanResults.get(i).level;
                ssids += "AP2: " + scanResults.get(i).SSID + "\n";
            }
            if(scanResults.get(i).BSSID.equals(BSSID_3)){
                rssi_3 = scanResults.get(i).level;
                ssids += "AP3: " + scanResults.get(i).SSID + "\n";
            }
            if(scanResults.get(i).BSSID.equals(BSSID_4)){
                rssi_4 = scanResults.get(i).level;
                ssids += "AP4: " + scanResults.get(i).SSID + "\n";
            }
        }
        textViewAccessPoints.setText(ssids);
        sendData(referencePoint, rssi_1, rssi_2, rssi_3, rssi_4);
 //ToDO: Use a dialog with radio buttons for selecting the RP
    }


    /**
     * Sending Post Request to the database as fingerprint data
     */
    private void sendData(final int referencePoint, final int rssi1, final int rssi2, final int rssi3, final int rssi4) {
        final RequestQueue queue = Volley.newRequestQueue(this);
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
                params.put("reference_point", String.valueOf(referencePoint));
                params.put("rssi_1", String.valueOf(rssi1));
                params.put("rssi_2", String.valueOf(rssi2));
                params.put("rssi_3", String.valueOf(rssi3));
                params.put("rssi_4", String.valueOf(rssi4));
                return params;
            }

        };
        queue.add(strReq);
    }

    private void showRadioButtonDialog(int referencePointCount) {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.radiobutton_dialog);
        TextView textViewCancel = (TextView) dialog.findViewById(R.id.textViewCancel);

        List<String> stringList=new ArrayList<>();  // here is list
        for(int i=0; i<referencePointCount; i++) {
            stringList.add("Reference Point " + (i + 1));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);

        for(int i=0;i<stringList.size();i++){
            RadioButton rb=new RadioButton(this);  // dynamically creating RadioButton and adding to RadioGroup.
            rb.setText(stringList.get(i));
            rg.addView(rb);
        }
        dialog.show();
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        Log.e("selected RadioButton->",btn.getText().toString());
                        referencePoint = Integer.parseInt(String.valueOf(x + 1));
                        textViewCurrentRP.setText(String.valueOf(x + 1));
                        dialog.dismiss();
                    }
                }
            }
        });

        //Dismiss the dialog when cancel is pressed
        textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
