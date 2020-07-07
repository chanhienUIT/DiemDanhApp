package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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

public class RegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    String registerURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Đăng ký user mới để tiếp tục", Snackbar.LENGTH_SHORT);
        snackbar.show();

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null)
            finish();

        final EditText editTextEmail = findViewById(R.id.editTextClassID);
        editTextEmail.setEnabled(false);
        final EditText editTextName = findViewById(R.id.editTextClassName);
        EditText editTextMac = findViewById(R.id.editTextMac);
        editTextMac.setEnabled(false);

        spinner = findViewById(R.id.roleSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(RegisterActivity.this, R.array.role_array, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        Intent intent = getIntent();
        final String email = intent.getStringExtra("email");
        final String name = intent.getStringExtra("name");
        final String idToken = intent.getStringExtra("idToken");
        final String macAddress = getMacAddr();

        editTextEmail.setText(email);
        editTextName.setText(name);
        editTextMac.setText(macAddress);

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int i = spinner.getSelectedItemPosition();
                    if (i == 0) {
                        registerURL = "http://diemdanh.ddns.net/user/register";
                    } else {
                        registerURL = "http://diemdanh.ddns.net/teacher/register";
                    }

                    URL url = new URL(registerURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    assert account != null;
                    String name = editTextName.getText().toString();

                    jsonParam.put("idToken", idToken);
                    jsonParam.put("email", email);
                    jsonParam.put("name", name);
                    jsonParam.put("macAddr", macAddress);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
                    bw.write(jsonParam.toString());
                    bw.flush();
                    bw.close();

                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        connection.disconnect();
                        Intent returnIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(returnIntent);
                        finish();
                    } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Phiên hoạt động kết thúc, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        connection.disconnect();
                        Intent returnIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(returnIntent);
                        finish();
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(RegisterActivity.this, "Lỗi kết nối đến server " + registerURL, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onBackPressed() {
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = (GoogleSignInClient) GoogleSignIn.getClient(this, gso);
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
        } catch (Exception ex) {
        }
        Toast.makeText(RegisterActivity.this, "Bật định vị để lấy địa chỉ MAC", Toast.LENGTH_SHORT).show();
        return "02:00:00:00:00:00";
    }
}
