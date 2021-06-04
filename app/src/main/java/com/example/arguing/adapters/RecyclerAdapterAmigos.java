package com.example.arguing.adapters;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.arguing.modelos.Contacto;
import com.example.arguing.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RecyclerAdapterAmigos extends RecyclerView.Adapter<RecyclerAdapterAmigos.ViewHolderDatosAmigos> {

    private ArrayList<Contacto> listContactos;
    private Activity activity;

    public RecyclerAdapterAmigos(ArrayList<Contacto> listContactos, Activity activity) {
        this.listContactos = listContactos;
        this.activity = activity;
    }

    public ArrayList<Contacto> getListContactos() {
        return listContactos;
    }

    @NonNull
    @Override
    public ViewHolderDatosAmigos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_contacto, parent, false);
        return new ViewHolderDatosAmigos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatosAmigos holder, int position) {
        holder.asignarDatos(listContactos.get(position));
    }

    @Override
    public int getItemCount() {
        return listContactos.size();
    }

    public class ViewHolderDatosAmigos extends RecyclerView.ViewHolder {

        ImageView imagen;
        TextView nombre;
        CheckBox check;
        FirebaseStorage storage = FirebaseStorage.getInstance();


        public ViewHolderDatosAmigos(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.imageViewContact);
            nombre = itemView.findViewById(R.id.textViewNomContact);
            check = itemView.findViewById(R.id.checkBoxContact);
        }

        public void asignarDatos(Contacto contacto) {
            StorageReference storageRef = storage.getReference();
            StorageReference imageRef = storageRef.child("images/" + contacto.getEmail() + ".jpg");
            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(activity)
                            .load(uri)
                            .centerCrop()
                            .placeholder(R.drawable.default_user_icon)
                            .into(imagen);
                    nombre.setText(contacto.getNombre() + " \n" + contacto.getApellidos());
                    check.setChecked(contacto.getSeleccionado());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Glide.with(activity)
                            .load(R.drawable.default_user_icon)
                            .centerCrop()
                            .placeholder(R.drawable.default_user_icon)
                            .into(imagen);
                    nombre.setText(contacto.getNombre() + " \n" + contacto.getApellidos());
                    check.setChecked(contacto.getSeleccionado());
                }
            });

            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (check.isChecked()){
                        contacto.setSeleccionado(true);
                    }else {
                        contacto.setSeleccionado(false);
                    }
                }
            });

        }
    }
}
