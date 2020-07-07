package com.example.diemdanhapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationFragment extends Fragment {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<Notification> notificationList;
    private NotificationAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    ProgressBar loadingProgressBar;
    String idToken;
    JSONArray jsonResponse;


    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        View view = inflater.inflate(R.layout.notification_fragment, container, false);

        UserActivity activity = (UserActivity) getActivity();

        loadingProgressBar = view.findViewById(R.id.fragmentProgressBar);

        swipeContainer = view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                notificationList.clear();
                new getNotification().execute();
                swipeContainer.setRefreshing(false);
            }
        });

        idToken = activity.getToken();

        recyclerView = view.findViewById(R.id.recyclerview);
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getActivity().getApplicationContext(), notificationList);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new getNotification().execute();

        return view;
    }

    private class getNotification extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            getNotificationData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            refreshNotificationData();
        }
    }

    private void getNotificationData() {
        try {
            URL url = new URL("http://diemdanh.ddns.net/attendance/getAttendance");
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

    public void refreshNotificationData() {
        if (jsonResponse == null) return;
        for (int i = 0; i < jsonResponse.length(); i++) {
            try {
                JSONObject jsonObject = jsonResponse.getJSONObject(i);

                Notification notification = new Notification();
                notification.setClassID(jsonObject.getString("ClassID"));
                notification.setTime(jsonObject.getString("receivedTime"));
                notification.setBuoi(jsonObject.getString("buoi"));
                notificationList.add(notification);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

}
