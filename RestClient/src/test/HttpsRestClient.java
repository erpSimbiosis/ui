package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsRestClient {

	String strFunc = "http://tanmiya-artha.no-ip.biz/RestWar/MyRESTApplication/";

	public static void main(String[] args) {
		new HttpsRestClient().testIt();
	}

	public class MyThread extends Thread {

		HttpsURLConnection con = null;

		public MyThread(String name) {
			try {
				URL url = new URL(strFunc + name);
				con = (HttpsURLConnection) url.openConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			// dumpl all cert info
			// print_https_cert(con);
			// dump all the content
			print_content(con);
		}

	}

	public HttpsRestClient() {
		// Jangan pakai SNI extension
		System.setProperty("jsse.enableSNIExtension", "false");
	}

	private void testIt() {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		// use hostname
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			public boolean verify(String hostname, SSLSession sslSession) {
				if (hostname.equals("tanmiya-artha.no-ip.biz")) {
					return true;
				}
				return false;
			}
		});

		for (int i = 0; i < 50; i++) {
			MyThread thr = new MyThread("Test"+i);
			thr.start();
		}
		// Now you can access an https URL without having the certificate in the
		// truststore
//		try {
//			URL url = new URL(
//					"https://tanmiya-artha.no-ip.biz/RestWar/MyRESTApplication/Iwan");
//			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
//			// dumpl all cert info
//			// print_https_cert(con);
//			// dump all the content
//			print_content(con);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

	private void print_https_cert(HttpsURLConnection con) {

		if (con != null) {

			try {

				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : "
							+ cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : "
							+ cert.getPublicKey().getFormat());
					System.out.println("\n");
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void print_content(HttpsURLConnection con) {
		if (con != null) {

			try {

				System.out.println("****** Content of the URL ********");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						con.getInputStream()));

				String input;

				while ((input = br.readLine()) != null) {
					System.out.println(input);
				}
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
