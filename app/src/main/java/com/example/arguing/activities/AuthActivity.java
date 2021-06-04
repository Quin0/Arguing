package com.example.arguing.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arguing.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthActivity extends AppCompatActivity {
    private Button registerButton, logInButton, googleButton;
    private EditText emailEditText, passwordEditText;
    private final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        setup();

    }

    private void setup() {
        registerButton = findViewById(R.id.registerButton);
        logInButton = findViewById(R.id.logInButton);
        googleButton = findViewById(R.id.googleButton);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        registerButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!emailEditText.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches() && !passwordEditText.getText().toString().isEmpty()) {
                            FirebaseAuth.getInstance()
                                    .createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                                    .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Creando nuevo usuario", Toast.LENGTH_SHORT).show();
                                                showHome(task.getResult().getUser().getEmail());
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Email ya registrado", Toast.LENGTH_SHORT).show();
                                                Vibrator vib;
                                                vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                vib.vibrate(40);
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getApplicationContext(), "Datos no validos", Toast.LENGTH_SHORT).show();
                            Vibrator vib;
                            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(40);
                        }
                    }
                }
        );

        logInButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!emailEditText.getText().toString().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches() && !passwordEditText.getText().toString().isEmpty()) {
                            FirebaseAuth.getInstance()
                                    .signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                                    .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Iniciando sesion", Toast.LENGTH_SHORT).show();
                                                showHome(task.getResult().getUser().getEmail());

                                            } else {
                                                Toast.makeText(getApplicationContext(), "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                                                Vibrator vib;
                                                vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                vib.vibrate(40);
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(getApplicationContext(), "Datos no validos", Toast.LENGTH_SHORT).show();
                            Vibrator vib;
                            vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(40);
                        }
                    }
                }
        );

        googleButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Configure Google Sign In
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();

                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(AuthActivity.this, gso);
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);

                    }
                }
        );
    }

    private void showHome(String email) {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.apply();
        Intent homeIntent = new Intent(this, MainActivity.class);
        homeIntent.putExtra("email", email);
        startActivity(homeIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {

                Task task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = (GoogleSignInAccount) task.getResult(ApiException.class);

                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener(AuthActivity.this, new OnCompleteListener<AuthResult>() {
                                @SuppressLint("CommitPrefEdits")
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        showHome(account.getEmail());
                                    }
                                }
                            });
                }
            } catch (Throwable ApiException) {

            }
        }

    }
}
