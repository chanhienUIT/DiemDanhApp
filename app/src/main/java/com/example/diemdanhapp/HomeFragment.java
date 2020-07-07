package com.example.diemdanhapp;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
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
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private List<Class> classList;
    private ClassAdapter adapter;
    private SwipeRefreshLayout swipeContainer;
    ProgressBar loadingProgressBar;
    String idToken;
    JSONArray jsonResponse;
    TextView emptyListTextView;
    String BSSID;

    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        TextView welcomeTextView = view.findViewById(R.id.tvUserWelcome);
        final UserActivity activity = (UserActivity) getActivity();
        swipeContainer = view.findViewById(R.id.swipeContainer);

        loadingProgressBar = view.findViewById(R.id.fragmentProgressBar);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(true);
                classList.clear();
                new getClassData().execute();
                swipeContainer.setRefreshing(false);
            }
        });

        idToken = activity.getToken();
        BSSID = activity.BSSID;

        String userData = activity.getData();
        try {
            JSONObject object = new JSONObject(userData);
            welcomeTextView.setText("Xin chào sinh viên " + object.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        recyclerView = view.findViewById(R.id.recyclerview);
        classList = new ArrayList<>();
        adapter = new ClassAdapter(getActivity().getApplicationContext(), classList);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        new getClassData().execute();

        adapter.setOnItemLongClickListener(new ClassAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                String classID = classList.get(position).getClassID();
                new checkdiemdanh().execute(classID);
            }
        });

        adapter.setOnItemClickListener(new ClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!BSSID.equals("20:00:00:00:00:00")) {
                    String classID = classList.get(position).getClassID();
                    new diemdanh().execute(classID);
                } else {
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Bật định vị và khởi động lại app", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        });
        return view;
    }

    private class checkdiemdanh extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            String classID = strings[0];
            try {
                URL url = new URL("http://diemdanh.ddns.net/attendance/checkClassAttendance");
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
                jsonParam.put("classID", classID);

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

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONArray buoiArray = jsonObject.getJSONArray("buoi");
                    StringBuilder checkdiemdanh = new StringBuilder("Bạn đã điểm danh buổi: ");
                    for (int i = 0; i < buoiArray.length(); i++) {
                        checkdiemdanh.append(buoiArray.getString(i));
                        checkdiemdanh.append(" ");
                    }
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), checkdiemdanh.toString(), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Bạn chưa điểm danh buổi nào", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    connection.disconnect();
                    ((UserActivity) Objects.requireNonNull(getActivity())).signout();
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

    private class diemdanh extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String classID = strings[0];
            try {
                URL url = new URL("http://diemdanh.ddns.net/attendance/diemdanh");
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
                jsonParam.put("classID", classID);
                jsonParam.put("teacherMacAddr", BSSID);
                jsonParam.put("phoneMAC", getMacAddr());

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

                    StudentDialogFragment diemdanhDialog = new StudentDialogFragment();
                    diemdanhDialog.setMessage(jsonObject.getString("buoi"));
                    diemdanhDialog.show(getActivity().getSupportFragmentManager() , "Dialog");

                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_CONFLICT) {
                    connection.disconnect();
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Bạn đã điểm danh trong tuần này", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    connection.disconnect();
                    Snackbar snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),"Điểm danh không thành công", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    connection.disconnect();
                    ((UserActivity) Objects.requireNonNull(getActivity())).signout();
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

    public static class StudentDialogFragment extends DialogFragment {
        String response;
        public void setMessage(String response) {
            this.response = response;
        }
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Điểm danh thành công!")
                    .setMessage("Bạn đã điểm danh thành công buổi " + response)
                    .setPositiveButton("Đóng", null)
                    .create();
        }
    }

    private class getClassData extends AsyncTask<Void, Void, Void> {
        @Override public void onPreExecute() {
            super.onPreExecute();
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            getCurrentClassData();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            loadingProgressBar.setVisibility(View.GONE);
            super.onPostExecute(result);
            refreshCurrentClassData();
            if (adapter.getItemCount() == 0) {
                recyclerView.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }


    private void getCurrentClassData() {
        try {
            URL url = new URL("http://diemdanh.ddns.net/class/getcurrentclass");
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
            snackbar.show();
            ((UserActivity)getActivity()).signout();
        }

    }

    private void refreshCurrentClassData() {
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
        Toast.makeText(getActivity(), "Bật định vị để lấy địa chỉ MAC", Toast.LENGTH_SHORT).show();
        return "02:00:00:00:00:00";
    }

}
