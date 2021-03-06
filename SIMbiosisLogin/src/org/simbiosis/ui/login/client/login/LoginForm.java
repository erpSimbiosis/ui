package org.simbiosis.ui.login.client.login;

import java.util.Date;

import org.simbiosis.ui.login.client.Base64Utils;
import org.simbiosis.ui.login.client.LoginConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LoginForm extends Composite {

	private static LoginFormUiBinder uiBinder = GWT
			.create(LoginFormUiBinder.class);

	interface LoginFormUiBinder extends UiBinder<Widget, LoginForm> {
	}

	@UiField
	TextBox userName;
	@UiField
	TextBox password;
	@UiField
	Button loginSubmit;
	@UiField
	HorizontalPanel messageHolder;

	Label message = new Label();
	HorizontalPanel loadingPanel = new HorizontalPanel();

	LoginConfig loginConfig = null;

	public LoginForm() {

	}

	public LoginForm(LoginConfig loginConfig) {
		this.loginConfig = loginConfig;

		initWidget(uiBinder.createAndBindUi(this));
		//
		userName.getElement().setPropertyString("placeholder", "Nama pengguna");
		password.getElement().setPropertyString("placeholder", "Kata kunci");
		//
		message.setStyleName("login-errormessage");
		//
		HTMLPanel symbol = new HTMLPanel(
				"<i class=\"fa fa-spinner fa-fw fa-spin\" />");
		symbol.setStyleName("login-loadingsymbol");
		Label loadLabel = new Label("Loading...");
		loadLabel.setStyleName("login-loadingtext");
		loadingPanel.add(symbol);
		loadingPanel.add(loadLabel);
		//
		removeCookie();
	}

	private void loadSalt() {
		String url = Window.Location.getProtocol() + "//"
				+ Window.Location.getHost() + "/systemapi/session/salt";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable e) {
					Window.alert(e.getMessage());
				}

				public void onResponseReceived(Request request,
						Response response) {
					if (200 == response.getStatusCode()) {
						login(response.getText());
					} else {
						Window.alert("Received HTTP status code other than 200 : "
								+ response.getStatusText());
					}
				}
			});
		} catch (RequestException e) {
			// Couldn't connect to server
			Window.alert(e.getMessage());
		}

	}

	private void loginError() {
		hideLoading();
		showMessage("Sistem login gagal");
	}

	private void login(String salt) {
		String url = Window.Location.getProtocol() + "//"
				+ Window.Location.getHost() + "/systemapi/session/login";
		String passwordSalt = salt + password.getText();
		String param = "userName=" + userName.getText() + "&password=" + salt
				+ ":" + Base64Utils.toBase64(passwordSalt);
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
		builder.setHeader("Content-type", "application/x-www-form-urlencoded");
		try {
			builder.sendRequest(param, new RequestCallback() {

				public void onError(Request request, Throwable e) {
					Window.alert(e.getMessage());
				}

				public void onResponseReceived(Request request,
						Response response) {
					if (200 == response.getStatusCode()) {
						String result = response.getText();
						if (result.isEmpty()) {
							loginError();
						} else {
							hideLoading();
							onLoginSuccess(result);
						}
					} else {
						loginError();
					}
				}
			});
		} catch (RequestException e) {
			// Couldn't connect to server
			Window.alert(e.getMessage());
		}
	}

	@UiHandler("loginSubmit")
	public void onLogin(ClickEvent event) {
		showLoading();
		//
		loadSalt();
	}

	private void createCookie(String sessionName) {
		final long DURATION = 1000 * 60 * 60 * 24 * 14;
		// duration remembering login. 2 weeks in this example.
		Date expires = new Date(System.currentTimeMillis() + DURATION);
		Cookies.setCookie("simbiosis", sessionName, expires, null, "/", false);
	}

	private void removeCookie() {
		Cookies.removeCookie("simbiosis", "/");
	}

	private void onLoginSuccess(String result) {
		if (!result.isEmpty()) {
			//
			createCookie(result);
			//
			Window.Location.replace(loginConfig.getAfterLogin());
		} else {
			showMessage("Nama pengguna / kunci salah");
		}
	}

	private void showMessage(String text) {
		messageHolder.clear();
		messageHolder.add(message);
		message.setText(text);
	}

	private void showLoading() {
		messageHolder.clear();
		messageHolder.add(loadingPanel);
	}

	private void hideLoading() {
		messageHolder.clear();
	}
}
