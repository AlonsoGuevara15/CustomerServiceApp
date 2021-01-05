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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

import pe.edu.pucp.customerserviceapp.aitel.AitelActivity;
import pe.edu.pucp.customerserviceapp.clases.Chat;
import pe.edu.pucp.customerserviceapp.clases.Usuario;
import pe.edu.pucp.customerserviceapp.student.StudentActivity;

public class ChatRecycler extends RecyclerView.Adapter<ChatRecycler.MessageViewHolder> {

    private static ArrayList<Chat> lista;
    private static final String TAG = "debugeo";
    private Context contexto;
    private String nombreotro;

    public ChatRecycler(ArrayList<Chat> l, Context contexto, String nombreotro) {
        lista = l;
        this.contexto = contexto;
        this.nombreotro = nombreotro;
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
        String chatid = lista.get(position).getChatid();



        LocalDateTime lc = lista.get(position).getFecha().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        String date = lc.format(formatter);
        Boolean isImg = lista.get(position).getAttachedImg();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (contexto instanceof StudentActivity) {
                        StudentActivity act = (StudentActivity) contexto;
                        act.downloadIMAGE("imageschat/"+chatid+".jpg",nombreotro+"_"+chatid + ".jpg");
                    } else if (contexto instanceof AitelActivity) {
                        AitelActivity act = (AitelActivity) contexto;
                        act.downloadIMAGE("imageschat/"+chatid+".jpg",nombreotro+"_"+chatid + ".jpg");
                    }

                }
            };

            if (lista.get(position).getSenderId().equals(currentUser.getUid())) {
                holder.msgIN.setVisibility(View.GONE);
                holder.imageIN.setVisibility(View.GONE);


                if (isImg) {
                    holder.imageOUT.setVisibility(View.VISIBLE);
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("imageschat/" + chatid + ".jpg");
                    Glide.with(contexto).load(imageRef).into(holder.imageOUT);

                    holder.msgOUT.setVisibility(View.GONE);
                    holder.imageOUT.setOnClickListener(onClickListener);
                } else {
                    holder.msgOUT.setVisibility(View.VISIBLE);
                    holder.msgOUT.setText(msg);
                    holder.imageOUT.setVisibility(View.GONE);

                }
                holder.dateOUT.setVisibility(View.VISIBLE);
                holder.dateIN.setVisibility(View.GONE);
                holder.dateOUT.setText(date);
            } else {
                holder.msgOUT.setVisibility(View.GONE);
                holder.imageOUT.setVisibility(View.GONE);

                if (isImg) {
                    holder.imageIN.setVisibility(View.VISIBLE);
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("imageschat/" + chatid + ".jpg");
                    Glide.with(contexto).load(imageRef).into(holder.imageIN);
                    holder.msgIN.setVisibility(View.GONE);
                    holder.imageIN.setOnClickListener(onClickListener);
                } else {
                    holder.msgIN.setVisibility(View.VISIBLE);
                    holder.msgIN.setText(msg);
                    holder.imageIN.setVisibility(View.GONE);
                }

                holder.dateIN.setVisibility(View.VISIBLE);
                holder.dateOUT.setVisibility(View.GONE);
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
        public ImageView imageIN;
        public ImageView imageOUT;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.msgIN = itemView.findViewById(R.id.textMessageIN);
            this.msgOUT = itemView.findViewById(R.id.textMessageOUT);
            this.dateIN = itemView.findViewById(R.id.fechaIN);
            this.dateOUT = itemView.findViewById(R.id.fechaOUT);
            this.imageIN = itemView.findViewById(R.id.imageIN);
            this.imageOUT = itemView.findViewById(R.id.imageOUT);
//            this.icon = itemView.findViewById(R.id.iconArchivo);
//            this.button = itemView.findViewById(R.id.floatingActionButton);

        }
    }


}
