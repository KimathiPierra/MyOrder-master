package com.example.pierra.myorder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pierra.myorder.Common.Common;
import com.example.pierra.myorder.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import static com.example.pierra.myorder.R.id.edtPassword;

public class SignIn extends AppCompatActivity {

    MaterialEditText edtphone, edtpassword;
    Button btnSignIn;
    TextView txtForgotPwd;


     FirebaseDatabase database;
    DatabaseReference table_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtphone = (MaterialEditText) findViewById(R.id.edtphone);
        edtpassword = (MaterialEditText) findViewById(edtPassword);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        txtForgotPwd = (TextView)findViewById(R.id.txtForgotPwd);

        //init firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("User");

        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPwdDialog();
            }
        });



      btnSignIn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              if (Common.isConnectedToInternet(getBaseContext())) {

                  final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                  mDialog.setMessage("Please wait...");
                  mDialog.show();

                  table_user.addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(DataSnapshot dataSnapshot) {
                          if (dataSnapshot.child(edtphone.getText().toString()).exists()) {

                              mDialog.dismiss();

                              User user = dataSnapshot.child(edtphone.getText().toString()).getValue(User.class);
                              user.setPhone(edtphone.getText().toString());
                              if (user.getPassword().equals(edtpassword.getText().toString())) {
                                  Toast.makeText(SignIn.this, "Sign in successFull", Toast.LENGTH_SHORT).show();
                                  {
                                      Intent homeIntent = new Intent(SignIn.this, Home.class);
                                      Common.currentUser = user;
                                      startActivity(homeIntent);
                                      finish();
                                  }
                              } else {
                                  Toast.makeText(SignIn.this, "Wrong password", Toast.LENGTH_SHORT).show();
                              }
                          } else {
                              mDialog.dismiss();
                              Toast.makeText(SignIn.this, "User not in database", Toast.LENGTH_SHORT).show();
                          }
                      }

                      @Override
                      public void onCancelled(DatabaseError databaseError) {

                      }
                  });
              }
              else
              {
                        Toast.makeText(SignIn.this, "Please check you internet connection", Toast.LENGTH_SHORT).show();
                  return;
              }
          }

      });
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter your secure code");

        LayoutInflater inflater = this.getLayoutInflater();
        View forgot_view = inflater.inflate(R.layout.forgot_password_layout,null);

        builder.setView(forgot_view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        final MaterialEditText edtphone = (MaterialEditText)forgot_view.findViewById(R.id.edtphone);
        final MaterialEditText edtSecureCode = (MaterialEditText)forgot_view.findViewById(R.id.edtSecureCode);



        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(edtphone.getText().toString())
                                .getValue(User.class);

                        if (user.getSecureCode().equals(edtSecureCode.getText().toString()))
                            Toast.makeText(SignIn.this, "Your password : "+user.getPassword(), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(SignIn.this, "Wrong secure code", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }


}
