package app.xedigital.ai.model.user;

public class UserModelResponse{
	private Data data;
	private boolean success;
	private String message;
	private int statusCode;

	public Data getData(){
		return data;
	}

	public boolean isSuccess(){
		return success;
	}

	public String getMessage(){
		return message;
	}

	public int getStatusCode(){
		return statusCode;
	}
}
