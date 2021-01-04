package pe.edu.pucp.customerserviceapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private static String other;
    private static String nombreother;
    static RecyclerView mRecyclerView;
    private static ArrayList<Chat> lista = new ArrayList<>();
    private static final String TAG = "debugeo";
    private ChatRecycler crAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }


    public static ChatFragment newInstance(String r, String nr, ArrayList<Chat> l) {
        ChatFragment fragment = new ChatFragment();
        other = r;
        nombreother = nr;
        lista = l;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        TextView nombre = view.findViewById(R.id.receiverName);
        ImageButton btnsend = view.findViewById(R.id.send_message);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("chats").whereEqualTo("receiverId", currentUser.getUid()).whereEqualTo("senderId", other).whereEqualTo("readbyreceiver", false).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                                addRow(chat);
                                refreshRV();
                                chat.setReadbyreceiver(true);
                                dc.getDocument().getReference().set(chat);
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


            nombre.setText(nombreother);

            mRecyclerView = view.findViewById(R.id.chatRecycler);
            crAdapter = new ChatRecycler(lista);
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
                        txtmsg.setText("");
                        Toast.makeText(getContext(), "Enviando...", Toast.LENGTH_SHORT).show();
                        Date timestamp = new Date();
                        System.out.println(timestamp);
                        //return number of milliseconds since January 1, 1970, 00:00:00 GMT
//
                        Chat newchat = new Chat(newmsg, timestamp, false, currentUser.getUid(), other);

                        db.collection("chats").add(newchat).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getContext(), "Mensaje enviado", Toast.LENGTH_SHORT).show();
                                addRow(newchat);
                                refreshRV();
                            }
                        });
                    }

                }
            });
        }
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
