package pe.edu.pucp.customerserviceapp.client;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import pe.edu.pucp.customerserviceapp.MainActivity;
import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "debugeo";
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
        setContentView(R.layout.activity_client);
        setFloatingButton();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        if (snapshot.get("currently").equals("atendiendo")){





                        }

                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                }
            });
        }else{
            startActivity(new Intent(ClientActivity.this, MainActivity.class));
            finish();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "CLIENTE PAUSADO");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null){
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());
            docRef.update("currently", "idle");

            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragmentclient);
            if (fragment != null) {
                fm.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }else{
            Log.d(TAG, "NO HAY USER");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "CLIENTE RESUMIDO");
        WaitingFragment wFragment = WaitingFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentclient, wFragment)
                .commit();

    }

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
                        UsuarioManager.logout(ClientActivity.this);
                    }
                });
        mupgrade.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(ClientActivity.this, "clickeado el upgrade", Toast.LENGTH_SHORT).show();
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