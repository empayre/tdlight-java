package it.tdlight.client;

import it.tdlight.ExceptionHandler;
import it.tdlight.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.PhoneNumberAuthenticationSettings;
import it.tdlight.jni.TdApi.SetAuthenticationPhoneNumber;
import it.tdlight.jni.TdApi.UpdateAuthorizationState;

public class AuthorizationStateWaitAuthenticationDataHandler implements GenericUpdateHandler<UpdateAuthorizationState> {

	private final TelegramClient client;
	private final Authenticable authenticable;
	private final ExceptionHandler exceptionHandler;

	public AuthorizationStateWaitAuthenticationDataHandler(TelegramClient client,
			Authenticable authenticable,
			ExceptionHandler exceptionHandler) {
		this.client = client;
		this.authenticable = authenticable;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void onUpdate(UpdateAuthorizationState update) {
		if (update.authorizationState.getConstructor() == TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR) {
			authenticable.getAuthenticationSupplier().get().whenComplete((authData, ex) -> {
				if (ex != null) {
					exceptionHandler.onException(ex);
					return;
				}
				this.onAuthData(authData);
			});
		}
	}

	public void onAuthData(AuthenticationData authenticationData) {
		if (authenticationData.isBot()) {
			String botToken = authenticationData.getBotToken();
			TdApi.CheckAuthenticationBotToken response = new TdApi.CheckAuthenticationBotToken(botToken);
			client.send(response, ok -> {
				if (ok.getConstructor() == TdApi.Error.CONSTRUCTOR) {
					throw new TelegramError((TdApi.Error) ok);
				}
			}, exceptionHandler);
		} else if (authenticationData.isQrCode()) {
			TdApi.RequestQrCodeAuthentication response = new TdApi.RequestQrCodeAuthentication();
			client.send(response, ok -> {
				if (ok.getConstructor() == TdApi.Error.CONSTRUCTOR) {
					throw new TelegramError((TdApi.Error) ok);
				}
			}, exceptionHandler);
		} else {
			PhoneNumberAuthenticationSettings phoneSettings = new PhoneNumberAuthenticationSettings(false,
					false,
					false,
					false,
					null,
					null
			);

			String phoneNumber = authenticationData.getUserPhoneNumber();
			SetAuthenticationPhoneNumber response = new SetAuthenticationPhoneNumber(phoneNumber, phoneSettings);
			client.send(response, ok -> {
				if (ok.getConstructor() == TdApi.Error.CONSTRUCTOR) {
					throw new TelegramError((TdApi.Error) ok);
				}
			}, exceptionHandler);
		}
	}
}
