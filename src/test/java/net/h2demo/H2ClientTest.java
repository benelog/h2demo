package net.h2demo;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
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
		System.setProperty("io.netty.noUnsafe", "true");
		Netty4ClientHttpRequestFactory requestFactory = new Netty4ClientHttpRequestFactory();
		requestFactory.setSslContext(new JdkSslContext(context, true, ClientAuth.NONE));
		SslContext sslContext = new JdkSslContext(context, true, ClientAuth.NONE);
		// 아래와 같이 SslContext를 생성해도 됨.
		// SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		requestFactory.setSslContext(sslContext);
		RestTemplate restTemplate = new RestTemplate(requestFactory);

		getHome(restTemplate);
	}

	@Test
	public void getHome_webClient() throws InterruptedException {
		System.setProperty("io.netty.noUnsafe", "true");

		SslContext sslContext = new JdkSslContext(context, true, ClientAuth.NONE);
		ClientHttpConnector connector = new ReactorClientHttpConnector(opt -> opt.sslContext(sslContext));
		WebClient client = WebClient.builder()
				.baseUrl("https://h2demo.net")
				.clientConnector(connector)
				.build();

		client.get()
				.uri("/")
				.exchange()
				.flatMap(res -> res.toEntity(String.class))
				.subscribe(entity -> {
					System.out.println(entity.getBody());
					System.out.println(entity.getHeaders());

				});

		TimeUnit.SECONDS.sleep(1);

	}
	private void getHome(RestTemplate restTemplate) {
		ResponseEntity<String> response = restTemplate.getForEntity("https://h2demo.net", String.class);

		System.out.println(response.getHeaders());
		System.out.println(response.getBody());
	}

}
