package it.units.firebaseprojectexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    Button changepass;
    Button deleteaccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        DatabaseReference mUserInfoDatabase = FirebaseDatabase.getInstance("https://fir-projectexample-158c2-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("UserInfo").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userEmail = dataSnapshot.child("Email").getValue().toString();
                TextView email = findViewById(R.id.email_profile);
                email.setText(userEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mUserInfoDatabase.addListenerForSingleValueEvent(valueEventListener);

        changepass = findViewById(R.id.btn_changepass);
        deleteaccount = findViewById(R.id.btn_deleteaccount);

        changepass.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        deleteaccount.setOnClickListener(v -> {

            AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileActivity.this);
            dialog.setTitle("Are you sure?");
            dialog.setMessage("Deleting this account will result in completely removing your account from Save Your Money and you will no longer be able to access this account. " +
                    "In future if you wish to use the same email then you need to register again.");
            dialog.setPositiveButton("DELETE", (dialog12, which) -> mUser.delete().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Account Deleted Successfully..", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProfileActivity.this, "This operation is sensitive and need re-authentication...", Toast.LENGTH_LONG).show();
                    Intent intent1 = new Intent(ProfileActivity.this, LoginActivity.class);
                    mAuth.signOut();
                    startActivity(intent1);
                }
            }));
            dialog.setNegativeButton("NO", (dialog1, which) -> dialog1.dismiss());
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        });

        ImageView back_arrow = findViewById(R.id.back);
        back_arrow.setOnClickListener(v -> onBackPressed());
    }
}
