package com.example.diemdanhapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<Class> classList;
    private AllClassAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    ProgressBar loadingProgressBar;
    String idToken;
    JSONArray jsonResponse;


    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        View view = inflater.inflate(R.layout.class_fragment, container, false);

        UserActivity activity = (UserActivity) getActivity();

        loadingProgressBar = view.findViewById(R.id.fragmentProgressBar);

        swipeContainer = view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                classList.clear();
                new getAllClassData().execute();
                swipeContainer.setRefreshing(false);
            }
        });

        idToken = activity.getToken();

        recyclerView = view.findViewById(R.id.recyclerview);
        classList = new ArrayList<>();
        adapter = new AllClassAdapter(getActivity().getApplicationContext(), classList);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new getAllClassData().execute();

        adapter.setOnItemClickListener(new AllClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String classID = classList.get(position).getClassID();
                new diemdanh().execute(classID);
            }
        });

        return view;
    }

    private class diemdanh extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String classID = strings[0];
            try {
                URL url = new URL("http://diemdanh.ddns.net/class/enrollclass");
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
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Đăng ký lớp thành công", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
                    connection.disconnect();
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Bạn đã nằm trong lớp này", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Không thể kết nối đến server", Snackbar.LENGTH_SHORT);
                ((UserActivity)getActivity()).signout();
                snackbar.show();
            }
            return null;
        }
    }

    private class getAllClassData extends AsyncTask<Void, Void, Void> {
        @Override public void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            getAllClassData();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            loadingProgressBar.setVisibility(View.GONE);
            super.onPostExecute(result);
            refreshAllClassData();

        }
    }

    private void getAllClassData() {
        try {
            URL url = new URL("http://diemdanh.ddns.net/class/getall");
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
                Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Phiên hoạt động kết thúc, vui lòng đăng nhập lại", Snackbar.LENGTH_SHORT);
                snackbar.show();
                ((UserActivity)getActivity()).signout();
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Không thể kết nối đến server", Snackbar.LENGTH_SHORT);
            ((UserActivity)getActivity()).signout();
            snackbar.show();
        }

    }

    public void refreshAllClassData() {
        if (jsonResponse == null) return;
        for (int i = 0; i < jsonResponse.length(); i++) {
            try {
                JSONObject jsonObject = jsonResponse.getJSONObject(i);

                Class Class = new Class();
                Class.setClassID(jsonObject.getString("classID"));
                Class.setClassName(jsonObject.getString("className"));
                Class.setTeacher(jsonObject.getString("teacher"));
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
