package app.xedigital.ai.admin.adminAPI;

import app.xedigital.ai.admin.adminModal.ActiveShift.ActiveShiftResponse;
import app.xedigital.ai.admin.adminModal.AdminUsers.AdminUserResponse;
import app.xedigital.ai.admin.adminModal.Branches.CompanyBranchResponse;
import app.xedigital.ai.admin.adminModal.Dashboard.AdminDashboardResponse;
import app.xedigital.ai.admin.adminModal.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.admin.adminModal.EmployeeLeaves.EmployeeLeaveResponse;
import app.xedigital.ai.admin.adminModal.LeaveGraph.LeaveGraphResponse;
import app.xedigital.ai.admin.adminModal.Role.UserRoleResponse;
import app.xedigital.ai.admin.adminModal.UserDetails.UserDetailsResponse;
import app.xedigital.ai.admin.adminModal.VisitorManual.VisitorManualRequest;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsAdminDetailsResponse;
import app.xedigital.ai.admin.adminModal.VisitorsAdminDetails.VisitorsItem;
import app.xedigital.ai.admin.adminModal.addBucket.AddBucketRequest;
import app.xedigital.ai.admin.adminModal.addFace.AddFaceResponse;
import app.xedigital.ai.admin.adminModal.assignLeave.AssignLeaveRequest;
import app.xedigital.ai.admin.adminModal.department.DepartmentResponse;
import app.xedigital.ai.admin.adminModal.leaveBalance.LeaveBalanceResponse;
import app.xedigital.ai.admin.adminModal.leaveType.LeaveTypeResponse;
import app.xedigital.ai.admin.adminModal.partners.PartnersResponse;
import app.xedigital.ai.admin.adminModal.updateEmployee.UpdateEmployeeRequest;
import app.xedigital.ai.admin.adminModal.visitorContact.VisitorContactResponse;
import app.xedigital.ai.admin.adminModal.visitorFace.VisitorFaceResponse;
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
    Call<LoginModelResponse> loginApi1(@Field("email") String email, @Field("password") String password);


    //GET APIs

    @GET("leaves/dashboard/graph/approved/employees")
    Call<LeaveGraphResponse> getLeavesGraph(@Header("Authorization") String authToken);

    @GET("partners")
    Call<PartnersResponse> getPartners(@Header("Authorization") String authToken);

    @GET("partners/profile/{partnerId}")
    Call<UserDetailsResponse> getPartner(@Header("Authorization") String authToken, @Path("partnerId") String partnerId);

    @GET("users")
    Call<AdminUserResponse> getAllUsers(@Header("Authorization") String authToken);

    @PUT("partners/profile/{partnerId}")
    Call<ResponseBody> updatePartner(@Header("Authorization") String authToken, @Path("partnerId") String partnerId, @Body RequestBody requestBody);

    @GET("roles")
    Call<UserRoleResponse> getRoles(@Header("Authorization") String authToken);

    @GET("employees")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<EmployeeDetailResponse> getEmployees(@Header("Authorization") String authToken);

    @GET("dashboard")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<AdminDashboardResponse> getDashboard(@Header("Authorization") String authToken);

    @GET("departments")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<DepartmentResponse> getDepartments(@Header("Authorization") String authToken);

    @GET("shifts?active=true")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<ActiveShiftResponse> getShifts(@Header("Authorization") String authToken);

    @GET("users/profile/{userId}")
    Call<UserDetailsResponse> getUser(@Header("Authorization") String authToken, @Path("userId") String userId);

    @GET("visitorcategories")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<ResponseBody> getVisitorCategories(@Header("Authorization") String authToken);

    @GET("visitors/checkedin/{visitorContact}")
    Call<VisitorContactResponse> getCheckedIn(@Header("Authorization") String authToken, @Path("visitorContact") String visitorContact);

    @GET("visitors/checkedout/{contact}")
    Call<VisitorContactResponse> getCheckedOut(@Header("Authorization") String authToken, @Path("contact") String contact);

    @GET("branches/{companyId}/company")
    Call<CompanyBranchResponse> getBranches(@Header("Authorization") String authToken, @Path("companyId") String companyId);

    //    ?start=&end=&type=&department=&employee=&page=&limit=&branch=&prefix=
    @GET("visitors")
    Call<VisitorsAdminDetailsResponse> getVisitors(@Header("Authorization") String authToken);

    @GET("leavetypes?active=true")
    Call<LeaveTypeResponse> getLeaveTypes(@Header("Authorization") String authToken);

    @GET("leaves/type/{leaveId}/employees/{employeeId}")
    Call<LeaveBalanceResponse> getLeaves(@Header("Authorization") String authToken, @Path("leaveId") String leaveId, @Path("employeeId") String employeeId);

    @GET("leaves/employee/{employeeId}")
    Call<EmployeeLeaveResponse> getLeavesEmployee(@Header("Authorization") String authToken, @Path("employeeId") String employeeId);


    //    POST APIs
    @POST("face/recognize")
    Call<ResponseBody> recognizeFace(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("partners")
    Call<ResponseBody> addPartner(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("employees/face")
    Call<ResponseBody> FaceDetails(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("visitors/face")
    Call<VisitorFaceResponse> FaceDetailsVisitor(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("images/add/bucket")
    Call<ResponseBody> addBucket(@Header("Authorization") String authToken, @Body AddBucketRequest addBucketRequest);

    @POST("otp/tinyurl")
    Call<ResponseBody> getTinyUrl(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("https://app.xedigital.ai/api/v1/visitors/signout")
    Call<ResponseBody> signOut(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("face/add")
    Call<AddFaceResponse> addFace(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("images/add/bucket")
    Call<ResponseBody> addBucket(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("visitors/manual")
    Call<ResponseBody> ManualVisitor(@Header("Authorization") String authToken, @Body VisitorManualRequest visitorManualRequest);

    @POST("leaves/assign/employee")
    Call<ResponseBody> assignLeave(@Header("Authorization") String authToken, @Body AssignLeaveRequest requestBody);

    //    PUT APIs
    @PUT("users/profile/{userId}")
    Call<ResponseBody> updateUser(@Header("Authorization") String authToken, @Path("userId") String userId, @Body RequestBody requestBody);

    @PUT("employees/profile/{employeeId}")
    Call<ResponseBody> updateEmployee(@Header("Authorization") String authToken, @Path("employeeId") String employeeId, @Body UpdateEmployeeRequest requestBody);

    //Check IN
    @PUT("visitors/signoutmanual/{visitorId}")
    Call<ResponseBody> checkInManual(@Header("Authorization") String authToken, @Path("visitorId") String visitorId, @Body VisitorsItem visitorData);

    @PUT("visitors/signout/{VisitorId}")
    Call<ResponseBody> checkOut(@Header("Authorization") String authToken, @Path("VisitorId") String VisitorId, @Body VisitorsItem visitorData);

}
