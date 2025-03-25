package com.example.notesprovip;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText emailEdt, passwordEdt;
    Button loginBtn;
    ProgressBar progressBar;
    TextView createaccountBtnTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        initbd();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        createaccountBtnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent= new Intent();

                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });
    }

    private void loginUser() {
        String email= emailEdt.getText().toString();
        String password= passwordEdt.getText().toString();
        boolean isValidated= ValidateData(email,password);
        if (!isValidated) {
            return;
        }
        loginAccountFirebase(email,password);
    }

    private void loginAccountFirebase(String email, String password) {
        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        runProgress(true);
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                runProgress(false);
                if (task.isSuccessful()) {
                    if (firebaseAuth.getCurrentUser().isEmailVerified()){
                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                        finish();
                    }
                    else {
                        Toast.makeText(LoginActivity.this,"Email chưa được xác minh, hãy kiểm tra Email", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(LoginActivity.this,
                            task.getException().getLocalizedMessage(),
                            Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    void runProgress(boolean inprogress){
        if (inprogress == true) {
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    boolean ValidateData(String email, String password){
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEdt.setError("Email không hợp lệ!");
            return false;
        }
        if (password.length()<7) {
            passwordEdt.setError("Mật khẩu phải lớn hơn 7 kí tự");
        }

        return true;
    }
    private void initbd() {
        emailEdt= findViewById(R.id.email_edittext);
        passwordEdt= findViewById(R.id.password_edittext);
        loginBtn= findViewById(R.id.loginButton);
        progressBar= findViewById(R.id.progress_bar);
        createaccountBtnTv= findViewById(R.id.createAccount_tv_button);
    }
}