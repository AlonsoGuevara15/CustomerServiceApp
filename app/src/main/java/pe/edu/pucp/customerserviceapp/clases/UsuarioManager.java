package pe.edu.pucp.customerserviceapp.clases;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import pe.edu.pucp.customerserviceapp.ChatFragment;
import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.admin.AdminActivity;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;
import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;

public class UsuarioManager {
    private static final String TAG = "debugeo";
    private static final String NOTIFCHANNEL = "newmsg";
    private static int CHATNUMBER = 1;
    public static final String ROLE_PENDING = "pendiente";
    public static final String ROLE_STUDENT = "Estudiante";
    public static final String ROLE_AITEL = "Aitel";
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
                        Usuario user = new Usuario(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getEmail());
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

    public static void openPrivateChat(Usuario otheruser, FragmentActivity act, int fragmentid, ImageButton imageButton) {
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
                                Log.d(TAG, "acabo lista de chats");
                                db.collection("chats")
                                        .whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("senderId", otheruser.getUid())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Chat chatrecibido = document.toObject(Chat.class);
                                                        if (!chatrecibido.getReadbyreceiver()) {
                                                            chatrecibido.setReadbyreceiver(true);
                                                            document.getReference().set(chatrecibido);
                                                        }
                                                        lista.add(chatrecibido);

                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                    }
                                                    Collections.sort(lista);

                                                    FragmentManager fm = act.getSupportFragmentManager();

                                                    Fragment cFragmentOld = fm.findFragmentById(fragmentid);
                                                    if (cFragmentOld != null) {
                                                        fm.beginTransaction().remove(cFragmentOld).commit();
                                                    }
                                                    act.findViewById(fragmentid).setVisibility(View.VISIBLE);
                                                    ChatFragment cFragment = ChatFragment.newInstance(otheruser, lista,imageButton,fragmentid);
                                                    fm.beginTransaction()
                                                            .add(fragmentid, cFragment)
                                                            .addToBackStack(null)
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

    public static void saveImageStorage(Activity activity, Intent data, Usuario OTROUSUARIO) {
        Toast.makeText(activity, "Enviando...", Toast.LENGTH_SHORT).show();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataimage = baos.toByteArray();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Date timestamp = new Date();
//
            Chat newchat = new Chat("image!", timestamp, true, currentUser.getUid(), OTROUSUARIO.getUid());

            db.collection("chats").add(newchat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    String chatid = documentReference.getId();
                    newchat.setChatid(chatid);

                    documentReference.set(newchat).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("imageschat/" + chatid + ".jpg");
                            UploadTask uploadTask = imageRef.putBytes(dataimage);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    exception.printStackTrace();
                                    activity.startActivity(new Intent(activity, MainActivity.class));
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    newchat.setImageloaded(true);
                                    newchat.setFecha(new Date());
                                    documentReference.set(newchat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(activity, "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                    // ...
                                }
                            });

                        }
                    });

                }
            });


        }
    }

    public static void setChatNotif(FirebaseUser currentUser, Activity activity) {
        NotificationManager notificationManager =
                (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFCHANNEL,
                    "Notificaciones de Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription("Notificaciones de nuevo mensaje recibido");
            notificationManager.createNotificationChannel(notificationChannel);
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats")
                .whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("readbyreceiver", false).whereEqualTo("chatid","")
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
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {


                                                    Usuario user = document.toObject(Usuario.class);
                                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, NOTIFCHANNEL);
                                                    builder.setSmallIcon(R.mipmap.ic_launcher_round);
                                                    builder.setContentTitle(user.getNombre());
                                                    builder.setPriority(NotificationCompat.PRIORITY_HIGH);

                                                    if (!chat.getAttachedImg()) {
                                                        Log.d(TAG, "ADD notif text msg: " + dc.getDocument().getData());

                                                        LocalDateTime lc = chat.getFecha().toInstant()
                                                                .atZone(ZoneId.systemDefault()).toLocalDateTime();
                                                        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                                                        String date = lc.format(formatter);

                                                        builder.setContentText(date + ": " + chat.getMsg());

                                                        notificationManager.notify(CHATNUMBER, builder.build());
                                                        CHATNUMBER++;
                                                    } else {
                                                        dc.getDocument().getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                                                if (error != null) {
                                                                    Log.w(TAG, "listen:error", error);
                                                                    return;
                                                                }
                                                                if (snapshot != null && snapshot.exists()) {
                                                                    Chat chatchanged = snapshot.toObject(Chat.class);
                                                                    if (chatchanged.getImageloaded() && !chatchanged.getReadbyreceiver() && !chatchanged.getChatid().equals("")) {
                                                                        Log.d(TAG, "Modified notif img msg: " + dc.getDocument().getData());


                                                                        LocalDateTime lc = chatchanged.getFecha().toInstant()
                                                                                .atZone(ZoneId.systemDefault()).toLocalDateTime();
                                                                        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                                                                        String date2 = lc.format(formatter);

                                                                        builder.setContentText(date2 + ": " + "*Foto*");

                                                                        notificationManager.notify(CHATNUMBER, builder.build());
                                                                        CHATNUMBER++;
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });


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
    }

}
