package app.xedigital.ai.api;

import app.xedigital.ai.model.addAttendance.AddAttendanceRequest;
import app.xedigital.ai.model.addAttendanceRequest.AddedAttendanceCancelRequest;
import app.xedigital.ai.model.addedAttendanceList.AddedAttendanceListResponse;
import app.xedigital.ai.model.appliedLeaves.AppliedLeavesResponse;
import app.xedigital.ai.model.applyLeaves.ApplyLeaveRequest;
import app.xedigital.ai.model.approveClaim.ApproveClaimResponse;
import app.xedigital.ai.model.attendance.EmployeeAttendanceResponse;
import app.xedigital.ai.model.branch.UserBranchResponse;
import app.xedigital.ai.model.claimLength.ClaimLengthResponse;
import app.xedigital.ai.model.claimSave.ClaimSaveRequest;
import app.xedigital.ai.model.claimSubmit.ClaimUpdateRequest;
import app.xedigital.ai.model.dcrData.DcrDataResponse;
import app.xedigital.ai.model.dcrSubmit.DcrFormRequest;
import app.xedigital.ai.model.debitLeave.DebitLeaveRequest;
import app.xedigital.ai.model.employeeClaim.EmployeeClaimResponse;
import app.xedigital.ai.model.employeeLeaveType.EmployeeLeaveTypeResponse;
import app.xedigital.ai.model.getDocuments.DocumentListResponse;
import app.xedigital.ai.model.holiday.HolidayModelResponse;
import app.xedigital.ai.model.leaveApprovalPending.LeavePendingApprovalResponse;
import app.xedigital.ai.model.leaveType.LeaveTypeResponse;
import app.xedigital.ai.model.leaveUpdateStatus.LeaveUpdateRequest;
import app.xedigital.ai.model.leaves.LeavesResponse;
import app.xedigital.ai.model.login.LoginModelResponse1;
import app.xedigital.ai.model.policy.PolicyResponse;
import app.xedigital.ai.model.profile.UserProfileResponse;
import app.xedigital.ai.model.regularize.RegularizeAttendanceRequest;
import app.xedigital.ai.model.regularizeApplied.RegularizeAppliedResponse;
import app.xedigital.ai.model.regularizeList.RegularizeApprovalResponse;
import app.xedigital.ai.model.regularizeUpdateStatus.RegularizeUpdateRequest;
import app.xedigital.ai.model.shiftApplied.ShiftAppliedResponse;
import app.xedigital.ai.model.shiftApprovalList.ShiftApproveListResponse;
import app.xedigital.ai.model.shiftApprove.ShiftApproveRequest;
import app.xedigital.ai.model.shiftTime.ShiftTimeResponse;
import app.xedigital.ai.model.shiftUpdate.ShiftUpdateRequest;
import app.xedigital.ai.model.shifts.ShiftTypeResponse;
import app.xedigital.ai.model.uploadDocument.UploadDocumentRequest;
import app.xedigital.ai.model.user.UserModelResponse;
import app.xedigital.ai.model.visitorsDetails.VisitorsDetailsResponse;
import app.xedigital.ai.model.vms.GetVisitorsResponse;
import app.xedigital.ai.ui.userProfileEmail.UserProfileByEmailResponse;
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

    @GET("leaves/applied")
    retrofit2.Call<AppliedLeavesResponse> getAppliedLeaves(@Header("Authorization") String authToken);

    @GET("leavetypes?active=true")
    retrofit2.Call<LeaveTypeResponse> getLeaveTypes(@Header("Authorization") String authToken);

    @GET("leaves/type/{leaveID}/employees/{employeeID}")
    retrofit2.Call<EmployeeLeaveTypeResponse> getEmployeeLeave(@Header("Authorization") String authToken, @Path("leaveID") String leaveID, @Path("employeeID") String employeeID);

    @GET("leavetypes/profile/{leaveID}")
    retrofit2.Call<ResponseBody> getLeaveTypeDetails(@Header("Authorization") String authToken, @Path("leaveID") String leaveID);

    @GET("leaves/unapproved/{leaveId}/employees/{employeeId}")
    retrofit2.Call<ResponseBody> getUnapprovedLeaves(@Header("Authorization") String authToken, @Path("leaveId") String leaveId, @Path("employeeId") String employeeId);

    @GET("leaves/applied/report/manager/{employeeId}")
    retrofit2.Call<LeavePendingApprovalResponse> getPendingApprovalLeaves(@Header("Authorization") String authToken, @Path("employeeId") String employeeId);

    @GET("claims/employee/claimLength")
    retrofit2.Call<ClaimLengthResponse> getClaimLength(@Header("Authorization") String authToken);

    @GET("policies")
    retrofit2.Call<PolicyResponse> getPolicies(@Header("Authorization") String authToken);

    @GET("claims/employee/claim")
    retrofit2.Call<EmployeeClaimResponse> getClaims(@Header("Authorization") String authToken);

    @GET("claims/employee/claim?start=&end=&employee=&page=&limit=&branch=&prefix=&rm=true")
    retrofit2.Call<ApproveClaimResponse> getClaimsForApproval(@Header("Authorization") String authToken);

    @GET("shifts/shift/shiftTypes")
    retrofit2.Call<ShiftTypeResponse> getShiftTypes(@Header("Authorization") String authToken);

    @GET("shifts/shiftType/{shiftId}")
    retrofit2.Call<ShiftTimeResponse> getShiftTime(@Header("Authorization") String authToken, @Path("shiftId") String shiftId);

    @GET("shifts/shiftChange/shiftApprovalRequest")
    retrofit2.Call<ShiftAppliedResponse> getShiftApplied(@Header("Authorization") String authToken);

    @GET("shifts/shiftChange/shiftApprovalRequest?startTime=&endTime=&employee=&page=&limit=&branch=&prefix=&rm=true")
    retrofit2.Call<ShiftApproveListResponse> getShiftApprovalList(@Header("Authorization") String authToken);

    @GET("employees/uploadDoc/list/{empId}")
    retrofit2.Call<DocumentListResponse> getDocList(@Header("Authorization") String authToken, @Path("empId") String empId);

    @GET("employees/attendance/add/regularizeList")
    retrofit2.Call<AddedAttendanceListResponse> getAddedAttendance(@Header("Authorization") String authToken);

    @GET("visitors?start=&end=&type=&department=&employee=&page=&limit=&branch=&prefix=")
    retrofit2.Call<GetVisitorsResponse> getVisitors(@Header("Authorization") String authToken);

    @GET("employees/profile/email/{empEmail}")
    retrofit2.Call<UserProfileByEmailResponse> getUserProfileByEmail(@Path("empEmail") String empEmail, @Header("Authorization") String authToken);
    @GET("visitors/detail/{contact}")
    retrofit2.Call<VisitorsDetailsResponse> getVisitorDetail(@Path("contact") String contact, @Header("Authorization") String authToken);

    @GET("leaves/applied/report/crmanager/{employeeId}")
    retrofit2.Call<LeavePendingApprovalResponse> getPendingApprovalLeavesCR(@Header("Authorization") String authToken, @Path("employeeId") String employeeId);

    //    POST APIs
    @POST("face/recognize")
    retrofit2.Call<ResponseBody> FaceRecognitionApi(@Body RequestBody requestBody);

    @POST("employees/face")
    retrofit2.Call<ResponseBody> FaceDetailApi(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("/face/recognize")
    retrofit2.Call<ResponseBody> VmsFaceRecognitionApi(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("employees/attendance/punch")
    retrofit2.Call<ResponseBody> AttendanceApi(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("employees/attendance/add/employee")
    retrofit2.Call<ResponseBody> AddAttendanceApi(@Header("Authorization") String token, @Body AddAttendanceRequest requestBody);

    @POST("employees/attendance/punch/regularize/employee/{attendanceId}")
    Call<ResponseBody> RegularizeApi(@Header("Authorization") String authToken, @Path("attendanceId") String attendanceId, @Body RegularizeAttendanceRequest requestBody);

    @POST("employees/dcr")
    retrofit2.Call<ResponseBody> DcrSubmit(@Header("Authorization") String token, @Body DcrFormRequest requestBody);

    @POST("leaves/apply")
    retrofit2.Call<ResponseBody> LeavesApply(@Header("Authorization") String token, @Body ApplyLeaveRequest requestBody);

    @POST("shifts/shiftChange")
    retrofit2.Call<ResponseBody> ShiftChange(@Header("Authorization") String token, @Body ShiftUpdateRequest requestBody);

//    PUT API

    @PUT("employees/attendance/regularize/applied/status/{attendanceId}")
    Call<ResponseBody> RegularizeAttendanceStatus(@Header("Authorization") String authToken, @Path("attendanceId") String attendanceId, @Body RegularizeUpdateRequest requestBody);

    @PUT("leaves/used/debit")
    retrofit2.Call<ResponseBody> LeavesUsedDebit(@Header("Authorization") String token, @Body DebitLeaveRequest requestBody);

    @PUT("leaves/status/{managerId}")
    retrofit2.Call<ResponseBody> LeavesStatus(@Header("Authorization") String token, @Path("managerId") String managerId, @Body LeaveUpdateRequest requestBody);

    @PUT("claims/status/{id}")
    retrofit2.Call<ResponseBody> claimStatus(@Header("Authorization") String token, @Path("id") String id, @Body RequestBody requestBody);

    @PUT("shifts/shift/applied/status/{shiftId}")
    retrofit2.Call<ResponseBody> UpdateShiftStatus(@Header("Authorization") String token, @Path("shiftId") String shiftId, @Body ShiftApproveRequest requestBody);

    @POST("images/add/bucket/claim")
    retrofit2.Call<ResponseBody> uploadImage(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimOne")
    retrofit2.Call<ResponseBody> uploadImage1(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimTwo")
    retrofit2.Call<ResponseBody> uploadImage2(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimThree")
    retrofit2.Call<ResponseBody> uploadImage3(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimFour")
    retrofit2.Call<ResponseBody> uploadImage4(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimFive")
    retrofit2.Call<ResponseBody> uploadImage5(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimSix")
    retrofit2.Call<ResponseBody> uploadImage6(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimSeven")
    retrofit2.Call<ResponseBody> uploadImage7(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimEight")
    retrofit2.Call<ResponseBody> uploadImage8(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("images/add/bucket/claimNine")
    retrofit2.Call<ResponseBody> uploadImage9(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("claims")
    retrofit2.Call<ResponseBody> claimSubmit(@Header("Authorization") String token, @Body ClaimUpdateRequest requestBody);

    @POST("claims/save")
    retrofit2.Call<ResponseBody> claimSave(@Header("Authorization") String token, @Body ClaimSaveRequest requestBody);

    @POST("images/add/bucket/employeesdoc")
    retrofit2.Call<ResponseBody> uploadDoc(@Header("Authorization") String token, @Body RequestBody requestBody);

    @POST("employees/uploadDoc")
    retrofit2.Call<ResponseBody> uploadDocument(@Header("Authorization") String token, @Body UploadDocumentRequest requestBody);

    @PUT("employees/addAttendance/regularize/applied/status/{attendanceId}")
    Call<ResponseBody> AddedAttendanceStatus(@Header("Authorization") String authToken, @Path("attendanceId") String attendanceId, @Body AddedAttendanceCancelRequest requestBody);
}
