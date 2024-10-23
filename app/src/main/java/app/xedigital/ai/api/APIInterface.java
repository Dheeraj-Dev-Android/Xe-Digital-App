package app.xedigital.ai.api;

import app.xedigital.ai.model.addAttendance.AddAttendanceRequest;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.dcrData.DcrDataResponse;
import app.xedigital.ai.model.dcrSubmit.DcrFormRequest;
import app.xedigital.ai.model.holiday.HolidayModelResponse;
import app.xedigital.ai.model.leaves.LeavesResponse;
import app.xedigital.ai.model.login.LoginModelResponse1;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.model.regularize.RegularizeAttendanceRequest;
import app.xedigital.ai.model.regularizeApplied.RegularizeAppliedResponse;
import app.xedigital.ai.model.regularizeList.RegularizeApprovalResponse;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.model.user.UserModelResponse;

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
import retrofit2.http.Query;

public interface APIInterface {

    @FormUrlEncoded
    @POST("authentication/login")
    retrofit2.Call<LoginModelResponse1> loginApi1(@Field("email") String email, @Field("password") String password);

    //GET APIs
    @GET("users/profile/{userId}")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<UserModelResponse> getUserData(@Path("userId") String id, @Header("Authorization") String authToken);

    @GET("branches/profile/{branchId}")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<UserBranchResponse> getBranchData(@Path("branchId") String id, @Header("Authorization") String authToken);

    @GET("employees/profile/{userId}")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<UserProfileResponse> getUserProfile(@Path("userId") String id, @Header("Authorization") String authToken);

    @GET("employees/attendance/punch")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<EmployeeAttendanceResponse> getAttendance(@Header("Authorization") String authToken, @Query("start") String startDate, @Query("end") String endDate, @Query("sorting") String sorting, @Query("employee") String employee, @Query("page") String page, @Query("limit") String limit, @Query("branch") String branch, @Query("prefix") String prefix);

    @GET("holidays")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<HolidayModelResponse> getHolidays(@Header("Authorization") String authToken);

    @GET("employees/dcrData")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    Call<DcrDataResponse> getEmployeeDcr(@Header("Authorization") String authToken, @Query("start") String startDate, @Query("end") String endDate, @Query("sorting") String sorting, @Query("employee") String employee, @Query("page") String page, @Query("limit") String limit, @Query("branch") String branch, @Query("prefix") String prefix);

    @GET("employees/attendance/regularize/applied")
    retrofit2.Call<RegularizeAppliedResponse> getRegularizeApplied(@Header("Authorization") String authToken);

    @GET("leaves/employee")
    retrofit2.Call<LeavesResponse> getLeaves(@Header("Authorization") String authToken);

    @GET("employees/attendance/regularize/applied?start=&end=&employee=&page=&limit=&branch=&prefix=&rm=true")
    retrofit2.Call<RegularizeApprovalResponse> getRegularizeApproval(@Header("Authorization") String authToken);

    //    POST APIs
    @POST("face/recognize")
    retrofit2.Call<ResponseBody> FaceRecognitionApi(@Body RequestBody requestBody);

    @POST("employees/face")
    retrofit2.Call<ResponseBody> FaceDetailApi(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("employees/attendance/punch")
    retrofit2.Call<ResponseBody> AttendanceApi(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("employees/attendance/add/employee")
    retrofit2.Call<ResponseBody> AddAttendanceApi(@Header("Authorization") String token, @Body AddAttendanceRequest requestBody);

    @POST("employees/attendance/punch/regularize/employee/{attendanceId}")
    Call<ResponseBody> RegularizeApi(@Header("Authorization") String authToken, @Path("attendanceId") String attendanceId, @Body RegularizeAttendanceRequest requestBody);

    @POST("employees/dcr")
    retrofit2.Call<ResponseBody> DcrSubmit(@Header("Authorization") String token, @Body DcrFormRequest requestBody);

//    PUT API

    @PUT("employees/attendance/regularize/applied/status/{attendanceId}")
    Call<ResponseBody> RegularizeAttendanceStatus(@Header("Authorization") String authToken, @Path("attendanceId") String attendanceId, @Body RegularizeUpdateRequest requestBody);
}
