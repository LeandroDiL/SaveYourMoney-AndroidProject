package it.units.firebaseprojectexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button btnReg;
    private TextView mSignin;

    private ProgressDialog mDialog;
    private FirebaseAuth mAuth;

    private DatabaseReference mUserInfoDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        mDialog=new ProgressDialog(this);

        registration();
    }
    private void registration(){

        mEmail=findViewById(R.id.email_reg);
        mPass=findViewById(R.id.password_reg);
        Button btnReg = findViewById(R.id.btn_reg);
        TextView mSignin = findViewById(R.id.signin_here);

        mSignin.setOnClickListener(v -> {
            Intent intent= new Intent(RegisterActivity.this,LoginActivity.class);
            startActivity(intent);
        });

        btnReg.setOnClickListener(view -> {
            String email=mEmail.getText().toString().trim();
            String pass=mPass.getText().toString().trim();
            if(TextUtils.isEmpty(email)){
                mEmail.setError("Email required..",null);
                return;
            }
            if(TextUtils.isEmpty(pass)){
                mPass.setError("Password required..",null);
                return;
            }
            if(pass.length()<6)
            {
                mPass.setError("Must contain at least 6 characters..",null);
                return;
            }

            mDialog.setMessage("Processing..");
            mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    mDialog.dismiss();
                    sendEmailVerification();
                    mEmail=findViewById(R.id.email_reg);
                    String email1 =mEmail.getText().toString().trim();
                    FirebaseUser mUser=mAuth.getCurrentUser();
                    if(mAuth!=null) {
                        String uid = mUser.getUid();
                        DatabaseReference myRootRef = FirebaseDatabase.getInstance("https://fir-projectexample-158c2-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("UserInfo").child(uid);
                        DatabaseReference userNameRef =  myRootRef.child("Email");
                        userNameRef.setValue(email1);
                    }
                }
                else {
                    mDialog.dismiss();

                    Toast.makeText(getApplicationContext(),"Registration failed..",Toast.LENGTH_SHORT).show();

                }
            });
        });
    }
    private void sendEmailVerification()
    {
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if(firebaseUser!=null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    Toast.makeText(RegisterActivity.this,"Registration Successful.Verification mail sent successfully..",Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    finish();
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Error occurred sending verification mail..",Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}