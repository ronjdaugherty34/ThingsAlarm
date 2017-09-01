package com.example.thingsalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;



class MotionEntryAdapter extends FirebaseRecyclerAdapter<MotionEntry, MotionEntryAdapter.MotionEntryViewHolder> {


    public static class MotionEntryViewHolder extends RecyclerView.ViewHolder {

        public final ImageView image;
        public final TextView time;
        public final TextView metadata;

        public MotionEntryViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.imageView1);
            this.time = itemView.findViewById(R.id.textView1);
            this.metadata = itemView.findViewById(R.id.textView2);

        }
    }


    private Context mApplicationContext;

    public MotionEntryAdapter(Context context, DatabaseReference ref) {
        super(MotionEntry.class, R.layout.motion_entry, MotionEntryViewHolder.class, ref);

        mApplicationContext = context.getApplicationContext();
    }


    @Override
    protected void populateViewHolder(MotionEntryViewHolder viewHolder, MotionEntry model, int position) {


        // Display the timestamp
        CharSequence prettyTime = DateUtils.getRelativeDateTimeString(mApplicationContext,
                model.getTimestamp(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
        viewHolder.time.setText(prettyTime);

        // Display the image
        if (model.getImage() != null) {
            // Decode image data encoded by the Cloud Vision library
            byte[] imageBytes = Base64.decode(model.getImage(), Base64.NO_WRAP | Base64.URL_SAFE);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (bitmap != null) {
                viewHolder.image.setImageBitmap(bitmap);
            } else {
                Drawable placeholder =
                        ContextCompat.getDrawable(mApplicationContext, R.drawable.ic_image);
                viewHolder.image.setImageDrawable(placeholder);
            }
        }
        // Display the metadata
        if (model.getAnnotations() != null) {
            ArrayList<String> keywords = new ArrayList<>(model.getAnnotations().keySet());

            int limit = Math.min(keywords.size(), 3);
            viewHolder.metadata.setText(TextUtils.join("\n", keywords.subList(0, limit)));
        } else {
            viewHolder.metadata.setText("no annotations yet");
        }


    }
}