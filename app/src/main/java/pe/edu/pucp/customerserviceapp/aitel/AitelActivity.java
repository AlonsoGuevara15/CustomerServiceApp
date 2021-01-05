package pe.edu.pucp.customerserviceapp.aitel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;

public class AitelActivity extends AppCompatActivity {

    private static final String NOTIFCHANNEL = "newmsg";
    private static final String TAG = "debugeo";
    private static int CHATNUMBER = 1;
    private static String RUTAFIRE;
    private static String IMAGELOCAL;
    private static Usuario OTROUSUARIO = new Usuario();
    private static final int GETIMAGE = 10;
    private static final int CAMERA_PERMISSION = 3;
    private static final int Download_PERMISSION = 4;
    private ProgressBar PBCHATIMAGEDownload;
    private ProgressBar PBCHATIMAGE;
    private static ListenerRegistration lr = null;


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
        setContentView(R.layout.activity_aitel);
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
                                case UsuarioManager.ROLE_AITEL:


                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    ArrayList<Usuario> lista = new ArrayList<>();
                                    Log.d(TAG, "EMPIEZA A BUSCAR");
                                    db.collection("users")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "on complete");
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            Log.d(TAG, "obtuvo un usuario");
                                                            Usuario usuario = document.toObject(Usuario.class);
                                                            if (!currentUser.getUid().equals(usuario.getUid()))
                                                                lista.add(usuario);
                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                        }
                                                        RecyclerView mRecyclerView = findViewById(R.id.recyclerusersAitel);
                                                        UsersRecycler uAdapter = new UsersRecycler(lista, AitelActivity.this, R.id.fragmentAitel);
                                                        mRecyclerView.setAdapter(uAdapter);
                                                        mRecyclerView.setLayoutManager(new LinearLayoutManager(AitelActivity.this));
                                                    }
                                                }
                                            });



                                    if (lr == null) {
                                        Log.d(TAG, "lr es null");
                                        lr = UsuarioManager.setChatNotif(currentUser, AitelActivity.this);
                                    } else {
                                        Log.d(TAG, "lr no es null");
                                    }

                                        TextView textViewn = findViewById(R.id.textprofileAITEL);

                                        textViewn.setText("Bienvenid@, " + currentUser.getDisplayName());
                                        break;
                                        default:
                                            startActivity(new Intent(AitelActivity.this, MainActivity.class));
                                            finish();
                                            break;

                                    }

                                    //SI VIENE DE NOTIFICACION:
                                    Intent intent = getIntent();
                                    if (intent.getSerializableExtra("usuarionotif") != null) {
                                        Usuario usuario = (Usuario) intent.getSerializableExtra("usuarionotif");
                                        UsuarioManager.openPrivateChat(usuario, AitelActivity.this, R.id.fragmentAitel, null);
                                    }
                            }

                        } else {
                            Log.d("debugeo", "get failed with ", task.getException());
                            startActivity(new Intent(AitelActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
            } else{
                startActivity(new Intent(AitelActivity.this, MainActivity.class));
                finish();
            }


        }

        public void tomarFoto (Usuario usuario, ProgressBar pb){

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
//    public void guardarFotoTomada(Bitmap bitmap){
//        device.setNombreFoto("prueba.jpg");
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, device.getNombreFoto());
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
//            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//            uri  = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//            try(OutputStream outputStream = getContentResolver().openOutputStream(uri)){
//                bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//
//        }
//    }


        public void downloadIMAGE (String rutaFire, String nombrelocal){
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
                        Toast.makeText(AitelActivity.this, "Archivo Descargado", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "ERROR DESCARGA");
                        Toast.makeText(AitelActivity.this, "No se pudo descargar", Toast.LENGTH_LONG).show();
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
        public void onRequestPermissionsResult ( int requestCode,
        @NonNull String[] permissions,
        @NonNull int[] grantResults){
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
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case GETIMAGE:
                    if (resultCode == RESULT_OK) {
                        PBCHATIMAGE.setVisibility(View.VISIBLE);
                        UsuarioManager.saveImageStorage(AitelActivity.this, data, OTROUSUARIO);
                    }
                    break;
            }
        }

        public void setFloatingButton () {
            // Register all the FABs with their IDs
            // This FAB button is the Parent
            minfo = findViewById(R.id.info);
            // FAB button
            mlogout = findViewById(R.id.logout);

            // Also register the action name text, of all the FABs.
            logoutActionText = findViewById(R.id.logout_action_text);

            // Now set all the FABs and all the action name
            // texts as GONE
            mlogout.setVisibility(View.GONE);
            logoutActionText.setVisibility(View.GONE);


            isAllFabsVisible = false;

            minfo.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!isAllFabsVisible) {
                                mlogout.show();
                                logoutActionText.setVisibility(View.VISIBLE);
                                isAllFabsVisible = true;
                            } else {
                                mlogout.hide();
                                logoutActionText.setVisibility(View.GONE);
                                isAllFabsVisible = false;
                            }
                        }
                    });

            mlogout.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UsuarioManager.logout(AitelActivity.this);
                        }
                    });


        }


    }