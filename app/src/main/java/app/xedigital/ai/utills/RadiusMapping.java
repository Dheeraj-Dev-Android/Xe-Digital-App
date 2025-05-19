package app.xedigital.ai.utills;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import androidx.annotation.NonNull;

import java.util.List;

import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.api.APIInterface;
import app.xedigital.ai.model.radius.GetRadiusResponse;
import app.xedigital.ai.model.radius.RadiusItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RadiusMapping {

    private static final String TAG = "RadiusMapping";
    private final Context context;
    private final APIInterface apiInterface;

    public RadiusMapping(Context context) {
        this.context = context;
        apiInterface = getRadius();
    }

    private String getAuthToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("authToken", "");
    }

    public void checkIfWithinAnyRadius(double userLat, double userLng, RadiusCheckCallback callback) {
        String authToken = getAuthToken();
        if (authToken != null) {
            Call<GetRadiusResponse> call = apiInterface.getRadious(authToken);
            call.enqueue(new Callback<GetRadiusResponse>() {
                @Override
                public void onResponse(@NonNull Call<GetRadiusResponse> call, @NonNull Response<GetRadiusResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<RadiusItem> radiusItems = response.body().getData().getRadious();
                        if (radiusItems != null && !radiusItems.isEmpty()) {
                            for (RadiusItem item : radiusItems) {
                                double lat = item.getLattitude();
                                double lng = item.getLognitude();
                                float[] distance = new float[1];
                                Location.distanceBetween(userLat, userLng, lat, lng, distance);
                                if (distance[0] <= item.getRadious()) {
                                    callback.onResult(true); // User is inside
                                    return;
                                }
                            }
                            callback.onResult(false); // User is outside all radii
                        } else {
                            callback.onResult(false);
                        }
                    } else {
                        callback.onResult(false);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetRadiusResponse> call, @NonNull Throwable t) {
                    callback.onResult(false);
                }
            });
        } else {
            callback.onResult(false);
        }
    }

    public APIInterface getRadius() {
        return APIClient.getInstance().getRadius();
    }

    public interface RadiusCheckCallback {
        void onResult(boolean isInside);
    }
}
