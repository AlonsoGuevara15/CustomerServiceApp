package pe.edu.pucp.customerserviceapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;

import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class MainActivity extends AppCompatActivity {
    private static final int LOGIN_FIREBASE = 1;

    private Button buttonInicio;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        TextView fireText = findViewById(R.id.textTitle);
        ImageView logo = findViewById(R.id.imgLogo);
        buttonInicio = findViewById(R.id.buttonIniciaSesion);
        progress = findViewById(R.id.progressBar_inicio);
        buttonInicio.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);


        validacionUsuario();

        Animation animacionAbajo = AnimationUtils.loadAnimation(this, R.anim.down_animation);

        fireText.setAnimation(animacionAbajo);
        logo.setAnimation(animacionAbajo);


    }

    public void login(View view) {
        List<AuthUI.IdpConfig> proveedores = Collections.singletonList(
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
            buttonInicio.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            if (currentUser.isEmailVerified()) {
                UsuarioManager.openUserMenu(MainActivity.this);
            } else {
                currentUser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (currentUser.isEmailVerified()) {
                            UsuarioManager.openUserMenu(MainActivity.this);
                        } else {
                            Toast.makeText(MainActivity.this, "Se le envió un correo para verificar su cuenta", Toast.LENGTH_LONG).show();
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    buttonInicio.setVisibility(View.VISIBLE);
                                    progress.setVisibility(View.INVISIBLE);
                                    Log.d("debugeo", "Correo Enviado");
                                }
                            });
                        }
                    }
                });
            }
        } else {
            buttonInicio.setVisibility(View.VISIBLE);
            progress.setVisibility(View.INVISIBLE);
        }
    }

}