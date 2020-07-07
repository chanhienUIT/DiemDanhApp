package com.example.diemdanhapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class UserFragment extends Fragment {

    ImageView profileImageView;
    TextView textViewName, textViewEmail, textViewMssv;
    String response;
    ProgressBar progressBar;
    Button signoutbutton;
    Button editbutton;
    JSONObject jsonObject;


    String idToken, email, name;
    @Override
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        View view = inflater.inflate(R.layout.user_fragment, container, false);

        profileImageView = view.findViewById(R.id.ProfilePicImageView);
        textViewName = view.findViewById(R.id.textViewClassName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewMssv = view.findViewById(R.id.textViewMssv);
        progressBar = view.findViewById(R.id.UserProgressBar);
        signoutbutton = view.findViewById(R.id.sign_out_button);
        editbutton = view.findViewById(R.id.edit_button);


        textViewName.setText("");
        textViewEmail.setText("");
        textViewMssv.setText("");
        signoutbutton.setVisibility(View.GONE);
        editbutton.setVisibility(View.GONE);

        final UserActivity activity = (UserActivity) getActivity();
        assert activity != null;
        String imgUrl = activity.getPhotoURL();
        response = activity.getData();
        new getProfileTask(profileImageView).execute(imgUrl);
        signoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.signout();
            }
        });

        editbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(activity, EditActivity.class);
                idToken = activity.getToken();
                editIntent.putExtra("email", email);
                editIntent.putExtra("name", name);
                editIntent.putExtra("idToken", idToken);
                startActivity(editIntent);
            }
        });
        return view;
    }

    private class getProfileTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;
        getProfileTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap profilepic = null;
            try {
                jsonObject = new JSONObject(response);
                InputStream inputStream = new URL(imageUrl).openStream();
                profilepic = BitmapFactory.decodeStream(inputStream);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return profilepic;
        }

        protected void onPostExecute(Bitmap result) {
            progressBar.setVisibility(View.GONE);
            imageView.setImageBitmap(result);
            try {
                name = jsonObject.getString("name");
                email = jsonObject.getString("email");
                String mssv = jsonObject.getString("mssv");
                textViewName.setText(name);
                textViewEmail.setText(email);
                textViewMssv.setText(mssv);
                signoutbutton.setVisibility(View.VISIBLE);
                editbutton.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
