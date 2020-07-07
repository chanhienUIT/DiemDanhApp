package com.example.diemdanhapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.internal.SafeIterableMap;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {
    GoogleSignInAccount account;

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<Class> classList;
    private TeacherClassAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    ProgressBar loadingProgressBar;
    String idToken;
    JSONArray jsonResponse, jsonAttendanceResponse;
    Button CreateClassButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account = GoogleSignIn.getLastSignedInAccount(this);
        setContentView(R.layout.activity_teacher);
        idToken = getToken();

        loadingProgressBar = findViewById(R.id.fragmentProgressBar);

        swipeContainer = findViewById(R.id.swipeContainer);

        CreateClassButton = findViewById(R.id.createClassButton);

        TextView teacherInfo = findViewById(R.id.TeacherInfo);
        String name = "";
        Intent intent = getIntent();
        String info = intent.getStringExtra("RESPONSE");
        JSONObject teacherjson = null;
        try {
            teacherjson = new JSONObject(info);
            name = teacherjson.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String teacherpage = null;
        assert teacherjson != null;
        teacherpage = "Xin chào giảng viên " + name;
        teacherInfo.setText(teacherpage);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                classList.clear();
                new getTeacherClass().execute();
                swipeContainer.setRefreshing(false);
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        classList = new ArrayList<>();
        adapter = new TeacherClassAdapter(getApplicationContext(), classList);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new getTeacherClass().execute();

        adapter.setOnItemClickListener(new TeacherClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String classID = classList.get(position).getClassID();
                new getClassAttendance().execute(classID);
            }
        });

        final String finalName = name;
        CreateClassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createClassIntent = new Intent(getApplicationContext(), CreateClassActivity.class);
                createClassIntent.putExtra("idToken",idToken);
                createClassIntent.putExtra("name", finalName);
                startActivity(createClassIntent);
            }
        });

    }

    private class getClassAttendance extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String classID = strings[0];
            try {
                URL url = new URL("http://diemdanh.ddns.net/attendance/teacherCheckClassAttendance");
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
                jsonParam.put("ClassID", classID);

                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();

                connection.connect();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    connection.disconnect();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String responseLine;
                    StringBuffer response = new StringBuffer();
                    while ((responseLine = bufferedReader.readLine()) != null) {
                        response.append(responseLine);
                    }
                    bufferedReader.close();
                    jsonAttendanceResponse = new JSONArray(response.toString());

                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    connection.disconnect();
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Phiên hoạt động kết thúc vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                    signout();
                    snackbar.show();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Không thể kết nối đến server", Snackbar.LENGTH_SHORT);
                signout();
                snackbar.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (jsonAttendanceResponse != null) {
                StringBuilder ThongTinDiemDanh = new StringBuilder();

                for(int i = 0; i < jsonAttendanceResponse.length(); i++) {

                    try {
                        JSONObject jsonObject = jsonAttendanceResponse.getJSONObject(i);
                        ThongTinDiemDanh.append("Mã số sinh viên: ");
                        ThongTinDiemDanh.append(jsonObject.getString("student"));
                        ThongTinDiemDanh.append("\nBuổi: ");
                        JSONArray buoiArray = jsonObject.getJSONArray("buoi");
                        for (int j = 0; j < buoiArray.length(); j++) {
                            ThongTinDiemDanh.append(buoiArray.getString(j));
                            ThongTinDiemDanh.append(" ");
                        }
                        ThongTinDiemDanh.append("\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                TeacherDialogFragment diemdanhDialog = new TeacherDialogFragment();
                diemdanhDialog.setMessage(ThongTinDiemDanh.toString());
                diemdanhDialog.show(getSupportFragmentManager(), "Dialog");
            } else {

            }
        }
    }

    public static class TeacherDialogFragment extends DialogFragment {
        String response;
        public void setMessage(String response) {
            this.response = response;
            if (response.equals("")) {
                this.response = "Chưa có thông tin điểm danh";
            }
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Thông tin điểm danh lớp")
                    .setMessage(response)
                    .setPositiveButton("Đóng", null)
                    .create();
        }
    }

    private class getTeacherClass extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("http://diemdanh.ddns.net/class/getTeacherClass");
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
                    while ((responseLine = bufferedReader.readLine()) != null) {
                        response.append(responseLine);
                    }
                    bufferedReader.close();
                    jsonResponse = new JSONArray(response.toString());

                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    connection.disconnect();
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Phiên hoạt động kết thúc, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    signout();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"Không thể kết nối đến server", Snackbar.LENGTH_SHORT);
                signout();
                snackbar.show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingProgressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
            if (jsonResponse == null) return;
            for (int i = 0; i < jsonResponse.length(); i++) {
                try {
                    JSONObject jsonObject = jsonResponse.getJSONObject(i);

                    Class Class = new Class();
                    Class.setClassID(jsonObject.getString("classID"));
                    Class.setClassName(jsonObject.getString("className"));
                    Class.setStartTime(jsonObject.getString("startTime"));
                    Class.setEndTime(jsonObject.getString("endTime"));
                    Class.setDay(jsonObject.getString("day"));

                    classList.add(Class);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        account = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void signout() {
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
    }

    @Override
    public void onBackPressed() {
        signout();
        finish();
    }

    public String getToken() {
        Intent intent = getIntent();
        return intent.getStringExtra("idToken");
    }
}
