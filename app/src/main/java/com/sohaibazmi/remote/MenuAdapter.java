package com.sohaibazmi.remote;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<MenuItem> menuItemList;
    private Context context;

    public MenuAdapter(List<MenuItem> menuItemList, Context context) {
        this.menuItemList = menuItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem menuItem = menuItemList.get(position);
        holder.menuTitle.setText(menuItem.getTitle());
        holder.menuIcon.setImageResource(menuItem.getIconResId());

        holder.itemView.setOnClickListener(v -> handleMenuClick(menuItem.getTitle()));
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView menuIcon;
        TextView menuTitle;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            menuTitle = itemView.findViewById(R.id.menuTitle);
        }
    }

    private void handleMenuClick(String title) {
        switch (title) {
            case "Play Games":
                context.startActivity(new Intent(context, GamesActivity.class));
                break;

            case "Market Live":
                context.startActivity(new Intent(context, MarketActivity.class));
                break;

            case "News & Updates":
                context.startActivity(new Intent(context, NewsSourcesActivity.class));
                break;

            case "Contact Developer":
                context.startActivity(new Intent(context, ContactDeveloperActivity.class));
                break;

            case "Spin & Win":
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Toast.makeText(context, "Please login first to play.", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, ProfileActivity.class));
                    return;
                }

                String uid = user.getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid).child("earnings");
                ref.get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String earningStr = snapshot.getValue(String.class).replace("₹", "").trim();
                        double balance = Double.parseDouble(earningStr);

                        if (balance >= 1.0) {
                            context.startActivity(new Intent(context, DiceActivity.class));
                        } else {
                            Toast.makeText(context, "You need at least ₹1 to play.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No profile found. Please register first.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> Toast.makeText(context, "Error checking balance.", Toast.LENGTH_SHORT).show());
                break;

            case "Profile":
                context.startActivity(new Intent(context, ProfileActivity.class));
                break;

            case "Scan QR":
                context.startActivity(new Intent(context, QRScannerActivity.class));
                break;

            default:
                Toast.makeText(context, "Feature not available.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
