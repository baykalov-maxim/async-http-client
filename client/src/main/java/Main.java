import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

public class Main {

	public static void main(String[] args) {
		System.out.println(performHttpGet("", "").getStatusCode());
		
	}
	
	private static Response performHttpGet(String sni, String fullUrl) {

        AsyncHttpClient c = new DefaultAsyncHttpClient();

        try {
            Future<Response> f = c.prepareGet("https://www.google.com.ua/")
            		.setVirtualHost("asd")
                    .execute();

            return f.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
