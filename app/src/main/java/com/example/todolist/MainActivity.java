package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.EventListener;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String idUser;
    ListView listViewTareas;
    ArrayAdapter<String> adapterTareas;

    List<String> listaTareas = new ArrayList<>();

    List<String> listaIdTareas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        idUser = mAuth.getCurrentUser().getEmail();
        listViewTareas = findViewById(R.id.listTareas);
        actualizarUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.mas) {
            //abrir cuadro de diálogo para añadir tarea

            final EditText taskEditTest = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nueva tarea")
                    .setMessage("¿Qué quieres hacer a continuación?")
                    .setView(taskEditTest)
                    .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //AÑADIR TAREA A LA BASE DE DATOS
                            String miTarea = taskEditTest.getText().toString();
                            // Add a new document with a generated id.
                            Map<String, Object> data = new HashMap<>();
                            data.put("nombreTarea", miTarea);
                            data.put("usuario", idUser);

                            db.collection("Tareas")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(MainActivity.this, "Tarea añadida", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Fallo al crear la tarea", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .create();
            dialog.show();
            return true;
        }else if (item.getItemId() == R.id.logout) {
            //cierre de sesion de Firebase
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }

    }

    private void actualizarUI(){

        db.collection("Tareas")
                .whereEqualTo("usuario", idUser)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {

                            return;
                        }
                        listaTareas.clear();
                        listaIdTareas.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            listaTareas.add(doc.getString("nombreTarea"));
                            listaIdTareas.add(doc.getId());
                        }

                        //RELLENAR LISTVIEW CON EL ADAPTER
                        if(listaTareas.size() == 0){
                            listViewTareas.setAdapter(null);
                        }else{
                            adapterTareas = new ArrayAdapter<>(MainActivity.this, R.layout.item_tarea,R.id.textViewTarea, listaTareas);
                            listViewTareas.setAdapter(adapterTareas);
                        }
                    }
                });

    }

    public void borrarTarea(View view) {
        View parent = (View) view.getParent();
        TextView tareaTextView = parent.findViewById(R.id.textViewTarea);
        String tarea = tareaTextView.getText().toString();
        int posicion = listaTareas.indexOf(tarea);

        db.collection("Tareas").document(listaIdTareas.get(posicion)).delete();
        Toast.makeText(MainActivity.this, "Tarea realizada", Toast.LENGTH_SHORT).show();
    }

    public void actualizarTarea(View view){

        Toast.makeText(MainActivity.this, "Edit", Toast.LENGTH_SHORT).show();

        final EditText nuevaTarea = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Editar tarea")
                .setMessage("¿Quieres editar la tarea?")
                .setView(nuevaTarea)
                .setPositiveButton("Editar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        View parent = (View) view.getParent();
                        TextView tareaTextView = parent.findViewById(R.id.textViewTarea);
                        String tarea = tareaTextView.getText().toString();
                        int posicion = listaTareas.indexOf(tarea);
                        DocumentReference tareaRef = db.collection("Tareas").document(listaIdTareas.get(posicion));

                        // Set the "isCapital" field of the city 'DC'
                        tareaRef
                                .update("nombreTarea", nuevaTarea.getText().toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }

                })
                .setNegativeButton("Cancelar", null)
        .show();
    ;}

}