package pe.edu.pucp.customerserviceapp.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import pe.edu.pucp.customerserviceapp.R;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class AdminActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_admin);
        setFloatingButton();
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
                        UsuarioManager.logout(AdminActivity.this);
                    }
                });


    }

}