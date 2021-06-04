package com.example.arguing.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arguing.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String email;
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefs.getString("email", null);
        if (email == null){
            authIntent();
        }else{
            setTheme(R.style.Theme_Arguing);
            db.collection("users").document(email)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot ds = task.getResult();
                                if (!ds.exists()){
                                    userIntent(email);
                                }
                            } else {
                            }
                        }
                    });
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            setup(email);
        }
    }

    private void setup(String email){
        Button buttonExit = findViewById(R.id.buttonExit);
        homeIntent();

    }

    private void authIntent(){
        Intent authIntent = new Intent(this, AuthActivity.class);
        startActivity(authIntent);
    }
    private void homeIntent(){
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
    }

    private void userIntent(String email){
        Intent userIntent = new Intent(this, UserActivity.class);
        userIntent.putExtra("email", email);
        userIntent.putExtra("existe", false);
        startActivity(userIntent);
    }

    private void agregarDB(String email){

    }
}

