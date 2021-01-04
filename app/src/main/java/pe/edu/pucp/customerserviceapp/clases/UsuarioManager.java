package pe.edu.pucp.customerserviceapp.clases;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import pe.edu.pucp.customerserviceapp.ChatFragment;
import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.admin.AdminActivity;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;
import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;

public class UsuarioManager {
    private static final String TAG = "debugeo";
    public static final String ROLE_PENDING = "pendiente";
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_AITEL = "aitel";
    public static final String ROLE_ADMIN = "admin";

    public static void openUserMenu(Activity activity) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Usuario user = new Usuario(currentUser.getUid(), currentUser.getDisplayName(),currentUser.getEmail());
                        docRef.set(user);
                        activity.startActivity(new Intent(activity, StudentActivity.class));
                    } else {
                        String rol = (String) document.get("rol");
                        switch (rol) {
                            case ROLE_AITEL:
                                activity.startActivity(new Intent(activity, AitelActivity.class));
                                break;
                            case ROLE_ADMIN:
                                activity.startActivity(new Intent(activity, AdminActivity.class));
                                break;
                            default:
                                activity.startActivity(new Intent(activity, StudentActivity.class));
                                break;
                        }
                    }
                    activity.finish();
                } else {
                    Log.d("debugeo", "get failed with ", task.getException());
                    activity.startActivity(new Intent(activity, MainActivity.class));
                }
            }
        });
    }
    public static void openPrivateChat(Usuario otheruser, FragmentActivity act, int fragmentid) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            ArrayList<Chat> lista = new ArrayList<>();
            Log.d(TAG, "EMPIEZA A BUSCAR");
            db.collection("chats")
                    .whereEqualTo("senderId", currentUser.getUid()).whereEqualTo("receiverId", otheruser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "on complete");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, "obtuvo un chat");
                                    Chat chatenviado = document.toObject(Chat.class);
                                    lista.add(chatenviado);
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                                db.collection("chats")
                                        .whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("senderId", otheruser.getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Chat chatrecibido = document.toObject(Chat.class);
                                                        if(!chatrecibido.getReadbyreceiver()){
                                                            chatrecibido.setReadbyreceiver(true);
                                                            document.getReference().set(chatrecibido);
                                                        }
                                                        lista.add(chatrecibido);

                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                    }
                                                    Collections.sort(lista);
                                                    ChatFragment cFragment = ChatFragment.newInstance(otheruser.getUid(), otheruser.getNombre(), lista);
                                                    act.getSupportFragmentManager().beginTransaction()
                                                            .add(fragmentid, cFragment)
                                                            .commit();

                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });

        }

    }




    public static void logout(Activity activity) {
        AuthUI instance = AuthUI.getInstance();
        instance.signOut(activity).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
        });
    }
}
