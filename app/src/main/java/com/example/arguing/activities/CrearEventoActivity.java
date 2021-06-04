package com.example.arguing.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.arguing.R;
import com.example.arguing.adapters.MyListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrearEventoActivity extends AppCompatActivity {

    private EditText titulo, location, fecha;
    private Button aceptar;
    private ListView listView;
    private List<String> mLista = new ArrayList<>();
    private MyListAdapter mAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Vibrator vib;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_evento);
        mContext = getApplicationContext();
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        listView = findViewById(R.id.listaView);
        titulo = findViewById(R.id.editTextTituloEvento);
        location = findViewById(R.id.editTextLocation);
        fecha = findViewById(R.id.editTextFecha);
        aceptar = findViewById(R.id.buttonCrearEvento);
        Places.initialize(getApplicationContext(), getString(R.string.CLAVE_API_MAPS));
        location.setFocusable(false);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CrearEventoActivity.this);

                startActivityForResult(intent, 100);
            }
        });

        location.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Localizacion", location.getText());
                clipboard.setPrimaryClip(clip);
                vib.vibrate(40);
                Toast.makeText(CrearEventoActivity.this, "Copiado en el portapapeles",Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        fecha.setFocusable(false);
        fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLista.size() < 3){
                    showDateTimeDialog();
                }else {
                    Toast.makeText(CrearEventoActivity.this, "Maximo 3 fechas",Toast.LENGTH_SHORT).show();
                }

            }
        });

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (titulo.getText().toString().isEmpty() || location.getText().toString().isEmpty()) {
                    Toast.makeText(CrearEventoActivity.this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    vib.vibrate(40);
                } else {
                    SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE);
                    String email = prefs.getString("email", null);
                    Map<String, Object> evento = new HashMap<>();
                    evento.put("owner", email);
                    evento.put("titulo", titulo.getText().toString());
                    evento.put("localizacion", location.getText().toString());
                    List<String> horas = mAdapter.getmObjects();
                    Map<String, Integer> horarios = new HashMap<>();
                    for (int i = 0; i < horas.size(); i++) {
                        horarios.put(horas.get(i), 0);
                    }
                    evento.put("horarios", horarios);
                    String id = db.collection("eventos").document().getId();

                        db.collection("eventos").document(id)
                                .set(evento)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent crearEventoIntent = new Intent(getApplicationContext(), AgregarUsuariosActivity.class);
                                        crearEventoIntent.putExtra("id", id);
                                        startActivity(crearEventoIntent);
                                    }
                                });
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            location.setText(place.getAddress());
        }
    }

    private void showDateTimeDialog() {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);

                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MM-yy HH:mm");

                        mLista.add(simpleDateFormat.format(calendar.getTime()).toString());
                        MyListAdapter adapter = new MyListAdapter(CrearEventoActivity.this, R.layout.list_item_fecha, mLista);
                        listView.setAdapter(adapter);
                    }
                };

                new TimePickerDialog(CrearEventoActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };

        new DatePickerDialog(CrearEventoActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();

    }
}