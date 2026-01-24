package com.example.volunteerconnectapp.models;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.volunteerconnectapp.ProfileActivity;
import com.example.volunteerconnectapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.ViewHolder> {

    private static final String TAG = "OpportunityAdapter";
    private Context context;
    private List<Opportunity> opportunities;
    private OnRegisterClickListener listener;

    public interface OnRegisterClickListener {
        void onRegisterClick(Opportunity opportunity, int position);
    }

    public OpportunityAdapter(Context context, List<Opportunity> opportunities, OnRegisterClickListener listener) {
        this.context = context;
        this.opportunities = opportunities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_opportunity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Opportunity opportunity = opportunities.get(position);

        holder.tvOrgName.setText(opportunity.getOrganizationName());
        holder.tvTitle.setText(opportunity.getTitle());
        holder.tvLocation.setText(opportunity.getLocation());


        holder.orgHeader.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("user_id", opportunity.getOrgId());
            intent.putExtra("user_type", "organization");
            context.startActivity(intent);
        });


        String dateText = formatDate(opportunity.getStartDate());
        holder.tvDate.setText(dateText);


        String description = opportunity.getDescription();
        holder.isExpanded = false;

        if (description.length() > 100) {
            holder.tvDescription.setText(description.substring(0, 100) + "...");
            holder.tvReadMore.setVisibility(View.VISIBLE);
            holder.tvReadMore.setText("Read more");
        } else {
            holder.tvDescription.setText(description);
            holder.tvReadMore.setVisibility(View.GONE);
        }


        holder.tvReadMore.setOnClickListener(v -> {
            if (holder.isExpanded) {
                // Collapse
                holder.tvDescription.setText(description.substring(0, 100) + "...");
                holder.tvReadMore.setText("Read more");
                holder.isExpanded = false;
            } else {
                // Expand
                holder.tvDescription.setText(description);
                holder.tvReadMore.setText("Read less");
                holder.isExpanded = true;
            }
        });


        if (opportunity.getOrganizationLogo() != null && !opportunity.getOrganizationLogo().isEmpty()) {
            String logoUrl = ApiHelper.getProfileImageUrl("organization", opportunity.getOrganizationLogo());
            Log.d(TAG, "Loading organization logo: " + logoUrl);
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_organization)
                    .error(R.drawable.ic_organization)
                    .circleCrop()
                    .into(holder.ivOrgLogo);
        } else {
            holder.ivOrgLogo.setImageResource(R.drawable.ic_organization);
        }


        if (opportunity.getImageUrl() != null && !opportunity.getImageUrl().isEmpty()) {
            String imageUrl = ApiHelper.getOpportunityImageUrl(opportunity.getImageUrl());
            Log.d(TAG, "Loading opportunity image: " + imageUrl);
            Log.d(TAG, "Image filename: " + opportunity.getImageUrl());

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_volunteer)
                    .error(R.drawable.placeholder_volunteer)
                    .centerCrop()
                    .into(holder.ivOpportunity);
        } else {
            holder.ivOpportunity.setImageResource(R.drawable.placeholder_volunteer);
        }

        // Set button state
        if (opportunity.isRegistered()) {
            holder.btnRegister.setText("Registered");
            holder.btnRegister.setEnabled(false);
            holder.btnRegister.setBackgroundResource(R.drawable.button_registered);
        } else {
            holder.btnRegister.setText("Register");
            holder.btnRegister.setEnabled(true);
            holder.btnRegister.setBackgroundResource(R.drawable.button_primary);
        }

        holder.btnRegister.setOnClickListener(v -> {
            if (!opportunity.isRegistered() && listener != null) {
                listener.onRegisterClick(opportunity, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout orgHeader;
        ImageView ivOrgLogo, ivOpportunity;
        TextView tvOrgName, tvTitle, tvDescription, tvLocation, tvDate, tvReadMore;
        Button btnRegister;
        boolean isExpanded = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orgHeader = itemView.findViewById(R.id.orgHeader);
            ivOrgLogo = itemView.findViewById(R.id.ivOrgLogo);
            ivOpportunity = itemView.findViewById(R.id.ivOpportunity);
            tvOrgName = itemView.findViewById(R.id.tvOrgName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvReadMore = itemView.findViewById(R.id.tvReadMore);
            btnRegister = itemView.findViewById(R.id.btnRegister);
        }
    }
}