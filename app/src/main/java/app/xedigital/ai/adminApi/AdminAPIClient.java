package app.xedigital.ai.adminApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import app.xedigital.ai.model.Admin.EmployeeDetails.Crossmanager;
import app.xedigital.ai.model.Admin.EmployeeDetails.CrossmanagerDeserializer;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminAPIClient {

    private static final String BASE_URL = "https://app.xedigital.ai/";
    private static final String BASE_URL_2 = "https://app.xedigital.ai/api/v1/";

    private static AdminAPIClient instance;
    private final Retrofit retrofit1;
    private final Retrofit retrofit2;

    private AdminAPIClient() {
        // Optional: Enable for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Crossmanager.class, new CrossmanagerDeserializer())
                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // Uncomment for logging
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        retrofit1 = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();

        retrofit2 = new Retrofit.Builder()
                .baseUrl(BASE_URL_2)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    public static synchronized AdminAPIClient getInstance() {
        if (instance == null) {
            instance = new AdminAPIClient();
        }
        return instance;
    }

    public AdminAPIInterface getBase1() {
        return retrofit1.create(AdminAPIInterface.class);
    }

    public AdminAPIInterface getBase2() {
        return retrofit2.create(AdminAPIInterface.class);
    }

}
