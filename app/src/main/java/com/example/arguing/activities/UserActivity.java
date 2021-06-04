package com.example.arguing.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.arguing.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UserActivity extends AppCompatActivity {
    private Button guardar, buttonExit;
    private TextView textEmail;
    private EditText editName, editApellidos, editTelef, editFecha;
    private ImageView imageProfile;
    private String email;
    private Activity activity;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private Pattern pattern;
    private Matcher matcher;
    private final String DATEPATTERN = "[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]";
    boolean noExiste = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        email = getIntent().getExtras().getString("email");
        boolean existe = getIntent().getExtras().getBoolean("existe");
        textEmail = findViewById(R.id.textEmail);
        editName = findViewById(R.id.editNombre);
        editApellidos = findViewById(R.id.editApellidos);
        editTelef = findViewById(R.id.editTextPhone);
        editFecha = findViewById(R.id.editTextDate);
        textEmail.setText(email);
        guardar = findViewById(R.id.button);
        buttonExit = findViewById(R.id.buttonExit);
        imageProfile = findViewById(R.id.imageProfile);
        activity = this;

        if (existe){
            cargarFoto();
            db.collection("users").document(email)
            .get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        editName.setText(task.getResult().get("nombre").toString());
                        editApellidos.setText(task.getResult().get("apellidos").toString());
                        editTelef.setText(task.getResult().get("telefono").toString());
                        editFecha.setText(task.getResult().get("fecha").toString());
                    }
                }
            });
        }

        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern = Pattern.compile(DATEPATTERN);
                matcher = pattern.matcher(editFecha.getText().toString());
                if (!editName.getText().toString().isEmpty() && !editApellidos.getText().toString().isEmpty() && !editTelef.getText().toString().isEmpty() && matcher.matches()) {
                    db.collection("users").whereEqualTo("telefono", editTelef.getText().toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                              List<DocumentSnapshot> ds = task.getResult().getDocuments();

                              for (QueryDocumentSnapshot document : task.getResult()) {
                                  if (document.get("telefono").toString().equals(editTelef.getText().toString()) && !document.getId().toString().equals(email)){

                                      noExiste = false;
                                      TextInputLayout telef = findViewById(R.id.editTextPhone_layout);
                                      telef.setError("Numero ya en uso");

                                  }
                              }
                                db.collection("users").document(email)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (noExiste){
                                                    if (task.getResult().getString("nombre") == null){
                                                        agregarDatosUsuario(email, editName.getText().toString(), editApellidos.getText().toString(), editTelef.getText().toString(), editFecha.getText().toString());
                                                    }else {
                                                        actualizarDatosUsuario(email, editName.getText().toString(), editApellidos.getText().toString(), editTelef.getText().toString(), editFecha.getText().toString());
                                                    }
                                                        finish();
                                                }
                                            }
                                        });
                            }
                        });
                }else {
                    Toast.makeText(UserActivity.this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    Vibrator vib;
                    vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(40);
                }
            }
        });
        buttonExit.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onClick(View v) {
                        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove("email");
                        editor.apply();
                        authIntent();
                    }
                }
        );
        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });

    }

    private void cargarFoto(){
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("images/"+ email +".jpg");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(activity)
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.drawable.default_user_icon)
                        .into(imageProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void authIntent(){
        Intent authIntent = new Intent(this, AuthActivity.class);
        startActivity(authIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null){
            Uri img = data.getData();
            Glide.with(activity)
                    .load(img)
                    .centerCrop()
                    .placeholder(R.drawable.default_user_icon)
                    .into(imageProfile);
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images/"+ email +".jpg");
            UploadTask ut = imageRef.putFile(img);

            ut.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });
        }
    }
    public static void agregarDatosUsuario(String email,String nombre, String apellidos, String telefono, String fecha){
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellidos", apellidos);
        user.put("telefono", telefono);
        user.put("fecha", fecha);
        db.collection("users").document(email)
                .set(user);
    }
    public static void actualizarDatosUsuario(String email,String nombre, String apellidos, String telefono, String fecha){
        Map<String, Object> user = new HashMap<>();
        user.put("nombre", nombre);
        user.put("apellidos", apellidos);
        user.put("telefono", telefono);
        user.put("fecha", fecha);
        db.collection("users").document(email)
                .update(user);
    }
}