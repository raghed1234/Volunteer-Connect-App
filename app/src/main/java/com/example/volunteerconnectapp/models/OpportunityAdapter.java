package com.example.volunteerconnectapp.models;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.volunteerconnectapp.ProfileActivity;
import com.example.volunteerconnectapp.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OpportunityAdapter extends RecyclerView.Adapter<OpportunityAdapter.ViewHolder> {

    private Context context;
    private List<Opportunity> opportunities;
    private OnRegisterClickListener listener;
    private OnOpportunityDeletedListener deleteListener;
    private String userType;
    private int loggedInUserId;

    public interface OnRegisterClickListener {
        void onRegisterClick(Opportunity opportunity, int position);
    }

    public interface OnOpportunityDeletedListener {
        void onOpportunityDeleted();
    }

    public OpportunityAdapter(Context context, List<Opportunity> opportunities, OnRegisterClickListener listener) {
        this.context = context;
        this.opportunities = opportunities;
        this.listener = listener;

        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userType = prefs.getString("user_type", "volunteer"); // default to volunteer
        loggedInUserId = prefs.getInt("user_id", -1);
    }

    public void setOnOpportunityDeletedListener(OnOpportunityDeletedListener listener) {
        this.deleteListener = listener;
    }

    private void runOnUiThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
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

        holder.tvDate.setText(formatDate(opportunity.getStartDate()));

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
                holder.tvDescription.setText(description.substring(0, 100) + "...");
                holder.tvReadMore.setText("Read more");
            } else {
                holder.tvDescription.setText(description);
                holder.tvReadMore.setText("Read less");
            }
            holder.isExpanded = !holder.isExpanded;
        });


        if (opportunity.getOrganizationLogo() != null && !opportunity.getOrganizationLogo().isEmpty()) {
            Glide.with(context)
                    .load(ApiHelper.getProfileImageUrl("organization", opportunity.getOrganizationLogo()))
                    .placeholder(R.drawable.ic_organization)
                    .error(R.drawable.ic_organization)
                    .circleCrop()
                    .into(holder.ivOrgLogo);
        } else {
            holder.ivOrgLogo.setImageResource(R.drawable.ic_organization);
        }


        if (opportunity.getImageUrl() != null && !opportunity.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(ApiHelper.getOpportunityImageUrl(opportunity.getImageUrl()))
                    .placeholder(R.drawable.placeholder_volunteer)
                    .error(R.drawable.placeholder_volunteer)
                    .centerCrop()
                    .into(holder.ivOpportunity);
        } else {
            holder.ivOpportunity.setImageResource(R.drawable.placeholder_volunteer);
        }


        if (userType.equals("organization")) {
            holder.btnRegister.setVisibility(View.GONE);
        } else {
            holder.btnRegister.setVisibility(View.VISIBLE);

            if (opportunity.isRegistered()) {
                holder.btnRegister.setText("Registered");
                holder.btnRegister.setBackgroundResource(R.drawable.button_registered);
            } else {
                holder.btnRegister.setText("Register");
                holder.btnRegister.setBackgroundResource(R.drawable.button_primary);
            }

            holder.btnRegister.setOnClickListener(v -> {
                if (opportunity.isRegistered()) {
                    showUnregisterDialog(opportunity, position);
                } else if (listener != null) {
                    listener.onRegisterClick(opportunity, position);
                }
            });
        }


        if (userType.equals("organization") && opportunity.getOrgId() == loggedInUserId) {
            holder.itemView.setOnLongClickListener(v -> {
                showDeleteDialog(opportunity, position);
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
    }

    private void showUnregisterDialog(Opportunity opportunity, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Unregister")
                .setMessage("Cancel registration for \"" + opportunity.getTitle() + "\"?")
                .setPositiveButton("Yes", (d, w) -> unregisterFromOpportunity(opportunity, position))
                .setNegativeButton("No", null)
                .show();
    }

    private void unregisterFromOpportunity(Opportunity opportunity, int position) {
        ApiHelper.unregisterFromOpportunity(context, opportunity.getId(), new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            opportunity.setRegistered(false);
                            notifyItemChanged(position);
                            Toast.makeText(context, "Registration cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(context, "Failed: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showDeleteDialog(Opportunity opportunity, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Opportunity")
                .setMessage("Delete \"" + opportunity.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> deleteOpportunity(opportunity, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOpportunity(Opportunity opportunity, int position) {
        ApiHelper.deleteOpportunity(context, opportunity.getId(), loggedInUserId, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            opportunities.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, opportunities.size());
                            Toast.makeText(context, "Opportunity deleted", Toast.LENGTH_SHORT).show();
                            if (deleteListener != null) deleteListener.onOpportunityDeleted();
                        } else {
                            Toast.makeText(context, json.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(context, "Delete failed: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return opportunities.size();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return out.format(in.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout orgHeader;
        ImageView ivOrgLogo, ivOpportunity;
        TextView tvOrgName, tvTitle, tvDescription, tvLocation, tvDate, tvReadMore;
        Button btnRegister;
        boolean isExpanded;

        ViewHolder(View v) {
            super(v);
            orgHeader = v.findViewById(R.id.orgHeader);
            ivOrgLogo = v.findViewById(R.id.ivOrgLogo);
            ivOpportunity = v.findViewById(R.id.ivOpportunity);
            tvOrgName = v.findViewById(R.id.tvOrgName);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDescription = v.findViewById(R.id.tvDescription);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvDate = v.findViewById(R.id.tvDate);
            tvReadMore = v.findViewById(R.id.tvReadMore);
            btnRegister = v.findViewById(R.id.btnRegister);
        }
    }
}
