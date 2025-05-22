package app.xedigital.ai.adminApi;

import app.xedigital.ai.model.Admin.EmployeeDetails.EmployeeDetailResponse;
import app.xedigital.ai.model.login.LoginModelResponse;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AdminAPIInterface {

    @FormUrlEncoded
    @POST("authentication/login")
    retrofit2.Call<LoginModelResponse> loginApi1(@Field("email") String email, @Field("password") String password);

    //GET APIs
    @GET("employees?active=true")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<EmployeeDetailResponse> getEmployees(@Header("Authorization") String authToken);

    @GET("visitorcategories")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    retrofit2.Call<ResponseBody> getVisitorCategories(@Header("Authorization") String authToken);

    //    POST APIs
    @POST("face/recognize")
    retrofit2.Call<ResponseBody> recognizeFace(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("employees/face")
    retrofit2.Call<ResponseBody> FaceDetails(@Header("Authorization") String authToken, @Body RequestBody requestBody);

    @POST("visitors/face")
    retrofit2.Call<ResponseBody> FaceDetailsVisitor(@Header("Authorization") String authToken, @Body RequestBody requestBody);


}
