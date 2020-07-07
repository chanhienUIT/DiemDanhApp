package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    Button signoutbutton;
    TextView userloggedinTextView;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        userloggedinTextView = findViewById(R.id.textViewUserLoggedin);
        loadingProgressBar = findViewById(R.id.progressBar);
        loadingProgressBar.setVisibility(View.GONE);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_ICON_ONLY);
        signInButton.setOnClickListener(this);

        signoutbutton = findViewById(R.id.sign_out_button);
        signoutbutton.setOnClickListener(this);
        signoutbutton.setVisibility(View.GONE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                handleSignInResult(task);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingProgressBar.setVisibility(View.GONE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {

            userloggedinTextView.setText("Đã đăng nhập dưới email " + account.getEmail());
            final String idToken = account.getIdToken();
            final Intent UserHomeIntent = new Intent(MainActivity.this, UserActivity.class);
            final Intent TeacherHomeIntent = new Intent(MainActivity.this, TeacherActivity.class);
            try {
                loadingProgressBar.setVisibility(View.VISIBLE);
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
                jsonParam.put("idToken", idToken);

                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    connection.disconnect();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String responseLine;
                    StringBuffer response = new StringBuffer();
                    while((responseLine = bufferedReader.readLine()) != null) {
                        response.append(responseLine);
                    }
                    bufferedReader.close();

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (jsonObject.getString("role").equals("sinhvien"))
                    {
                        UserHomeIntent.putExtra("RESPONSE", response.toString());
                        UserHomeIntent.putExtra("idToken", account.getIdToken());
                        startActivity(UserHomeIntent);
                    }
                    else if (jsonObject.getString("role").equals("giangvien"))
                    {
                        TeacherHomeIntent.putExtra("RESPONSE", response.toString());
                        TeacherHomeIntent.putExtra("idToken", account.getIdToken());
                        startActivity(TeacherHomeIntent);
                    }

                }
                else if (connection.getResponseCode() == 404) {
                    loadingProgressBar.setVisibility(View.GONE);
                    connection.disconnect();
                    Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    registerIntent.putExtra("email", account.getEmail());
                    registerIntent.putExtra("name", account.getDisplayName());
                    registerIntent.putExtra("idToken", account.getIdToken());
                    startActivity(registerIntent);
                }
                else if (connection.getResponseCode() == 401) {
                    signOut();
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error ", e);
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Không thể kết nối đến server", Snackbar.LENGTH_SHORT);
                snackbar.show();
                loadingProgressBar.setVisibility(View.GONE);
            }
        } else {
            userloggedinTextView.setText("Đăng nhập bằng Google để tiếp tục");
            signoutbutton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signoutbutton.setVisibility(View.VISIBLE);
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                        loadingProgressBar.setVisibility(View.GONE);
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Đã đăng xuất thành công", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            assert account != null;
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

}
