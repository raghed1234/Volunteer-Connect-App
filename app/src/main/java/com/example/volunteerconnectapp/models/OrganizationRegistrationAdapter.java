package com.example.volunteerconnectapp.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.VolunteerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrganizationRegistrationAdapter extends BaseAdapter {

    private Context context;
    private List<VolunteerRegistration> registrations;
    private LayoutInflater inflater;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onAccept(VolunteerRegistration registration, int position);
        void onReject(VolunteerRegistration registration, int position);
    }

    public OrganizationRegistrationAdapter(Context context, List<VolunteerRegistration> registrations, OnActionClickListener listener) {
        this.context = context;
        this.registrations = registrations;
        this.listener = listener;
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
            convertView = inflater.inflate(R.layout.item_organization_registration, parent, false);
            holder = new ViewHolder();
            holder.tvVolunteerName = convertView.findViewById(R.id.tvVolunteerName);
            holder.tvOpportunityTitle = convertView.findViewById(R.id.tvOpportunityTitle);
            holder.tvVolunteerEmail = convertView.findViewById(R.id.tvVolunteerEmail);
            holder.tvVolunteerPhone = convertView.findViewById(R.id.tvVolunteerPhone);
            holder.tvVolunteerSkills = convertView.findViewById(R.id.tvVolunteerSkills);
            holder.tvRegisteredDate = convertView.findViewById(R.id.tvRegisteredDate);
            holder.tvLocation = convertView.findViewById(R.id.tvLocation);
            holder.tvStartDate = convertView.findViewById(R.id.tvStartDate);
            holder.btnAccept = convertView.findViewById(R.id.btnAccept);
            holder.btnReject = convertView.findViewById(R.id.btnReject);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VolunteerRegistration registration = registrations.get(position);

        holder.tvVolunteerName.setText(registration.getVolunteerName());
        holder.tvOpportunityTitle.setText(registration.getOpportunityTitle());


        if (registration.getVolunteerEmail() != null && !registration.getVolunteerEmail().isEmpty()) {
            holder.tvVolunteerEmail.setText("📧 " + registration.getVolunteerEmail());
            holder.tvVolunteerEmail.setVisibility(View.VISIBLE);
        } else {
            holder.tvVolunteerEmail.setVisibility(View.GONE);
        }


        if (registration.getVolunteerPhone() != null && !registration.getVolunteerPhone().isEmpty()) {
            holder.tvVolunteerPhone.setText("📱 " + registration.getVolunteerPhone());
            holder.tvVolunteerPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvVolunteerPhone.setVisibility(View.GONE);
        }


        if (registration.getVolunteerSkills() != null && !registration.getVolunteerSkills().isEmpty()) {
            holder.tvVolunteerSkills.setText("💼 Skills: " + registration.getVolunteerSkills());
            holder.tvVolunteerSkills.setVisibility(View.VISIBLE);
        } else {
            holder.tvVolunteerSkills.setVisibility(View.GONE);
        }


        if (registration.getLocation() != null && !registration.getLocation().isEmpty()) {
            holder.tvLocation.setText("📍 " + registration.getLocation());
        }


        String startDate = formatDate(registration.getStartDate());
        holder.tvStartDate.setText("📅 " + startDate);


        String registeredDate = formatDate(registration.getRegisteredAt());
        holder.tvRegisteredDate.setText("Registered: " + registeredDate);


        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(registration, position);
            }
        });


        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(registration, position);
            }
        });

        return convertView;
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

    static class ViewHolder {
        TextView tvVolunteerName;
        TextView tvOpportunityTitle;
        TextView tvVolunteerEmail;
        TextView tvVolunteerPhone;
        TextView tvVolunteerSkills;
        TextView tvRegisteredDate;
        TextView tvLocation;
        TextView tvStartDate;
        Button btnAccept;
        Button btnReject;
    }
}
