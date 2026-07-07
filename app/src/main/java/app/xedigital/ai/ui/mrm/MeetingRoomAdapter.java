package app.xedigital.ai.ui.mrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import app.xedigital.ai.R;
import app.xedigital.ai.model.meetingRoom.RoomDataItem;

public class MeetingRoomAdapter extends RecyclerView.Adapter<MeetingRoomAdapter.RoomViewHolder> {

    private final List<RoomDataItem> roomList = new ArrayList<>();

    public void setRooms(List<RoomDataItem> rooms) {
        this.roomList.clear();
        if (rooms != null) {
            this.roomList.addAll(rooms);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomDataItem item = roomList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageRoom;
        private final TextView textRoomName;
        private final TextView textRoomCode;
        private final TextView textFloor;
        private final TextView textSeats;
        private final TextView textLocation;
        private final MaterialButton btnReserve;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRoom = itemView.findViewById(R.id.imageRoom);
            textRoomName = itemView.findViewById(R.id.textRoomName);
            textRoomCode = itemView.findViewById(R.id.textRoomCode);
            textFloor = itemView.findViewById(R.id.textFloor);
            textSeats = itemView.findViewById(R.id.textSeats);
            textLocation = itemView.findViewById(R.id.textLocation);
            btnReserve = itemView.findViewById(R.id.btnReserve);
        }

        public void bind(RoomDataItem item) {
            // Bind text data fields with clean fallbacks
            textRoomName.setText(item.getRoomName() != null ? item.getRoomName() : "Unnamed Room");
            textRoomCode.setText(item.getRoomCode() != null ? item.getRoomCode() : "N/A");
            textFloor.setText(item.getFloor() != null ? item.getFloor() : "N/A");
            textSeats.setText(item.getSeats() != null ? item.getSeats() : "0");
            textLocation.setText(item.getLocation() != null ? item.getLocation() : "N/A");

            // Safe validation handling for images coming from AWS S3 endpoints
            String imageUrl = item.getDocFileURL();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext()).load(imageUrl).format(DecodeFormat.PREFER_RGB_565).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ic_meeting_room).error(R.drawable.ic_meeting_room).into(imageRoom);
            } else {
                // Instantly defaults locally if URL is null or empty string
                imageRoom.setImageResource(R.drawable.ic_meeting_room);
            }

            // Click interaction listener for the dynamic item reservation button
            btnReserve.setOnClickListener(v -> {
                String selectedRoom = item.getRoomName() != null ? item.getRoomName() : "Room";
                Toast.makeText(itemView.getContext(), "Initiating booking for: " + selectedRoom, Toast.LENGTH_SHORT).show();
            });
        }
    }
}