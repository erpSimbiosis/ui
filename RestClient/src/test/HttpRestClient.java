package test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.DatatypeConverter;

public class HttpRestClient {

	String getSalt = "http://localhost:8080/systemapi/session/salt";
	String postLogin = "http://localhost:8080/systemapi/session/login";
	String getLogout = "http://localhost:8080/systemapi/session/logout";

	public static void main(String[] args) {
		new HttpRestClient().testIt();
	}

	public class MyThread extends Thread {

		public MyThread() {
		}

		@Override
		public void run() {
			String password = "cordova";
			String salt = get(getSalt);
			String passwordSalt = salt + password;
			String passwordB64 = DatatypeConverter
					.printBase64Binary(passwordSalt.getBytes());
			String result = post(postLogin, "userName=iwanaf&password=" + salt
					+ ":" + passwordB64);
			if (result.isEmpty()) {
				System.out.println("Login gagal");
			} else {
				result = result.replace("/", "%2f");
				System.out.println(result);
				result = get(getLogout + "/" + result);
				System.out.println(result);
			}
		}

	}

	private void testIt() {
		for (int i = 0; i < 1; i++) {
			MyThread thr = new MyThread();
			thr.start();
		}
	}

	private HttpURLConnection createConnection(String strUrl)
			throws IOException {
		URL url = new URL(strUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		return con;
	}

	private String read(HttpURLConnection con) {
		String input = "";
		String result = "";
		if (con != null) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				while ((input = br.readLine()) != null) {
					result += input;
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private String get(String url) {
		String result = "";
		try {
			HttpURLConnection con = createConnection(url);
			con.setRequestMethod("GET");
			con.setDoOutput(true);
			result = read(con);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String post(String url, String params) {
		String result = "";
		try {
			HttpURLConnection con = createConnection(url);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			con.setRequestProperty("Content-Length",
					"" + Integer.toString(params.getBytes().length));
			con.setRequestProperty("Content-Language", "en-US");
			con.setUseCaches(false);
			con.setDoInput(true);
			con.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(params);
			wr.flush();
			wr.close();
			//
			result = read(con);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
