package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final String TAG = "LOG";
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;

    private ConstraintLayout loginView, registerView;
    private TextView registerViewButton, loginViewButton;

    private Button loginButton, registerButton;

    private EditText loginEmailText, loginPassText, registerEmailText, registerPassText, registerUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginView = findViewById(R.id.loginView);
        loginEmailText = findViewById(R.id.emailBox);
        loginEmailText.requestFocus();
        loginPassText = findViewById(R.id.passwordBox);
        registerView = findViewById(R.id.registerView);
        registerViewButton = findViewById(R.id.registrateButton);

        registerViewButton.setOnClickListener(view ->{
            registerUserName = findViewById(R.id.registerNameBox);
            registerUserName.requestFocus();
            registerEmailText = findViewById(R.id.registerEmailBox);
            registerPassText = findViewById(R.id.registerPasswordBox);
            loginView.animate().translationX(-loginView.getWidth()-1000).setDuration(500);
            registerView.setVisibility(View.VISIBLE);
            registerView.animate().translationX(0).setDuration(500);
        });

        loginViewButton = findViewById(R.id.logeateButton);
        loginViewButton.setOnClickListener(view ->{
            loginEmailText.requestFocus();
            registerView.animate().translationX(+registerView.getWidth()+1000).setDuration(500);
            loginView.animate().translationX(0).setDuration(500);
        });

        mAuth = FirebaseAuth.getInstance();



        //LOGIN
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(view ->{
            String email = loginEmailText.getText().toString();
            String password = loginPassText.getText().toString();
            if(checkLoginFields()){
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(Login.this,"Se ha logeado correctamente",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        //REGISTRO
        registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view ->{

            if(checkRegisterFields()){

                String username, email, pass;
                username = registerUserName.getText().toString();
                email = registerEmailText.getText().toString();
                pass = registerPassText.getText().toString();

                mAuth.createUserWithEmailAndPassword(email,pass)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                db = FirebaseFirestore.getInstance();
                                Map<String, Object> data = new HashMap<>();
                                data.put("name",username);

                                db.collection("usuarios").document(mAuth.getCurrentUser().getUid()).set(data);
                                Toast.makeText(Login.this,"Usuario registrado",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this,MainActivity.class);
                                startActivity(intent);
                                finish();

                            }else {
                                Toast.makeText(Login.this,"No se ha podido registrar",Toast.LENGTH_SHORT).show();
                            }
                        });


            }

        });

    }


    private boolean checkLoginFields(){
        String email = loginEmailText.getText().toString();
        String password = loginPassText.getText().toString();
        boolean check = true;

        if(email.isEmpty()){
            loginEmailText.setError("Campo vacio");
            loginEmailText.requestFocus();
            check = false;
        }
        if(password.isEmpty()) {
            loginPassText.setError("Campo vacio");
            loginPassText.requestFocus();
            check = false;
        }
        return check;
    }
    private boolean checkRegisterFields(){
        boolean check = true;
        if(registerUserName.getText().toString().length()< 3) {
            if(registerUserName.getText().toString().isEmpty())
                registerUserName.setError("El nombre está vacio");
            else registerUserName.setError("El nombre es demasiado corto");
            registerUserName.requestFocus();
            check = false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(registerEmailText.getText().toString()).matches()) {
            registerEmailText.setError("Introduce una direccion de correo valida");
            registerEmailText.requestFocus();
            check = false;
        }

        if(registerPassText.getText().toString().length()< 6) {
            if(registerPassText.getText().toString().isEmpty())
                registerPassText.setError("La contraseña está vacia");
            else registerPassText.setError("La contraseña es demasiado corta");
            registerPassText.requestFocus();
            check = false;
        }
        return check;
    }
}