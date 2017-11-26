package net.h2demo;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import okhttp3.OkHttpClient;


public class H2ClientTest {

	private static SSLContext context;
	@BeforeClass
	public static void setUp() throws NoSuchAlgorithmException, KeyManagementException {
		context = SSLContext.getInstance("SSL");
		X509TrustManager dummy = new DummyTrustManager();
		context.init(null, new TrustManager[]{dummy}, null);
	}

	@Test
	public void getHome_default() throws Exception {
		HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
		RestTemplate restTemplate = new RestTemplate();

		getHome(restTemplate);
	}

	@Test
	public void getHome_okHttp() throws Exception {
		OkHttpClient client = new OkHttpClient.Builder()
				.sslSocketFactory(context.getSocketFactory(), new DummyTrustManager())
				.build();

		OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(client);
		RestTemplate restTemplate = new RestTemplate(requestFactory);

		getHome(restTemplate);
	}

	@Test
	public void getHome_netty() throws Exception {
		Netty4ClientHttpRequestFactory requestFactory = new Netty4ClientHttpRequestFactory();
		requestFactory.setSslContext(new JdkSslContext(context, true, ClientAuth.NONE));
		RestTemplate restTemplate = new RestTemplate(requestFactory);

		getHome(restTemplate);
	}
	private void getHome(RestTemplate restTemplate) {
		ResponseEntity<String> response = restTemplate.getForEntity("https://h2demo.net", String.class);

		System.out.println(response.getHeaders());
		System.out.println(response.getBody());
	}

}
