package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import android.text.format.DateFormat;
import android.widget.Toast;

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
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class CreateClassActivity extends AppCompatActivity {

    Spinner spinner;
    String idToken;
    EditText editTextClassID, editTextClassName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_class);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(CreateClassActivity.this, R.array.day_array, R.layout.support_simple_spinner_dropdown_item);
        spinner = findViewById(R.id.daySpinner);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        editTextClassID = findViewById(R.id.editTextClassID);
        editTextClassName = findViewById(R.id.editTextClassName);

        final EditText StartTimeEditText = findViewById(R.id.editTextStartTime);
        StartTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateClassActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        StartTimeEditText.setText(hour + ":" + minute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Chọn thời gian bắt đầu");
                mTimePicker.show();
            }
        });

        final EditText EndTimeEditText = findViewById(R.id.editTextEndTime);
        EndTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateClassActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        EndTimeEditText.setText(hour + ":" + minute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Chọn thời gian kết thúc");
                mTimePicker.show();
            }
        });

        Intent intent = getIntent();
        final String idToken = intent.getStringExtra("idToken");
        final String name = intent.getStringExtra("name");

        Button createClassButton = findViewById(R.id.buttonCreate);
        createClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    URL url = new URL("http://diemdanh.ddns.net/class/createclass");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);

                    JSONObject jsonParam = new JSONObject();
                    String classID = editTextClassID.getText().toString();
                    String className = editTextClassName.getText().toString();
                    String day = spinner.getSelectedItem().toString();
                    String startTime = StartTimeEditText.getText().toString();
                    String endTime = EndTimeEditText.getText().toString();

                    jsonParam.put("idToken", idToken);
                    jsonParam.put("classID", classID);
                    jsonParam.put("className", className);
                    jsonParam.put("day", day);
                    jsonParam.put("name", name);
                    jsonParam.put("startTime", startTime);
                    jsonParam.put("endTime", endTime);

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
                    bw.write(jsonParam.toString());
                    bw.flush();
                    bw.close();

                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        connection.disconnect();
                        finish();
                    } else if (connection.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Đã có lớp dưới mã lớp này", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Phiên hoạt động kết thúc, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        connection.disconnect();
                        finish();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CreateClassActivity.this, "Lỗi kết nối đến server", Toast.LENGTH_SHORT).show();
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



}