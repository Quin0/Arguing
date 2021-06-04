package com.example.arguing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.arguing.R;
import com.example.arguing.modelos.Contacto;
import com.example.arguing.adapters.RecyclerAdapterAmigos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgregarUsuariosActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> agregados;
    private ArrayList<Contacto> listContactos;
    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private Button bt;
    private RecyclerAdapterAmigos adapter;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_usuarios);

        id = getIntent().getExtras().getString("id");
        bt = findViewById(R.id.buttonAgregarAmigos);
        recyclerView = findViewById(R.id.recyclerContactos);
        llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        listContactos = new ArrayList<Contacto>();

        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        String email = prefs.getString("email", null);

        db.collection("users").document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        agregados = (List<String>) task.getResult().get("amigos");
                        if (agregados != null){
                            if (agregados.size()>0) {
                                for (int i = 0; i < agregados.size(); i++) {
                                    db.collection("users").document(agregados.get(i))
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    DocumentSnapshot ds = task.getResult();
                                                    String email = ds.getId();
                                                    String nombre = ds.getString("nombre");
                                                    String apellidos = ds.getString("apellidos");
                                                    Contacto contacto = new Contacto(email, nombre, apellidos);
                                                    listContactos.add(contacto);
                                                    if (listContactos.size() == agregados.size()){
                                                        adapter = new RecyclerAdapterAmigos(listContactos, AgregarUsuariosActivity.this);
                                                        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
                                                        recyclerView.addItemDecoration(dividerItemDecoration);
                                                        recyclerView.setAdapter(adapter);
                                                    }
                                                }
                                            });
                                }
                            }
                        }else{
                            db.collection("eventos").document(id).delete();
                            onBackPressed();
                        }

                    }
                });

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Boolean> mMap = new HashMap<>();
                mMap.put(email, false);
                ArrayList<Contacto> list = adapter.getListContactos();
                for (int i = 0; i<list.size(); i++){
                    Contacto contacto = list.get(i);
                    if (contacto.getSeleccionado()){
                        mMap.put(contacto.getEmail(), false);
                    }
                }
                if (mMap.size()>1){
                    Map<String, Object> amigos = new HashMap<>();
                    amigos.put("invitados", mMap);
                    db.collection("eventos").document(id)
                            .update(amigos)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Object users[] = mMap.keySet().toArray();
                                    for (int i = 0; i < users.length; i++) {
                                        db.collection("users").document(users[i].toString())
                                                .update("eventos", FieldValue.arrayUnion(id))
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                    }
                                    Intent homeIntent = new Intent(getApplicationContext(), HomeActivity.class);
                                    startActivity(homeIntent);
                                }
                            });
                }else{
                    Toast.makeText(AgregarUsuariosActivity.this, "Selecciona al menos 1 amigo",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}