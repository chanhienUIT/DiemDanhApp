package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class UserActivity extends AppCompatActivity {

    GoogleSignInAccount account;
    BottomNavigationView bottomNavigationView;
    String imgUrl;
    String BSSID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null)
            wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null)
            BSSID = wifiInfo.getBSSID();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemID = item.getItemId();

                switch(itemID) {
                    case R.id.iconHome:
                        fragment = new HomeFragment();
                        break;
                    case R.id.iconClass:
                        fragment = new ClassFragment();
                        break;
                    case R.id.iconNotification:
                        fragment = new NotificationFragment();
                        break;
                    case R.id.iconUser:
                        fragment = new UserFragment();
                        break;
                }
                MenuItem menuItem = bottomNavigationView.getMenu().findItem(itemID);
                menuItem.setChecked(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                return false;
            }
        });
    }


    public String getData() {
        Intent intent = getIntent();
        return intent.getStringExtra("RESPONSE");
    }

    public String getToken() {
        Intent intent = getIntent();
        return intent.getStringExtra("idToken");
    }

    protected String getPhotoURL() {
        assert imgUrl != null;
        return imgUrl;
    }

    @Override
    protected void onStart() {
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
        try {
            URL url = new URL("http://diemdanh.ddns.net/user/authenticate");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("idToken", getToken());

            DataOutputStream os = new DataOutputStream(connection.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                signout();
                Intent returnIntent = new Intent(UserActivity.this, MainActivity.class);
                startActivity(returnIntent);
                finish();
            } if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                connection.disconnect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String responseLine;
                StringBuffer response = new StringBuffer();
                while ((responseLine = bufferedReader.readLine()) != null) {
                    response.append(responseLine);
                }
                bufferedReader.close();

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                imgUrl = jsonObject.getString("profilepic");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Phiên hoạt động đã hết, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
            snackbar.show();
            signout();
            Intent returnIntent = new Intent(UserActivity.this, MainActivity.class);
            startActivity(returnIntent);
            finish();

        }
    }

    public void signout() {
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {}
                });

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Đã đăng xuất", Snackbar.LENGTH_SHORT);
        snackbar.show();
        Intent returnIntent = new Intent(UserActivity.this, MainActivity.class);
        startActivity(returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        signout();
        Intent returnIntent = new Intent(UserActivity.this, MainActivity.class);
        startActivity(returnIntent);
        finish();
    }
}
