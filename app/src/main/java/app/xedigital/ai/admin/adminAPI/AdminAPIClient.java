package app.xedigital.ai.admin.adminAPI;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import app.xedigital.ai.admin.adminModal.EmployeeDetails.Crossmanager;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.CrossmanagerDeserializer;
import app.xedigital.ai.api.APIClient;
import app.xedigital.ai.utills.PersistentCookieJar;
import app.xedigital.ai.utills.TokenAuthenticator;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminAPIClient {

    private static final String BASE_URL = "https://app.xedigital.ai/";
    private static final String BASE_URL_2 = "https://app.xedigital.ai/api/v1/";
    private static final String BASE_URL_3 = "https://app.xedigital.ai/api/v2/";

//    private static final String BASE_URL = "http://10.172.6.33:4000/";
//    private static final String BASE_URL_2 = "http://10.172.6.33:4000/api/v1/";
//    private static final String BASE_URL_3 = "http://10.172.6.33:4000/api/v2/";

    private static AdminAPIClient instance;
    private static Context appContext;

    private final Retrofit retrofit1;
    private final Retrofit retrofit2;
    private final Retrofit retrofit3;

    private AdminAPIClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Gson gson = new GsonBuilder().registerTypeAdapter(Crossmanager.class, new CrossmanagerDeserializer()).create();

        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).retryOnConnectionFailure(true).connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS);

        // Reuse the same CookieJar + Authenticator from APIClient
        PersistentCookieJar cookieJar = APIClient.getSharedCookieJar();
        TokenAuthenticator authenticator = APIClient.getSharedAuthenticator();

        if (cookieJar != null && authenticator != null) {
            builder.cookieJar(cookieJar);
            builder.authenticator(authenticator);
        }

        OkHttpClient okHttpClient = builder.build();

        retrofit1 = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build();

        retrofit2 = new Retrofit.Builder().baseUrl(BASE_URL_2).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build();

        retrofit3 = new Retrofit.Builder().baseUrl(BASE_URL_3).addConverterFactory(GsonConverterFactory.create(gson)).client(okHttpClient).build();
    }

    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
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

    public AdminAPIInterface getBase3() {
        return retrofit3.create(AdminAPIInterface.class);
    }
}