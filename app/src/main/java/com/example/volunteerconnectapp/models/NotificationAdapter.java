package com.example.volunteerconnectapp.models;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.volunteerconnectapp.R;
import com.example.volunteerconnectapp.models.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends BaseAdapter {

    private Context context;
    private List<Notification> notifications;
    private LayoutInflater inflater;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_notification, parent, false);
            holder = new ViewHolder();
            holder.ivNotificationIcon = convertView.findViewById(R.id.ivNotificationIcon);
            holder.tvMessage = convertView.findViewById(R.id.tvMessage);
            holder.tvTime = convertView.findViewById(R.id.tvTime);
            holder.unreadIndicator = convertView.findViewById(R.id.unreadIndicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = notifications.get(position);

        // Set message
        holder.tvMessage.setText(notification.getMessage());

        // Set time
        String timeAgo = getTimeAgo(notification.getCreatedAt());
        holder.tvTime.setText(timeAgo);

        // Style based on read status
        if (!notification.isRead()) {
            // Unread notification - bold text and show indicator
            holder.tvMessage.setTypeface(null, Typeface.BOLD);
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.unreadIndicator.setVisibility(View.VISIBLE);
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.white));
        } else {
            // Read notification - normal text
            holder.tvMessage.setTypeface(null, Typeface.NORMAL);
            holder.tvMessage.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.unreadIndicator.setVisibility(View.GONE);
            convertView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        return convertView;
    }

    private String getTimeAgo(String dateTimeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date past = sdf.parse(dateTimeStr);
            Date now = new Date();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else if (hours < 24) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (days < 7) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(past);
            }
        } catch (Exception e) {
            return dateTimeStr;
        }
    }

    static class ViewHolder {
        ImageView ivNotificationIcon;
        TextView tvMessage;
        TextView tvTime;
        View unreadIndicator;
    }
}
