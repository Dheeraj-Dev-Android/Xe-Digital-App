package app.xedigital.ai.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    private static final String BASE_URL = "https://app.xedigital.ai/";
    private static final String BASE_URL_2 = "https://app.xedigital.ai/api/v1/";
    private static APIClient instance;
    private final Retrofit retrofit1;
    private final Retrofit retrofit2;

    private APIClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(loggingInterceptor).retryOnConnectionFailure(true).connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();

        retrofit1 = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();

        retrofit2 = new Retrofit.Builder().baseUrl(BASE_URL_2).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
    }

    public static synchronized APIClient getInstance() {
        if (instance == null) {
            instance = new APIClient();
        }
        return instance;
    }

    public APIInterface getApi() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getRecognize() {
        return retrofit1.create(APIInterface.class);
    }

    public APIInterface getLogin() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getUser() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getBranch() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getAttendance() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getHolidays() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getImage() {
        return retrofit1.create(APIInterface.class);
    }

    public APIInterface getFace() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getPunch() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getDcrData() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface RegularizeAttendance() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface AddAttendance() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getRegularizeApplied() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getLeaves() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getRegularizeListApproval() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getEmployeeClaim() {
        return retrofit2.create(APIInterface.class);
    }


    public APIInterface UpdateRegularizeListApproval() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface UpdateLeavesStatus() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface DcrFormSubmit() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface AppliedLeave() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface LeavesApply() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getLeaveTypes() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface UpdateLeaveListApproval() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getUnapprovedLeaves() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getLeaveTypeDetails() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getEmployeeLeave() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface debitLeave() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getPendingApprovalLeaves() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getClaimLength() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getPolicies() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getUploadImage() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface ClaimSubmit() {
        return retrofit2.create(APIInterface.class);
    }
    public APIInterface ClaimSave() {return  retrofit2.create(APIInterface.class);}

    public APIInterface ClaimUpdateStatus() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface ApproveClaim() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getShiftTypes() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getDocumentList() {
        return retrofit2.create(APIInterface.class);
    }

    public APIInterface getVisitors() {
        return retrofit2.create(APIInterface.class);
    }
}



