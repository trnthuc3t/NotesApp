package com.example.notesprovip;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
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

public class CreateAccountActivity extends AppCompatActivity {
    EditText emailEdt, passwordEdt, confirmPwEdt;
    Button creatAccountBtn;
    ProgressBar progressBar;
    TextView loginBtnTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        initbd();
        creatAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        loginBtnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupPasswordToggle(passwordEdt,R.drawable.ic_visibility, R.drawable.ic_visibility_off);
        setupPasswordToggle(confirmPwEdt,R.drawable.ic_visibility, R.drawable.ic_visibility_off);
    }



    private void createAccount() {
        String email= emailEdt.getText().toString();
        String password= passwordEdt.getText().toString();
        String confirmPassword= confirmPwEdt.getText().toString();
        boolean isValidated= ValidateData(email,password,confirmPassword);
        if (!isValidated) {
            return;
        }
        createAccountFirebase(email,password);

    }

    private void createAccountFirebase(String email, String password) {
        runProgress(true);

        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                CreateAccountActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        runProgress(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateAccountActivity.this,
                                    "Tạo tài khoản thành công. Kiểm tra Email để xác nhận",
                                    Toast.LENGTH_SHORT).show();
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            firebaseAuth.signOut();
                            finish();
                        }else {
                            Toast.makeText(CreateAccountActivity.this,
                                    task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    void runProgress(boolean inprogress){
        if (inprogress == true) {
            progressBar.setVisibility(View.VISIBLE);
            creatAccountBtn.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.GONE);
            creatAccountBtn.setVisibility(View.VISIBLE);
        }
    }

    boolean ValidateData(String email, String password, String confirmPassword){
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEdt.setError("Email không hợp lệ!");
            return false;
        }
        if (password.length()<7) {
            passwordEdt.setError("Mật khẩu phải lớn hơn 7 kí tự");
        }
        if (password == confirmPassword) {
            confirmPwEdt.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    private void initbd() {
        emailEdt= findViewById(R.id.email_edittext);
        passwordEdt= findViewById(R.id.password_edittext);
        confirmPwEdt= findViewById(R.id.confirmpw_edittext);
        creatAccountBtn= findViewById(R.id.loginButton);
        progressBar= findViewById(R.id.progress_bar);
        loginBtnTv= findViewById(R.id.createAccount_tv_button);
    }


    private void setupPasswordToggle(final EditText editText, int visibleIcon, int invisibleIcon) {
        final boolean[] isPasswordVisible = {false};
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibleIcon, 0);

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[2].getBounds().width())) {
                        isPasswordVisible[0] = !isPasswordVisible[0];
                        if (isPasswordVisible[0]) {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, invisibleIcon, 0);
                        } else {
                            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, visibleIcon, 0);
                        }
                        editText.setSelection(editText.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });
    }
}