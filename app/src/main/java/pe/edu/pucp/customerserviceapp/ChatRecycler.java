package pe.edu.pucp.customerserviceapp;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import pe.edu.pucp.customerserviceapp.clases.Chat;

public class ChatRecycler extends RecyclerView.Adapter<ChatRecycler.MessageViewHolder> {

    private static ArrayList<Chat> lista;
    private static final String TAG = "debugeo";

    public ChatRecycler(ArrayList<Chat> l) {
        lista = l;
    }
    public ChatRecycler() {
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        MessageViewHolder mViewHolder = new MessageViewHolder(itemView);
        return mViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String msg = lista.get(position).getMsg();


        String date = lista.get(position).getFechaStr();
        Boolean isImg = lista.get(position).getAttachedImg();

       FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (lista.get(position).getSenderId().equals(currentUser.getUid())){
                holder.msgIN.setVisibility(View.GONE);
                holder.dateIN.setVisibility(View.GONE);
                holder.msgOUT.setVisibility(View.VISIBLE);
                holder.dateOUT.setVisibility(View.VISIBLE);

                holder.msgOUT.setText(msg);
                holder.dateOUT.setText(date);

            }else{
                holder.msgIN.setVisibility(View.VISIBLE);
                holder.dateIN.setVisibility(View.VISIBLE);
                holder.msgOUT.setVisibility(View.GONE);
                holder.dateOUT.setVisibility(View.GONE);

                holder.msgIN.setText(msg);
                holder.dateIN.setText(date);

            }

        }


    }

    @Override
    public int getItemCount() {
        return lista.size();
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView msgIN;
        public TextView msgOUT;
        public TextView dateIN;
        public TextView dateOUT;
        public ImageView icon;
        public View button;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.msgIN = itemView.findViewById(R.id.textMessageIN);
            this.msgOUT = itemView.findViewById(R.id.textMessageOUT);
            this.dateIN = itemView.findViewById(R.id.fechaIN);
            this.dateOUT = itemView.findViewById(R.id.fechaOUT);
//            this.icon = itemView.findViewById(R.id.iconArchivo);
//            this.button = itemView.findViewById(R.id.floatingActionButton);

        }
    }


}
