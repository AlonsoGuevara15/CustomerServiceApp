package pe.edu.pucp.customerserviceapp.student;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import pe.edu.pucp.customerserviceapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WaitingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WaitingFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Boolean isWaiting=false;



    public WaitingFragment() {
        // Required empty public constructor
    }

    public static WaitingFragment newInstance() {
        WaitingFragment fragment = new WaitingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_waiting, container, false);
        Button btn = view.findViewById(R.id.btnWait);
        ProgressBar pb = view.findViewById(R.id.progressBar_wait);
        pb.setVisibility(View.GONE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isWaiting) { //Solicita
                    isWaiting=true;
                    pb.setVisibility(View.VISIBLE);
                    btn.setText(R.string.cancelar);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
                    docRef.update("currently", "pendiente");

                } else { //CANCELA
                    isWaiting=false;
                    pb.setVisibility(View.GONE);
                    btn.setText(R.string.solicitar_ayuda);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
                    docRef.update("currently", "idle");

                }


            }
        });
        return view;
    }

}