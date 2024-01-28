package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private String idUser;
    private FirebaseFirestore db;
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
        idUser = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        listViewTareas = findViewById(R.id.listTareas);
        actualizarUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.mas) {
            //activar cuadro dialogo para añadir tarea

            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nueva Tarea")
                    .setMessage("¿Que quieres hacer a continuacion?")
                    .setView(taskEditText)
                    .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Añadir tarea a la base de datos y al listView
                            String miTarea = taskEditText.getText().toString();
                            //Add a new document with a generated id.

                            Map<String, Object> data = new HashMap<>();
                            data.put("nombreTarea", miTarea);
                            data.put("usuario", idUser);

                            db.collection("Tareas")
                                    .add(data)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(MainActivity.this, "Tarea añadida correctamente", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Error al crear la tarea", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            }
                        })
                    .setNegativeButton("Cancelar", null)
                    .create();
            dialog.show();
            return true;

        }else if(item.getItemId() == R.id.logout) {
            //cierre de sesión de Firebase
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this,Login.class));
            finish();
            return true;
        }else return super.onOptionsItemSelected(item);

    }

    public void borrarTarea (View view){
        View parent = (View) view.getParent();
        TextView tareaTextView = parent.findViewById(R.id.textViewTarea);
        String tarea = tareaTextView.getText().toString();
        int posicion = listaTareas.indexOf(tarea);


        db.collection("Tareas").document(listaIdTareas.get(posicion)).delete();

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

                        //Rellenar el listView con el adapter
                        if(listaTareas.size() == 0) {
                            listViewTareas.setAdapter(null);
                        }else{
                            adapterTareas = new ArrayAdapter<>(MainActivity.this, R.layout.item_tarea, R.id.textViewTarea, listaTareas);
                            listViewTareas.setAdapter(adapterTareas);
                        }
                    }
                });
    }

}

