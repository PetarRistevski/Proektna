package ristevski.petar.pc.proektna;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class MainActivity extends AppCompatActivity {
    public static final String SAKAM_DA_POMOGNAM = "sakamDaPomognam";
    public static final String POTREBNA_MI_EUSLUGA = "potrebnaMiEUsluga";
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    String userType;
    DatabaseReference myRef = database.getReference();
    Switch swith;
    String currentUser;

    public void redirectActivity() {
        swith = findViewById(R.id.switch1);
        if(currentUser!=null) {
            if (userType.equals(POTREBNA_MI_EUSLUGA)) {


                Intent intent = new Intent(getApplicationContext(),NeedHelpActivity.class );
                intent.putExtra("momentalenUser", currentUser);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(),  ViewRequestsActivity.class);
                intent.putExtra("momentalenUser", currentUser);
                startActivity(intent);

            }
        }
    }
    public void getStarted(View view){
        swith = findViewById(R.id.switch1);
     //   final EditText userName = findViewById(R.id.etIme);
      //  final EditText password = findViewById(R.id.etPassword);
         userType = SAKAM_DA_POMOGNAM;
        if(swith.isChecked()){
            userType = POTREBNA_MI_EUSLUGA;

        }


        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                     currentUser = mAuth.getCurrentUser().getUid();
                myRef.child("Users").child(currentUser).setValue(currentUser);

                    redirectActivity();


                }
            }
        });


           }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
         mAuthListener = new FirebaseAuth.AuthStateListener() {
             @Override
             public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 FirebaseUser user  = firebaseAuth.getCurrentUser();
                 if(user == null) {


                 }
                  else {
                    redirectActivity();
                 }
             }
         };


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {

    }
}
