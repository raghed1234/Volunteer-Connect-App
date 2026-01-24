package com.example.volunteerconnectapp.models;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView;

import com.bumptech.glide.Glide;
import com.example.volunteerconnectapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VolunteerRegistrationAdapter extends BaseAdapter {

    private static final String TAG = "VolunteerRegAdapter";

    private Context context;
    private List<Opportunity> registrations;
    private LayoutInflater inflater;

    public VolunteerRegistrationAdapter(Context context, List<Opportunity> registrations) {
        this.context = context;
        this.registrations = registrations;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return registrations.size();
    }

    @Override
    public Object getItem(int position) {
        return registrations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_registration, parent, false);

            // Ensure proper ListView layout behavior
            convertView.setLayoutParams(new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT
            ));

            holder = new ViewHolder();
            holder.ivOrgLogo = convertView.findViewById(R.id.ivOrgLogo);
            holder.ivOpportunity = convertView.findViewById(R.id.ivOpportunity);
            holder.tvTitle = convertView.findViewById(R.id.tvTitle);
            holder.tvOrgName = convertView.findViewById(R.id.tvOrgName);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvDate = convertView.findViewById(R.id.tvDate);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Opportunity opportunity = registrations.get(position);

        // Title & org name
        holder.tvTitle.setText(opportunity.getTitle());
        holder.tvOrgName.setText(opportunity.getOrganizationName());

        // Date
        holder.tvDate.setText(formatDate(opportunity.getStartDate()));

        // Status
        String status = opportunity.getStatus();
        holder.tvStatus.setText(capitalizeFirst(status));
        holder.tvStatus.setBackgroundColor(getStatusColor(status));

        // ===============================
        // Organization logo (FIXED)
        // ===============================
        if (opportunity.getOrganizationLogo() != null
                && !opportunity.getOrganizationLogo().isEmpty()) {

            String logoUrl = ApiHelper.getProfileImageUrl(
                    "organization",
                    opportunity.getOrganizationLogo()
            );

            Log.d(TAG, "Loading org logo: " + logoUrl);

            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_organization)
                    .error(R.drawable.ic_organization)
                    .circleCrop()
                    .into(holder.ivOrgLogo);

        } else {
            holder.ivOrgLogo.setImageResource(R.drawable.ic_organization);
        }

        // ===============================
        // Opportunity image (FIXED)
        // ===============================
        if (opportunity.getImageUrl() != null
                && !opportunity.getImageUrl().isEmpty()) {

            String imageUrl = ApiHelper.getOpportunityImageUrl(
                    opportunity.getImageUrl()
            );

            Log.d(TAG, "Loading opportunity image: " + imageUrl);

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_volunteer)
                    .error(R.drawable.placeholder_volunteer)
                    .centerCrop()
                    .into(holder.ivOpportunity);

        } else {
            holder.ivOpportunity.setImageResource(R.drawable.placeholder_volunteer);
        }

        return convertView;
    }

    // ===============================
    // Helpers
    // ===============================

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            );
            SimpleDateFormat output = new SimpleDateFormat(
                    "MMM dd, yyyy",
                    Locale.getDefault()
            );
            Date date = input.parse(dateStr);
            return output.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase()
                + text.substring(1).toLowerCase();
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "approved":
                return Color.parseColor("#4CAF50"); // Green
            case "pending":
                return Color.parseColor("#FF9800"); // Orange
            case "rejected":
                return Color.parseColor("#F44336"); // Red
            case "cancelled":
                return Color.parseColor("#9E9E9E"); // Gray
            default:
                return Color.parseColor("#2196F3"); // Blue
        }
    }

    static class ViewHolder {
        ImageView ivOrgLogo;
        ImageView ivOpportunity;
        TextView tvTitle;
        TextView tvOrgName;
        TextView tvStatus;
        TextView tvDate;
    }
}
