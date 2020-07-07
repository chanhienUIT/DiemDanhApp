package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        final EditText editTextEmail = findViewById(R.id.editTextClassID);
        editTextEmail.setEnabled(false);
        final EditText editTextName = findViewById(R.id.editTextClassName);
        EditText editTextMac = findViewById(R.id.editTextMac);
        editTextMac.setEnabled(false);

        Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        final String name = intent.getStringExtra("name");
        final String idToken = intent.getStringExtra("idToken");
        final String macAddress = getMacAddr();

        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextMac.setText(macAddress);

        Button editButton = findViewById(R.id.buttonEdit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            try {
                URL url = new URL("http://diemdanh.ddns.net/user/edit");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(10000);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("Accept","application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                String name = editTextName.getText().toString();
                jsonParam.put("idToken", idToken);
                jsonParam.put("name", name);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
                bw.write(jsonParam.toString());
                bw.flush();
                bw.close();

                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    connection.disconnect();
                    Intent returnIntent = new Intent(EditActivity.this, MainActivity.class);
                    startActivity(returnIntent);
                    finish();
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Phiên hoạt động đã hết, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    Intent returnIntent = new Intent(EditActivity.this, MainActivity.class);
                    startActivity(returnIntent);
                    finish();
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Lỗi kết nối đến server", Snackbar.LENGTH_SHORT);
                snackbar.show();
                Intent returnIntent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(returnIntent);
                finish();
            }
            }
        });
    }

    @Override
    public void onBackPressed() {
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
        finish();
    }

    public String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ignored) {
        }
        return "02:00:00:00:00:00";
    }
}
