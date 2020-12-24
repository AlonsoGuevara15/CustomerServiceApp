package pe.edu.pucp.customerserviceapp.client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class ClientActivity extends AppCompatActivity {
    // Make sure to use the FloatingActionButton
    // for all the FABs
    FloatingActionButton minfo, mlogout,mupgrade;

    // These are taken to make visible and invisible along
    // with FABs
    TextView logoutActionText,upgradeActionText;
    // to check whether sub FAB buttons are visible or not.
    Boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_client);
        setFloatingButton();
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

}