//usr/bin/env jshell --add-modules jdk.incubator.httpclient --show-version "$0" "$@"; exit $?

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

TrustManager dummy = new X509TrustManager() {
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
};

SSLContext context = SSLContext.getInstance("SSL");
context.init(null, new TrustManager[]{dummy}, null);

import jdk.incubator.http.*;

HttpClient client = HttpClient.newBuilder().sslContext(context).build();
HttpRequest req = HttpRequest.newBuilder().uri(new URI("https://h2demo.net")).GET().build();
HttpResponse<String> res = client.send(req, HttpResponse.BodyHandler.asString());
System.out.println(res.headers().map());
System.out.println(res.body());
/exit
