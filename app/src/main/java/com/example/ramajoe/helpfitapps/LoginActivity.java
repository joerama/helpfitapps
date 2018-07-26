package com.example.ramajoe.helpfitapps;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ramajoe.helpfitapps.Common.Common;
import com.example.ramajoe.helpfitapps.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin,btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseDatabase Db;
    private DatabaseReference users;
    TextView txtForgotPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //button setup
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        txtForgotPwd = findViewById(R.id.txtForgotPwd);


        //firebase setup
        mAuth = FirebaseAuth.getInstance();
        Db = FirebaseDatabase.getInstance();
        users = Db.getReference("Users");
        //userDT = Db.getReference("UserData");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayRegister();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });
    }

    private void displayRegister() {
        //create dialog
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.activity_register,null);
        final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this);

        //get data
        final TextInputEditText name = register_layout.findViewById(R.id.RegName);
        final TextInputEditText username = register_layout.findViewById(R.id.RegUsername);
        final TextInputEditText email = register_layout.findViewById(R.id.RegEmail);
        final TextInputEditText password = register_layout.findViewById(R.id.RegPass);
        final RadioButton RBTrainer = register_layout.findViewById(R.id.RBTrainer);
        final RadioButton RBmember = register_layout.findViewById(R.id.RBMember);

        dialog.setView(register_layout);

        //set button register
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //set radio button result
                final String type;
                if(RBTrainer.isChecked()){ type = "trainer";}
                else type = "member";

                dialog.dismiss();
                waitingDialog.show();
                //Check validation
                if((TextUtils.isEmpty(name.getText().toString()) ||
                        TextUtils.isEmpty(email.getText().toString()) ||
                        TextUtils.isEmpty(username.getText().toString()) ||
                        TextUtils.isEmpty(password.getText().toString())) ||
                        (!RBTrainer.isChecked() && !RBmember.isChecked())){

                    Toast.makeText(LoginActivity.this, "Please fill up all data !!", Toast.LENGTH_SHORT).show();
                    waitingDialog.dismiss();
                }else{
                    //Register new user
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    //Save user to db
                                    final User user = new User();
                                    user.setFullname(name.getText().toString());
                                    user.setUsername(username.getText().toString());
                                    user.setEmail(email.getText().toString());
                                    user.setPassword(password.getText().toString());
                                    user.setType(type);

                                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    waitingDialog.dismiss();
                                                    Toast.makeText(LoginActivity.this, "Register successfully !", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    waitingDialog.dismiss();
                                                    Toast.makeText(LoginActivity.this, "Failed, "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();


                                }
                            });
                }
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void showLoginDialog() {
        final EditText email = findViewById(R.id.LgnEmail);
        final EditText password = findViewById(R.id.LgnPassword);
        final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this);
        waitingDialog.show();



        if(!email.getText().toString().equalsIgnoreCase("") && !password.getText().toString().equalsIgnoreCase("")){
            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            waitingDialog.dismiss();

                            users.child(mAuth.getCurrentUser().getUid().toString()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {


                                    User user = dataSnapshot.getValue(User.class);
                                    Common.currentUser = user;

                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    waitingDialog.dismiss();

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    waitingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Failed, "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            waitingDialog.dismiss();
            Toast.makeText(this, "Please fill your email & password !", Toast.LENGTH_SHORT).show();
        }

    }
}
