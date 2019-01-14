package worldjam.util;

public class WJUtil {

	public static String getDefaultUsername() {
		if(System.getenv("USERNAME") != null)
			return System.getenv("USERNAME");
		if(System.getProperty("user.name") != null)
			return System.getProperty("user.name");
		return "user1";
	}

}
