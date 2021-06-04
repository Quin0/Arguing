package com.example.arguing.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arguing.modelos.Evento;
import com.example.arguing.R;

import java.util.ArrayList;
import java.util.Map;

public class RecyclerAdapterEventos extends RecyclerView.Adapter<RecyclerAdapterEventos.ViewHolderDatosEventos>
                                    implements  View.OnClickListener{

    private ArrayList<Evento> eventos;
    private View.OnClickListener listener;
    private String email;
    public RecyclerAdapterEventos(ArrayList<Evento> eventos, String email) {
        this.eventos = eventos;
        this.email = email;
    }

    public ArrayList<Evento> getEventos() {
        return eventos;
    }

    public Evento getEvento(int pos){
        return eventos.get(pos);
    }

    @NonNull
    @Override
    public ViewHolderDatosEventos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_evento, parent, false);
        view.setOnClickListener(this);
        return new ViewHolderDatosEventos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatosEventos holder, int position) {
    holder.asignarDatos(eventos.get(position));
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if (listener!=null){
            listener.onClick(v);
        }
    }

    public class ViewHolderDatosEventos extends RecyclerView.ViewHolder{

        TextView titulo, localizacion, horario;

        public ViewHolderDatosEventos(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textViewEventoTitulo);
            localizacion = itemView.findViewById(R.id.textViewEventoLugar);
            horario = itemView.findViewById(R.id.textViewEventoHorario);

        }

        public void asignarDatos(Evento evento) {
            titulo.setText(evento.getTitulo());
            localizacion.setText(evento.getLocalizacion());
            if (evento.getAsistentes().get(email) != null && evento.getAsistentes().get(email)){
                horario.setText(fechaMasVotada(evento.getFecha()));
                horario.setTextColor(Color.BLACK);
            }else {
                horario.setText("Click aqui para seleccionar fecha");
                horario.setTextColor(Color.RED);
            }

        }

        public String fechaMasVotada(Map<String, Long> fechas){
            long max = 0;
            String votada = "";
            for (String key:fechas.keySet()) {
                if (fechas.get(key) > max || votada.equals("")) {
                    max = fechas.get(key);
                    votada = key;
                }else if(fechas.get(key) == max){
                    int compare = votada.compareTo(key);
                    if (compare > 0){
                        votada = key;
                    }
                }
            }
            return votada;
        }

    }
}
