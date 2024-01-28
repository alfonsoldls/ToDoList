package com.example.todolist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId,name;
    private TextView username;
    ListView listViewTasks;
    ArrayAdapter<String> taskAdapter;
    List<String> listaTareas = new ArrayList<>();
    List<String> listaIdTareas = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.mytoolbar);
        setSupportActionBar(toolbar);
        //Inicializar FireBase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        listViewTasks = findViewById(R.id.lista);
        username = findViewById(R.id.nameLabel);
        DocumentReference userDocumentRef = db.collection("usuarios").document(userId);
        userDocumentRef.get().addOnCompleteListener(e->{
            username.setText(e.getResult().getString("name"));
        });

        updateUserInterface();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add){
            final EditText taskEditText = new EditText(this);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Nueva tarea")
                    .setMessage("¿Cuál es la tarea?")
                    .setView(taskEditText)
                    .setPositiveButton("Añadir", (d,i)->{
                        String miTarea = taskEditText.getText().toString();
                        Map<String, Object> data = new HashMap<>();
                        data.put("descripcion",miTarea);

                        //Añadir la nueva tarea a la base de datos
                        db.collection("usuarios").document(userId).collection("tareas")
                                .add(data)
                                .addOnSuccessListener(success-> Toast.makeText(MainActivity.this,"Tarea añadida", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(fail -> Toast.makeText(MainActivity.this,"Fallo al crear la tarea", Toast.LENGTH_SHORT).show());

                    })
                    .setNegativeButton("Cancelar",null)
                    .create();
            dialog.show();
            return true;
        }else if(item.getItemId()== R.id.logout){
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
            return true;
        }else
            return super.onOptionsItemSelected(item);

    }

    private void updateUserInterface(){
        db.collection("usuarios")
                .document(userId)
                .collection("tareas")
                .addSnapshotListener((document,e) -> {
                    if(e != null){
                        return;
                    }
                    listaTareas.clear();
                    listaIdTareas.clear();
                    Log.d("121212", String.valueOf(document.size()));
                    for (QueryDocumentSnapshot doc: document){
                        listaTareas.add(doc.getString("descripcion"));
                        listaIdTareas.add(doc.getId());
                    }

                    if(listaIdTareas.size()==0){
                        listViewTasks.setAdapter(null);
                        Log.d(null, "Nohaynada");

                    }else{
                        taskAdapter = new ArrayAdapter<>(MainActivity.this,R.layout.item_task, R.id.task,listaTareas);
                        listViewTasks.setAdapter(taskAdapter);
                    }
                });

    }

    public void updateTask(View view){
        View parent = (View) view.getParent();
        TextView textViewTask = parent.findViewById(R.id.task);
        String tareaAnterior = textViewTask.getText().toString();
        int posicion = listaTareas.indexOf(tareaAnterior);
        final EditText taskEditText = new EditText(this);
        taskEditText.setText(tareaAnterior);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar tarea")
                .setMessage("Actualiza tu tarea")
                .setView(taskEditText)
                .setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String tareaActualizada = taskEditText.getText().toString();

                        db.collection("usuarios")
                                .document(userId)
                                .collection("tareas").document(listaIdTareas.get(posicion))
                                .update("descripcion",tareaActualizada)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this,"Tarea actualizada", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this,"No se ha podido actualizar la tarea", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                });

                    }
                })
                .setNegativeButton("Cancelar", null)
                .create();
        dialog.show();

    }

    public void deleteTask(View view){
        View parent = (View) view.getParent();
        TextView textViewTask = parent.findViewById(R.id.task);
        String tarea = textViewTask.getText().toString();
        int posicion = listaTareas.indexOf(tarea);

        db.collection("usuarios")
                .document(userId)
                .collection("tareas")
                .document(listaIdTareas.get(posicion)).delete();
    }

}