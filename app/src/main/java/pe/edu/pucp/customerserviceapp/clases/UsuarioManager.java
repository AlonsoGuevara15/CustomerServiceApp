package pe.edu.pucp.customerserviceapp.clases;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.admin.AdminActivity;
import pe.edu.pucp.customerserviceapp.client.ClientActivity;
import pe.edu.pucp.customerserviceapp.employee.EmployeeActivity;

public class UsuarioManager {
    public static final int ROLE_PENDING = 1;
    public static final int ROLE_CLIENT = 2;
    public static final int ROLE_EMPLOYEE = 3;
    public static final int ROLE_ADMIN = 4;

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
                        Usuario user = new Usuario(currentUser.getUid(), currentUser.getDisplayName());
                        docRef.set(user);
                        activity.startActivity(new Intent(activity, ClientActivity.class));
                    } else {
                        int rol = document.get("rol", Integer.class);
                        switch (rol) {
                            case ROLE_EMPLOYEE:
                                activity.startActivity(new Intent(activity, EmployeeActivity.class));
                                break;
                            case ROLE_ADMIN:
                                activity.startActivity(new Intent(activity, AdminActivity.class));
                                break;
                            default:
                                activity.startActivity(new Intent(activity, ClientActivity.class));
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
