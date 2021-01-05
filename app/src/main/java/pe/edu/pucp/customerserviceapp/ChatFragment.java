package pe.edu.pucp.customerserviceapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import pe.edu.pucp.customerserviceapp.admin.AdminActivity;
import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;
import pe.edu.pucp.customerserviceapp.clases.Chat;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static Usuario other;
    private static final int GETIMAGE = 10;
    static RecyclerView mRecyclerView;
    private static ArrayList<Chat> lista = new ArrayList<>();
    private static int fragmentid;
    private static ImageButton imageButton;
    private static final String TAG = "debugeo";
    private ChatRecycler crAdapter;
    ListenerRegistration lrout;
    ListenerRegistration lrin;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(Usuario o, ArrayList<Chat> l, ImageButton ib,int fid) {
        ChatFragment fragment = new ChatFragment();
        other = o;
        lista = l;
        imageButton = ib;
        fragmentid=fid;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (lrin != null) {
            lrin.remove();

        }
        if (lrout != null) {
            lrout.remove();
        }
        getActivity().findViewById(fragmentid).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        TextView nombre = view.findViewById(R.id.receiverName);
        ImageButton btnsend = view.findViewById(R.id.send_message);
        ImageButton btnsendimage = view.findViewById(R.id.send_image);
        ProgressBar progressBar = view.findViewById(R.id.progressBarchat);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            //IN MESSAGES LISTENER
            Query queryin = db.collection("chats").whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("senderId", other.getUid()).whereEqualTo("chatid", "").whereEqualTo("readbyreceiver", false);
            lrin = queryin.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Chat chat = dc.getDocument().toObject(Chat.class);

                                if (!chat.getAttachedImg()) {
                                    addRow(chat);
                                    refreshRV();
                                    chat.setReadbyreceiver(true);
                                    dc.getDocument().getReference().set(chat);
                                    Log.d(TAG, "ADD in text msg: " + dc.getDocument().getData());
                                } else {
                                    dc.getDocument().getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                            if (error != null) {
                                                Log.w(TAG, "listen:error", error);
                                                return;
                                            }
                                            if (snapshot != null && snapshot.exists()) {
                                                Chat chatnew = snapshot.toObject(Chat.class);

                                                if (chatnew.getImageloaded() && !chatnew.getReadbyreceiver() && !chatnew.getChatid().equals("")) {
                                                    Log.d(TAG, "Modified in img msg: " + snapshot.getData());
                                                    addRow(chatnew);
                                                    refreshRV();
                                                    chatnew.setReadbyreceiver(true);
                                                    snapshot.getReference().set(chatnew);

                                                }
                                            }
                                        }
                                    });
                                }


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
            //OUT MESSAGES LISTENER
            Query queryout = db.collection("chats").whereEqualTo("senderId", currentUser.getUid()).whereEqualTo("receiverId", other.getUid()).whereEqualTo("chatid", "").whereEqualTo("readbyreceiver", false);
            lrout = queryout.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Chat chat = dc.getDocument().toObject(Chat.class);
                                if (!chat.getAttachedImg()) {
                                    Log.d(TAG, "ADD out text msg: " + dc.getDocument().getData());
                                    Toast.makeText(getContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    addRow(chat);
                                    refreshRV();
                                    chat.setReadbyreceiver(true);
                                    dc.getDocument().getReference().set(chat);

                                } else {
                                    dc.getDocument().getReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                                            if (error != null) {
                                                Log.w(TAG, "listen:error", error);
                                                return;
                                            }
                                            if (snapshot != null && snapshot.exists()) {
                                                Chat chat = snapshot.toObject(Chat.class);

                                                if (chat.getImageloaded() && !chat.getReadbyreceiver() && !chat.getChatid().equals("")) {
                                                    Log.d(TAG, "Modified out img msg: " + snapshot.getData());
                                                    Toast.makeText(getContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    addRow(chat);
                                                    refreshRV();
                                                    chat.setReadbyreceiver(true);
                                                    snapshot.getReference().set(chat);

                                                }
                                            }
                                        }
                                    });

                                }

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


            nombre.setText(other.getNombre());
            mRecyclerView = view.findViewById(R.id.chatRecycler);
            crAdapter = new ChatRecycler(lista, getContext(),other.getNombre());
            mRecyclerView.setAdapter(crAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            refreshRV();
            EditText txtmsg = view.findViewById(R.id.textMsg);
            btnsend.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {

                    String newmsg = String.valueOf(txtmsg.getText()).trim();
                    if (newmsg.length() != 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        txtmsg.setText("");
                        Toast.makeText(getContext(), "Enviando...", Toast.LENGTH_SHORT).show();
                        Date timestamp = new Date();
                        System.out.println(timestamp);
                        //return number of milliseconds since January 1, 1970, 00:00:00 GMT
//
                        Chat newchat = new Chat(newmsg, timestamp, false, currentUser.getUid(), other.getUid());

                        db.collection("chats").add(newchat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                newchat.setChatid(documentReference.getId());

                                documentReference.set(newchat);
                            }
                        });
                    }

                }
            });
            btnsendimage.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    if (getActivity() instanceof StudentActivity) {
                        StudentActivity act = (StudentActivity) getActivity();
                        act.tomarFoto(other,progressBar);
                    } else if (getActivity() instanceof AitelActivity) {
                        AitelActivity act = (AitelActivity) getActivity();
                        act.tomarFoto(other,progressBar);
                    }


                }
            });

        }

        imageButton.setEnabled(true);
        return view;
    }

    /**
     * Adding row to existing list at end.
     */
    private void addRow(Chat chat) {
        lista.add(chat);
        Log.d(TAG, String.valueOf(lista.size()));
    }

    /**
     * Refreshing RecyclerView and scroll up dynamically
     */
    private void refreshRV() {
        mRecyclerView.getAdapter().notifyItemInserted(lista.size());
        mRecyclerView.smoothScrollToPosition(lista.size());
    }


}
