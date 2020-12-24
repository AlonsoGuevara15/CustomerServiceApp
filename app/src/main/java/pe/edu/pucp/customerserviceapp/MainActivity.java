package pe.edu.pucp.customerserviceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOGIN_FIREBASE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        validacionUsuario();

        Animation animacionArriba = AnimationUtils.loadAnimation(this,R.anim.up_animation);
        Animation animacionAbajo = AnimationUtils.loadAnimation(this,R.anim.down_animation);

        TextView fireText = findViewById(R.id.textTitle);
        Button buttonInicio =findViewById(R.id.buttonIniciaSesion);
        ImageView logo = findViewById(R.id.imgLogo);
        fireText.setAnimation(animacionAbajo);
        buttonInicio.setAnimation(animacionArriba);
        logo.setAnimation(animacionAbajo);

    }


    public void login(View view) {
        List<AuthUI.IdpConfig> proveedores = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        AuthUI instance = AuthUI.getInstance();
        Intent intent = instance
                .createSignInIntentBuilder()
                .setAvailableProviders(proveedores)
                .build();
        startActivityForResult(intent, LOGIN_FIREBASE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//
        if (requestCode == LOGIN_FIREBASE && resultCode == RESULT_OK) {
            validacionUsuario();
        }
    }

    public void validacionUsuario() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                Toast.makeText(MainActivity.this, "Su cuenta está verificada", Toast.LENGTH_LONG).show();
//                openFilesAct();
            } else {
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentUser.isEmailVerified()) {
                            Toast.makeText(MainActivity.this, "Su cuenta está verificada", Toast.LENGTH_LONG).show();

//                            openFilesAct();
                        } else {
                            Toast.makeText(MainActivity.this, "Se le envió un correo para verificar su cuenta", Toast.LENGTH_LONG).show();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("debugeo", "Correo Enviado");
                                }
                            });
                        }
                    }
                });
            }
        }


    }

//    public void openFilesAct() {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
//        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (!document.exists()) {
//                        Users user = new Users(currentUser.getDisplayName(),"Free",26214400,0); //26214400 - 25MB
//                        docRef.set(user);
//                    }
//                } else {
//                    Log.d("debugeo", "get failed with ", task.getException());
//                }
//            }
//        });
//        startActivity(new Intent(MainActivity.this, FilesActivity.class));
//        finish();
//
//    }
}