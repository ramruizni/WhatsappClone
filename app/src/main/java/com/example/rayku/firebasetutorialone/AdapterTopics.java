package com.example.rayku.firebasetutorialone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.HashMap;

public final class AdapterTopics extends RecyclerView.Adapter<AdapterTopics.ViewHolder> implements Filterable {

    private ArrayList<Topic> topics, filteredData;
    private String forumID;
    private Context context;
    private StorageReference storageReference;
    SharedPreferences sharedPreferences;
    private String userID;

    AdapterTopics(ArrayList<Topic> topics, String forumID, Context context) {
        userID = FirebaseAuth.getInstance().getUid();
        sharedPreferences = context.getSharedPreferences("com.example.rayku.firebasetutorialone", Context.MODE_PRIVATE);
        this.topics = topics;
        this.forumID = forumID;
        this.context = context;
        this.storageReference = FirebaseStorage.getInstance().getReference().child("topicImages");
        filteredData = topics;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ActivityTopic.class);
                intent.putExtra("forumID", forumID);

                TextView titleView = view.findViewById(R.id.titleView);
                String topicTitle = titleView.getText().toString();
                intent.putExtra("topicTitle", topicTitle);

                TextView phantomView = view.findViewById(R.id.phantomIDView);
                String theCheat = phantomView.getText().toString();
                intent.putExtra("topicID", theCheat);

                context.startActivity(intent);

            }
        });

        return ViewHolder.newInstance(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String title = filteredData.get(position).title;
        String lastMessage = filteredData.get(position).lastMessage;
        int rating = filteredData.get(position).rating;
        String phantomCheat = filteredData.get(position).ID;

        Boolean loadImages = sharedPreferences.getBoolean(userID+"/loadImages", true);

        holder.setTitle(title);
        holder.setLastMessage(lastMessage);
        holder.setRating(rating);
        holder.cheatTheSystem(phantomCheat);
        holder.setImage(phantomCheat, storageReference, loadImages);

    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView lastMessageView;
        private final TextView ratingView;
        private final ImageView imageView;
        private final TextView phantomCheatView;

        static ViewHolder newInstance(View itemView) {
            TextView titleView = itemView.findViewById(R.id.titleView);
            TextView lastMessageView = itemView.findViewById(R.id.lastMessageView);
            TextView ratingView = itemView.findViewById(R.id.ratingView);
            ImageView imageView = itemView.findViewById(R.id.imageView);
            TextView phantomCheatView = itemView.findViewById(R.id.phantomIDView);
            return new ViewHolder(itemView, titleView, lastMessageView, ratingView, imageView, phantomCheatView);
        }

        private ViewHolder(View itemView, TextView titleView, TextView lastMessageView,
                           TextView ratingView, ImageView imageView, TextView phantomCheatView) {
            super(itemView);
            this.titleView = titleView;
            this.lastMessageView = lastMessageView;
            this.ratingView = ratingView;
            this.imageView = imageView;
            this.phantomCheatView = phantomCheatView;
        }

        public void setTitle(String text) {
            titleView.setText(text);
        }
        public void setLastMessage(String text){ lastMessageView.setText(text); }
        void setRating(int rating) {
            ratingView.setText(Integer.toString(rating));
        }
        void cheatTheSystem(String omg){ phantomCheatView.setText(omg); }
        void setImage(String topicID, StorageReference storageReference, Boolean loadImages){

            if(loadImages) {
                String imageKey = topicID + ".jpg";
                storageReference.child(imageKey).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        try {
                            GlideApp.with(itemView.getRootView().getContext())
                                    .load(uri)
                                    .fitCenter()
                                    .into(imageView);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

        }
    }

    @Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                FilterResults filterResults = new FilterResults();

                if(charSequence==null || charSequence.length()==0){
                    filterResults.values = topics;
                    filterResults.count = topics.size();
                } else{
                    ArrayList<Topic> filterResultsData = new ArrayList<>();
                    for(Topic topic : topics){
                        if(topic.title.toLowerCase().contains(charSequence.toString().toLowerCase())){
                            filterResultsData.add(topic);
                        }
                    }
                    filterResults.values = filterResultsData;
                    filterResults.count = filterResultsData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList<Topic>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}