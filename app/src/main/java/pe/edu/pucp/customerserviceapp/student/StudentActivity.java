package pe.edu.pucp.customerserviceapp.student;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import pe.edu.pucp.customerserviceapp.ChatFragment;
import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.admin.AdminActivity;
import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;
import pe.edu.pucp.customerserviceapp.clases.Chat;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class StudentActivity extends AppCompatActivity {
    private static final String TAG = "debugeo";
    private static final String NOTIFCHANNEL = "newmsg";

    // Make sure to use the FloatingActionButton
    // for all the FABs
    FloatingActionButton minfo, mlogout, mupgrade;

    // These are taken to make visible and invisible along
    // with FABs
    TextView logoutActionText, upgradeActionText;
    // to check whether sub FAB buttons are visible or not.
    Boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        setFloatingButton();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFCHANNEL,
                    "Notificaciones de Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription("Notificaciones de nuevo mensaje recibido");
            notificationManager.createNotificationChannel(notificationChannel);
        }


        if (currentUser != null) {
            UsuarioManager.openPrivateChat(new Usuario("RkYcwQgYFCXyuYVKjKcNQUbamxi1", "AITEL PRUEBA", ""), StudentActivity.this, R.id.fragmentStudent);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("chats")
                    .whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("readbyreceiver", false)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "listen:error", e);
                                return;
                            }

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        Chat chat = dc.getDocument().toObject(Chat.class);
                                        DocumentReference docRef = db.collection("users").document(chat.getSenderId());
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Usuario user = document.toObject(Usuario.class);
                                                        NotificationCompat.Builder builder =
                                                                new NotificationCompat.Builder(StudentActivity.this, NOTIFCHANNEL);
                                                        builder.setSmallIcon(R.mipmap.ic_launcher_round);
                                                        builder.setContentTitle("AITELChat");
                                                        builder.setContentText(user.getNombre() + ": "+chat.getMsg());
                                                        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                                                        notificationManager.notify(1, builder.build());
                                                    }
                                                } else {
                                                    Log.d("debugeo", "get failed with ", task.getException());
                                                }
                                            }
                                        });
                                        Log.d(TAG, "New msg: " + dc.getDocument().getData());
                                        break;
                                    case MODIFIED:
                                        Log.d(TAG, "Modified msg: " + dc.getDocument().getData());
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "Removed msg: " + dc.getDocument().getData());
                                        break;
                                }
                            }
                        }
                    });


//            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
//            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                    @Nullable FirebaseFirestoreException e) {
//                    if (e != null) {
//                        Log.d(TAG, "Listen failed.", e);
//                        return;
//                    }
//
//                    if (snapshot != null && snapshot.exists()) {
//                        if (snapshot.get("currently").equals("atendiendo") && !snapshot.get("chatWith").equals("")) {
//                            String receiveruid = (String) snapshot.get("chatWith");
//                            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(receiveruid);
//                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//                                        if (document.exists()) {
//                                            String nombrereceiver = (String) document.get("nombre");
//                                            ChatFragment chatFragmentFragment = ChatFragment.newInstance(currentUser.getUid(), receiveruid, nombrereceiver,new ArrayList<Chat>());
//                                            getSupportFragmentManager().beginTransaction()
//                                                    .add(R.id.fragmentStudent, chatFragmentFragment)
//                                                    .commit();
//
//                                        }
//                                    } else {
//                                        Log.d("debugeo", "get failed with ", task.getException());
//                                        startActivity(new Intent(StudentActivity.this, MainActivity.class));
//                                    }
//                                }
//                            });
//
//
//                        }
//
//                    } else {
//                        Log.d(TAG, "Current data: null");
//                    }
//                }
//            });
        } else {
            startActivity(new Intent(StudentActivity.this, MainActivity.class));
            finish();
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
        mupgrade = findViewById(R.id.upgrade);

        // Also register the action name text, of all the FABs.
        logoutActionText = findViewById(R.id.logout_action_text);
        upgradeActionText = findViewById(R.id.upgrade_action_text);

        // Now set all the FABs and all the action name
        // texts as GONE
        mlogout.setVisibility(View.GONE);
        mupgrade.setVisibility(View.GONE);
        logoutActionText.setVisibility(View.GONE);
        upgradeActionText.setVisibility(View.GONE);


        isAllFabsVisible = false;

        minfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {
                            mlogout.show();
                            mupgrade.show();
                            logoutActionText.setVisibility(View.VISIBLE);
                            upgradeActionText.setVisibility(View.VISIBLE);
                            isAllFabsVisible = true;
                        } else {
                            mlogout.hide();
                            mupgrade.hide();
                            logoutActionText.setVisibility(View.GONE);
                            upgradeActionText.setVisibility(View.GONE);
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
        mupgrade.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(StudentActivity.this, "clickeado el upgrade", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
            return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network networks = connectivityManager.getActiveNetwork();
            if (networks == null)
                return false;

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(networks);
            if (networkCapabilities == null)
                return false;

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true;
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return true;
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                return true;
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo == null)
                return false;
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return true;
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return true;
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_ETHERNET)
                return true;

        }
        return false;
    }

}