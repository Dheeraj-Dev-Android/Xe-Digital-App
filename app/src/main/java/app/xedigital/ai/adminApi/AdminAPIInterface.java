package app.xedigital.ai.adminApi;

import app.xedigital.ai.model.Admin.AdminUsers.AdminUserResponse;
import app.xedigital.ai.model.Admin.Branches.CompanyBranchResponse;
import app.xedigital.ai.model.Admin.Dashboard.AdminDashboardResponse;
import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.Admin.LeaveGraph.LeaveGraphResponse;
import app.xedigital.ai.model.Admin.Role.UserRoleResponse;
import app.xedigital.ai.model.Admin.UserDetails.UserDetailsResponse;
import app.xedigital.ai.model.Admin.VisitorManual.VisitorManualRequest;
import app.xedigital.ai.model.Admin.VisitorsAdminDetails.VisitorsAdminDetailsResponse;
import app.xedigital.ai.model.Admin.addBucket.AddBucketRequest;
import app.xedigital.ai.model.Admin.addFace.AddFaceResponse;
import app.xedigital.ai.model.Admin.partners.PartnersResponse;
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
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminAPIInterface {


    @FormUrlEncoded
    @POST("authentication/login")
    retrofit2.Call<LoginModelResponse> loginApi1(@Field("email") String email, @Field("password") String password);

    @GET("leaves/dashboard/graph/approved/employees")
    retrofit2.Call<LeaveGraphResponse> getLeavesGraph(@Header("Authorization") String authToken);

    @GET("partners")
    retrofit2.Call<PartnersResponse> getPartners(@Header("Authorization") String authToken);

    @POST("partners")
    retrofit2.Call<ResponseBody> addPartner(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @GET("partners/profile/{partnerId}")
    retrofit2.Call<UserDetailsResponse> getPartner(@Header("Authorization") String authToken, @Path("partnerId") String partnerId);

    @GET("users")
    retrofit2.Call<AdminUserResponse> getAllUsers(@Header("Authorization") String authToken);

    @PUT("partners/profile/{partnerId}")
    retrofit2.Call<ResponseBody> updatePartner(@Header("Authorization") String authToken, @Path("partnerId") String partnerId, @Body RequestBody requestBody);

    @GET("roles")
    retrofit2.Call<UserRoleResponse> getRoles(@Header("Authorization") String authToken);

    //GET APIs
    @GET("employees")
//    @GET("employees?active=true")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<EmployeeDetailResponse> getEmployees(@Header("Authorization") String authToken);

    @GET("dashboard")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<AdminDashboardResponse> getDashboard(@Header("Authorization") String authToken);

    @GET("users/profile/{userId}")
    Call<UserDetailsResponse> getUser(@Header("Authorization") String authToken, @Path("userId") String userId);

    @PUT("users/profile/{userId}")
    Call<ResponseBody> updateUser(@Header("Authorization") String authToken, @Path("userId") String userId, @Body RequestBody requestBody);

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

    //    ?start=&end=&type=&department=&employee=&page=&limit=&branch=&prefix=
    @GET("visitors")
    retrofit2.Call<VisitorsAdminDetailsResponse> getVisitors(@Header("Authorization") String authToken);

}
