package com.example.arguing.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.arguing.R;
import com.example.arguing.modelos.Evento;
import com.example.arguing.adapters.RecyclerAdapterEventos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private ImageButton buttonUser, buttonSearch;
    private Button buttonPopUp;
    private Activity activity;
    private String email, emailAmigo;
    private FloatingActionButton fab;
    private boolean existe;
    private ArrayList<Evento> eventos;
    private RecyclerView recyclerViewEventos;
    private RecyclerAdapterEventos recyclerAdapterEventos;
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private ArrayList<CheckBox> fechas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
        email = prefs.getString("email", null);
        buttonUser = findViewById(R.id.buttonUser);
        buttonSearch = findViewById(R.id.buttonSearch);
        fab = findViewById(R.id.fab);
        recyclerViewEventos = findViewById(R.id.recyclerViewEventos);
        recyclerViewEventos.setLayoutManager(new LinearLayoutManager(this));
        eventos = new ArrayList<Evento>();
        db.collection("users").document(email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        List<String> eventosUsuario = (List<String>) task.getResult().get("eventos");
                        if (eventosUsuario != null) {
                            for (int i = 0; i < eventosUsuario.size(); i++) {
                                db.collection("eventos").document(eventosUsuario.get(i))
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                String titulo = document.getString("titulo");
                                                String localizacion = document.getString("localizacion");
                                                Map<String, Long> fechas = (Map<String, Long>) document.get("horarios");

                                                Map<String, Boolean> asistentes = (Map<String, Boolean>) document.get("invitados");

                                                String owner = document.getString("owner");

                                                Evento evento = new Evento(document.getId(), titulo, localizacion, fechas, asistentes, owner);
                                                eventos.add(evento);
                                                if (eventosUsuario.size() == eventos.size()) {
                                                    recyclerAdapterEventos = new RecyclerAdapterEventos(eventos, email);
                                                    recyclerAdapterEventos.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            createPopUpDialog(eventos.get(recyclerViewEventos.getChildAdapterPosition(v)));
                                                        }
                                                    });
                                                    recyclerViewEventos.setAdapter(recyclerAdapterEventos);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });



        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                Evento evento = recyclerAdapterEventos.getEvento(position);
                if (evento.getOwner().equals(email)){
                    db.collection("eventos").document(evento.getId())
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    for (String key : evento.getAsistentes().keySet()){
                                        db.collection("users").document(key)
                                                .update("eventos", FieldValue.arrayRemove(evento.getId()));
                                    }

                                }
                            });
                }else{
                    db.collection("eventos").document(evento.getId())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    Map<String, Boolean> asistentes = (Map<String, Boolean>) task.getResult().get("invitados");
                                    asistentes.remove(email);
                                    db.collection("eventos").document(evento.getId())
                                            .update("invitados", asistentes);
                                    db.collection("users").document(email)
                                            .update("eventos", FieldValue.arrayRemove(evento.getId()));
                                }
                            });
                }
                eventos.remove(position);
                recyclerAdapterEventos.notifyDataSetChanged();

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewEventos);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearEventoIntent();
            }
        });

        activity = this;
        buttonUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userIntent(email);
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        activity, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext())
                        .inflate(
                                R.layout.layout_bottom_sheet,
                                (LinearLayout)findViewById(R.id.bottomSheetContainer)
                        );

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
                TextView tx = bottomSheetDialog.findViewById(R.id.textViewEncontrado);
                EditText et = bottomSheetDialog.findViewById(R.id.editTextBuscador);
                Button bt = bottomSheetDialog.findViewById(R.id.buttonAgregar);
                CardView cv = bottomSheetDialog.findViewById(R.id.cardImage);
                ImageView iv = bottomSheetDialog.findViewById(R.id.imageCard);
                bottomSheetDialog.findViewById(R.id.buttonBuscar).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tx.setVisibility(View.VISIBLE);
                        tx.setText("Buscando usuario...");
                        bt.setVisibility(View.GONE);
                        cv.setVisibility(View.GONE);
                        db.collection("users")
                                .whereEqualTo("telefono", et.getText().toString())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()){
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (!document.getId().toString().equals(email)){
                                                    emailAmigo = document.getId().toString();
                                                    db.collection("users").document(email)
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task2) {

                                                                    List<String> amigos = (List<String>) task2.getResult().get("amigos");

                                                                    if (amigos != null && amigos.contains(emailAmigo)){
                                                                        existe = true;
                                                                    }else if(amigos == null || !amigos.contains(emailAmigo)){
                                                                        existe = false;
                                                                    }

                                                                    StorageReference storageRef = storage.getReference();
                                                                    StorageReference imageRef = storageRef.child("images/" + emailAmigo + ".jpg");
                                                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            Glide.with(activity)
                                                                                    .load(uri)
                                                                                    .centerCrop()
                                                                                    .placeholder(R.drawable.default_user_icon)
                                                                                    .into(iv);
                                                                            tx.setText(document.get("nombre").toString() + " \n" + document.get("apellidos").toString());
                                                                            bt.setVisibility(View.VISIBLE);
                                                                            cv.setVisibility(View.VISIBLE);
                                                                            if (existe){
                                                                                bt.setText("✓");
                                                                            }else {
                                                                                bt.setText("AGREGAR");
                                                                            }
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception exception) {
                                                                            Glide.with(activity)
                                                                                    .load(R.drawable.default_user_icon)
                                                                                    .centerCrop()
                                                                                    .placeholder(R.drawable.default_user_icon)
                                                                                    .into(iv);
                                                                            tx.setText(document.get("nombre").toString() + " \n" + document.get("apellidos").toString());
                                                                            bt.setVisibility(View.VISIBLE);
                                                                            cv.setVisibility(View.VISIBLE);
                                                                            if (existe){
                                                                                bt.setText("✓");
                                                                            }else {
                                                                                bt.setText("AGREGAR");
                                                                            }
                                                                        }
                                                                    });


                                                                    bt.setOnClickListener(new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            if (bt.getText().equals("✓")){
                                                                                existe = true;
                                                                                estadoBoton(bt, emailAmigo);
                                                                            }else {
                                                                                existe = false;
                                                                                estadoBoton(bt, emailAmigo);
                                                                            }

                                                                        }
                                                                    });
                                                                }
                                                            });


                                                }
                                            }
                                        }

                                    }
                                });

                    }
                });

            }
        });
    }

    public void estadoBoton(Button bt, String emailAmigo){
        if (existe){
            db.collection("users").document(getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).getString("email", null)).update("amigos", FieldValue.arrayRemove(emailAmigo))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            existe = false;
                            bt.setText("AGREGAR");
                        }
                    });
        }else{
            db.collection("users").document(getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).getString("email", null)).update("amigos", FieldValue.arrayUnion(emailAmigo))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            existe = true;
                            bt.setText("✓");
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    private void userIntent(String email){
        Intent userIntent = new Intent(this, UserActivity.class);
        userIntent.putExtra("email", email);
        userIntent.putExtra("existe", true);
        startActivity(userIntent);
    }

    private void crearEventoIntent(){
        Intent crearEventoIntent = new Intent(this, CrearEventoActivity.class);
        startActivity(crearEventoIntent);
    }

    private void createPopUpDialog(Evento evento) {
        if (!evento.getAsistentes().get(email)) {
            dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
            final View contactPopUpView = getLayoutInflater().inflate(R.layout.popup, null);
            fechas = new ArrayList<CheckBox>();
            fechas.add((CheckBox) contactPopUpView.findViewById(R.id.checkBoxFecha1));
            fechas.add((CheckBox) contactPopUpView.findViewById(R.id.checkBoxFecha2));
            fechas.add((CheckBox) contactPopUpView.findViewById(R.id.checkBoxFecha3));
            for (int i = 0; i < evento.getFecha().size(); i++) {
                fechas.get(i).setText(evento.getListFechas().get(i));
                fechas.get(i).setVisibility(View.VISIBLE);
            }

            buttonPopUp = (Button) contactPopUpView.findViewById(R.id.buttonCheckBoxFechas);

            dialogBuilder.setView(contactPopUpView);
            dialog = dialogBuilder.create();
            dialog.show();

            buttonPopUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean oneChecked = false;
                    for (CheckBox fecha:fechas
                         ) {
                        if (fecha.isChecked()){
                            oneChecked = true;
                            String key = fecha.getText().toString();
                            evento.getFecha().put(key, evento.getFecha().get(key) + 1);
                        }
                    }
                    if (oneChecked){
                        evento.getAsistentes().put(email, true);
                        recyclerAdapterEventos.notifyDataSetChanged();
                        Map<String, Object> mMap = new HashMap<>();
                        mMap.put("invitados", evento.getAsistentes());
                        mMap.put("horarios", evento.getFecha());
                        db.collection("eventos").document(evento.getId())
                                .update(mMap);
                    }

                    dialog.hide();
                }
            });
        }
    }
}