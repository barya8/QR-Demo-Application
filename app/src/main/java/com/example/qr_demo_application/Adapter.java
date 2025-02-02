package com.example.qr_demo_application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.qr_demo_application.model.QRCode;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.QRViewHolder> {

    private List<QRCode> qrCodeList;
    private MainActivity context;
    private String apiKey;

    public Adapter(MainActivity context, List<QRCode> qrCodeList, String apiKey) {
        this.context = context;
        this.qrCodeList = qrCodeList;
        this.apiKey = apiKey;
    }

    @Override
    public QRViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.qr_item, parent, false);
        return new QRViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QRViewHolder holder, int position) {
        QRCode qrCode = qrCodeList.get(position);
        holder.idTextView.setText(qrCode.getId());
        holder.sizeTextView.setText(qrCode.getSize());
        holder.correctionTextView.setText(qrCode.getCorrection());
        holder.urlTextView.setText(qrCode.getUrl());

        // Decode the Base64 image to a Bitmap
        String base64String = qrCode.getBarcodeImage();

        if (base64String != null && !base64String.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                // Load the Bitmap into the ImageView using Glide
                Glide.with(holder.itemView.getContext())
                        .load(bitmap)
                        .into(holder.barcodeImageView);
            } catch (Exception e) {
                Log.d("Adapter Error", "error in decode base 64 image to glide");
            }
        }

        // Set click listener for editing the QR code
        holder.editButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(qrCode);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(qrCode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    // Method to remove an item from the list
    public void removeItem(QRCode qrCode) {
        int position = qrCodeList.indexOf(qrCode);
        if (position != -1) {
            qrCodeList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to update the data in the adapter
    public void updateData(List<QRCode> newQrCodeList) {
        this.qrCodeList = newQrCodeList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public static class QRViewHolder extends RecyclerView.ViewHolder {
        public TextView sizeTextView, correctionTextView, urlTextView, idTextView;
        public ImageView barcodeImageView;
        public ImageView editButton, deleteButton;

        public QRViewHolder(View view) {
            super(view);
            idTextView = view.findViewById(R.id.qr_LBL_id);
            sizeTextView = view.findViewById(R.id.qr_LBL_size);
            correctionTextView = view.findViewById(R.id.qr_LBL_correction);
            urlTextView = view.findViewById(R.id.qr_LBL_url);
            barcodeImageView = view.findViewById(R.id.qr_IMG_barcode);
            editButton = view.findViewById(R.id.qr_IMG_info);
            deleteButton = view.findViewById(R.id.qr_IMG_delete);
        }
    }

    private OnActionListener actionListener;

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    public interface OnActionListener {
        void onEdit(QRCode qrCode);

        void onDelete(QRCode qrCode);
    }
}
