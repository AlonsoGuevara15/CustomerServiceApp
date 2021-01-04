package pe.edu.pucp.customerserviceapp.aitel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import pe.edu.pucp.customerserviceapp.ChatFragment;
import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.clases.Chat;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;

public class AitelActivity extends AppCompatActivity {

    private static final String NOTIFCHANNEL = "newmsg";
    private static final String TAG = "debugeo";
    private static int CHATNUMBER = 1;

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
            UsuarioManager.openPrivateChat(new Usuario("NzTAXz8aNccZ6ToXsT5ngvfz3qi2", "STUDENT PRUEBA", ""), AitelActivity.this, R.id.fragmentAitel);
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
                            //mgghermoza@gmail.com
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
                                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(AitelActivity.this, NOTIFCHANNEL);
                                                        builder.setSmallIcon(R.mipmap.ic_launcher_round);
                                                        builder.setContentTitle("AITELChat");
                                                        builder.setContentText(user.getNombre() + ": " + chat.getMsg());
                                                        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                                                        notificationManager.notify(CHATNUMBER, builder.build());
                                                        CHATNUMBER++;
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
        } else {
            startActivity(new Intent(AitelActivity.this, MainActivity.class));
            finish();
        }


    }


    public void setFloatingButton() {
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