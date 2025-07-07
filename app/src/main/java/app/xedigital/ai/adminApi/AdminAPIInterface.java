package app.xedigital.ai.adminApi;

import app.xedigital.ai.model.Admin.Branches.CompanyBranchResponse;
import app.xedigital.ai.model.Admin.Dashboard.AdminDashboardResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.Admin.VisitorManual.VisitorManualRequest;
import app.xedigital.ai.model.Admin.addBucket.AddBucketRequest;
import app.xedigital.ai.model.Admin.addFace.AddFaceResponse;
import app.xedigital.ai.model.Admin.visitorContact.VisitorContactResponse;
import app.xedigital.ai.model.Admin.visitorFace.VisitorFaceResponse;
import app.xedigital.ai.model.login.LoginModelResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AdminAPIInterface {


    @FormUrlEncoded
    @POST("authentication/login")
    retrofit2.Call<LoginModelResponse> loginApi1(@Field("email") String email, @Field("password") String password);

    //GET APIs
    @GET("employees?active=true")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<EmployeeDetailResponse> getEmployees(@Header("Authorization") String authToken);

    @GET("dashboard")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<AdminDashboardResponse> getDashboard(@Header("Authorization") String authToken);

    @GET("users/profile/{userId}")
    Call<UserDetailsResponse> getUser(@Header("Authorization") String authToken, @Path("userId") String userId);

    @GET("visitorcategories")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<ResponseBody> getVisitorCategories(@Header("Authorization") String authToken);

    //    POST APIs
    @POST("face/recognize")
    retrofit2.Call<ResponseBody> recognizeFace(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("employees/face")
    retrofit2.Call<ResponseBody> FaceDetails(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("visitors/face")
    retrofit2.Call<VisitorFaceResponse> FaceDetailsVisitor(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("images/add/bucket")
    retrofit2.Call<ResponseBody> addBucket(@Header("Authorization") String authToken, @Body AddBucketRequest addBucketRequest);

    @POST("otp/tinyurl")
    retrofit2.Call<ResponseBody> getTinyUrl(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @GET("visitors/checkedout/{contact}")
    retrofit2.Call<VisitorContactResponse> getCheckedOut(@Header("Authorization") String authToken, @Path("contact") String contact);

    @POST("/visitors/signout")
    retrofit2.Call<ResponseBody> signOut(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @GET("branches/{companyId}/company")
    retrofit2.Call<CompanyBranchResponse> getBranches(@Header("Authorization") String authToken, @Path("companyId") String companyId);

    @POST("face/add")
    retrofit2.Call<AddFaceResponse> addFace(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("images/add/bucket")
    retrofit2.Call<ResponseBody> addBucket(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("visitors/manual")
    retrofit2.Call<ResponseBody> ManualVisitor(@Header("Authorization") String authToken, @Body VisitorManualRequest visitorManualRequest);
}
