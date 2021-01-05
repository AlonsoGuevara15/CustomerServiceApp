package pe.edu.pucp.customerserviceapp;

import android.app.Activity;
import android.content.Context;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.clases.UsuarioManager;

public class UsersRecycler extends RecyclerView.Adapter<UsersRecycler.UserViewHolder>{
    private static ArrayList<Usuario> lista;
    private static final String TAG = "debugeo";
    private Context contexto;
    private static int fragmentid;

    public UsersRecycler(ArrayList<Usuario> l,Context contexto,int fi) {
        lista = l;
        this.contexto = contexto;
        fragmentid = fi;
    }

    public UsersRecycler() {

    }

    @NonNull
    @Override
    public UsersRecycler.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        UserViewHolder mViewHolder = new UserViewHolder(itemView);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Usuario usuario = lista.get(position);
        holder.setUserViewHolder(usuario.getNombre(),usuario.getCorreo(),usuario.getRol());
        holder.see.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.see.setEnabled(false);
                UsuarioManager.openPrivateChat(usuario, (FragmentActivity) contexto,fragmentid, holder.see);
            }
        });
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre;
        public TextView correo;
        public TextView rol;
        public ImageButton see;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            this.nombre = itemView.findViewById(R.id.nombre);
            this.correo = itemView.findViewById(R.id.correo);
            this.rol = itemView.findViewById(R.id.rol);
            this.see = itemView.findViewById(R.id.see_chat);
        }

        public void setUserViewHolder(String nombre, String correo, String rol) {
            this.nombre.setText(nombre);
            this.correo.setText(correo);
            this.rol.setText("Rol: "+rol);
        }
    }
}
