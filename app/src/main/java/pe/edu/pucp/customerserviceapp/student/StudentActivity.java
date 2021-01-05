package pe.edu.pucp.customerserviceapp.student;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.UsersRecycler;
import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class StudentActivity extends AppCompatActivity {
    private static final String TAG = "debugeo";
    private static String RUTAFIRE;
    private static String IMAGELOCAL;
    private static final int CAMERA_PERMISSION = 3;
    private static final int Download_PERMISSION = 4;


    private static final int GETIMAGE = 10;
    private static int CHATNUMBER = 1;
    private static ListenerRegistration lr = null;
    private ProgressBar PBCHATIMAGE;
    private ProgressBar PBCHATIMAGEDownload;

    private static Usuario OTROUSUARIO = new Usuario();

    // Make sure to use the FloatingActionButton
    // for all the FABs
    FloatingActionButton minfo, mlogout;

    // These are taken to make visible and invisible along
    // with FABs
    TextView logoutActionText;
    // to check whether sub FAB buttons are visible or not.
    Boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        setFloatingButton();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {


            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            String rol = (String) document.get("rol");
                            switch (rol) {
                                case UsuarioManager.ROLE_STUDENT:

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    ArrayList<Usuario> lista = new ArrayList<>();
                                    Log.d(TAG, "EMPIEZA A BUSCAR");
                                    db.collection("users")
                                            .whereEqualTo("rol", "Aitel")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "on complete");
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(TAG, "obtuvo un usuario");
                                                            Usuario usuario = document.toObject(Usuario.class);
                                                            lista.add(usuario);
                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                        }
                                                        RecyclerView mRecyclerView = findViewById(R.id.recyclerusersStudent);
                                                        UsersRecycler uAdapter = new UsersRecycler(lista, StudentActivity.this, R.id.fragmentStudent);
                                                        mRecyclerView.setAdapter(uAdapter);
                                                        mRecyclerView.setLayoutManager(new LinearLayoutManager(StudentActivity.this));
                                                    }
                                                }
                                            });
                                    if (lr == null) {
                                        Log.d(TAG, "lr es null");
                                        lr = UsuarioManager.setChatNotif(currentUser, StudentActivity.this);
                                    } else {
                                        Log.d(TAG, "lr no es null");
                                    }
                                    TextView textViewn = findViewById(R.id.textprofileST);

                                    textViewn.setText("Bienvenid@, " + currentUser.getDisplayName());


                                    break;
                                default:
                                    startActivity(new Intent(StudentActivity.this, MainActivity.class));
                                    finish();
                                    break;

                            }


                            //SI VIENE DE NOTIFICACION:
                            Intent intent = getIntent();
                            if (intent.getSerializableExtra("usuarionotif") != null) {
                                Usuario usuario = (Usuario) intent.getSerializableExtra("usuarionotif");
                                UsuarioManager.openPrivateChat(usuario, StudentActivity.this, R.id.fragmentStudent, null);
                            }
                        }

                    } else {
                        Log.d("debugeo", "get failed with ", task.getException());
                        startActivity(new Intent(StudentActivity.this, MainActivity.class));
                        finish();
                    }
                }
            });


        } else {
            startActivity(new Intent(StudentActivity.this, MainActivity.class));
            finish();
        }


    }

    public void tomarFoto(Usuario usuario, ProgressBar pb) {
        OTROUSUARIO = usuario;
        PBCHATIMAGE = pb;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        Log.d(TAG, "ENTRA TOMA FOTO");
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "TENIA PERMISO FOTO");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, GETIMAGE);
        } else {
            Log.d(TAG, "NO TENIA PERMISO FOTO");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    public void downloadIMAGE(String rutaFire, String nombrelocal) {
        RUTAFIRE = rutaFire;
        IMAGELOCAL = nombrelocal;
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d(TAG, "ENTRA DESCARGA");
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "TENIA PERMISO DESCARGA");
            StorageReference fileRef = FirebaseStorage.getInstance().getReference().child(rutaFire);
            File directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File localFile = new File(directorio, nombrelocal);
            fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "EXITO DESCARGA");
                    Toast.makeText(StudentActivity.this, "Archivo Descargado", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "ERROR DESCARGA");
                    Toast.makeText(StudentActivity.this, "No se pudo descargar", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    Log.d(TAG, "CARGANDO");
                }
            });
        } else {
            Log.d(TAG, "NO TENIA PERMISO DESCARGA");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Download_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION) {
                Log.d(TAG, "DIO PERMISO FOTO");
                tomarFoto(OTROUSUARIO, PBCHATIMAGE);
            } else if (requestCode == Download_PERMISSION) {
                Log.d(TAG, "DIO PERMISO descarga");
                downloadIMAGE(RUTAFIRE, IMAGELOCAL);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GETIMAGE:
                if (resultCode == RESULT_OK) {
                    PBCHATIMAGE.setVisibility(View.VISIBLE);
                    UsuarioManager.saveImageStorage(StudentActivity.this, data, OTROUSUARIO);
                }
                break;
        }


    }

    //    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d(TAG, "CLIENTE PAUSADO");
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (currentUser != null) {
//            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
//            docRef.update("currently", "idle");
//
//            FragmentManager fm = getSupportFragmentManager();
//            Fragment fragment = fm.findFragmentById(R.id.fragmentStudent);
//            if (fragment != null) {
//                fm.beginTransaction()
//                        .remove(fragment)
//                        .commit();
//            }
//        } else {
//            Log.d(TAG, "NO HAY USER");
//        }
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d(TAG, "CLIENTE RESUMIDO");
//
//
//
////        WaitingFragment wFragment = WaitingFragment.newInstance();
////        getSupportFragmentManager().beginTransaction()
////                .add(R.id.fragmentclient, wFragment)
////                .commit();
//
//    }


    public void setFloatingButton() {
        // Register all the FABs with their IDs
        // This FAB button is the Parent
        minfo = findViewById(R.id.info);
        // FAB button
        mlogout = findViewById(R.id.logout);
//        mupgrade = findViewById(R.id.upgrade);

        // Also register the action name text, of all the FABs.
        logoutActionText = findViewById(R.id.logout_action_text);
//        upgradeActionText = findViewById(R.id.upgrade_action_text);

        // Now set all the FABs and all the action name
        // texts as GONE
        mlogout.setVisibility(View.GONE);
//        mupgrade.setVisibility(View.GONE);
        logoutActionText.setVisibility(View.GONE);
//        upgradeActionText.setVisibility(View.GONE);


        isAllFabsVisible = false;

        minfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {
                            mlogout.show();
//                            mupgrade.show();
                            logoutActionText.setVisibility(View.VISIBLE);
//                            upgradeActionText.setVisibility(View.VISIBLE);
                            isAllFabsVisible = true;
                        } else {
                            mlogout.hide();
//                            mupgrade.hide();
                            logoutActionText.setVisibility(View.GONE);
//                            upgradeActionText.setVisibility(View.GONE);
                            isAllFabsVisible = false;
                        }
                    }
                });

        mlogout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UsuarioManager.logout(StudentActivity.this);
                    }
                });
//        mupgrade.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Toast.makeText(StudentActivity.this, "clickeado el upgrade", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }


}