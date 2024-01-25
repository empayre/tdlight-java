package it.tdlight.client;

import it.tdlight.ExceptionHandler;
import it.tdlight.TelegramClient;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.AuthorizationStateWaitRegistration;
import it.tdlight.jni.TdApi.RegisterUser;
import it.tdlight.jni.TdApi.UpdateAuthorizationState;

public class AuthorizationStateWaitRegistrationHandler implements GenericUpdateHandler<UpdateAuthorizationState> {

	private final TelegramClient client;
	private final ClientInteraction clientInteraction;
	private final ExceptionHandler exceptionHandler;

	public AuthorizationStateWaitRegistrationHandler(TelegramClient client,
			ClientInteraction clientInteraction,
			ExceptionHandler exceptionHandler) {
		this.client = client;
		this.clientInteraction = clientInteraction;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public void onUpdate(UpdateAuthorizationState update) {
		if (update.authorizationState.getConstructor() == AuthorizationStateWaitRegistration.CONSTRUCTOR) {
			TdApi.AuthorizationStateWaitRegistration authorizationState = (TdApi.AuthorizationStateWaitRegistration) update.authorizationState;
			ParameterInfoTermsOfService tos = new ParameterInfoTermsOfService(authorizationState.termsOfService);
			clientInteraction
					.onParameterRequest(InputParameter.TERMS_OF_SERVICE, tos)
					.thenCompose(ignored -> clientInteraction
							.onParameterRequest(InputParameter.ASK_FIRST_NAME, new EmptyParameterInfo()))
					.thenCompose(firstName -> clientInteraction
							.onParameterRequest(InputParameter.ASK_LAST_NAME, new EmptyParameterInfo()).thenAccept(lastName -> {
						if (firstName == null || firstName.isEmpty()) {
							exceptionHandler.onException(new IllegalArgumentException("First name must not be null or empty"));
							return;
						}
						if (firstName.length() > 64) {
							exceptionHandler.onException(new IllegalArgumentException("First name must be under 64 characters"));
							return;
						}
						if (lastName == null) {
							exceptionHandler.onException(new IllegalArgumentException("Last name must not be null"));
							return;
						}
						if (lastName.length() > 64) {
							exceptionHandler.onException(new IllegalArgumentException("Last name must be under 64 characters"));
							return;
						}
						RegisterUser response = new RegisterUser(firstName, lastName);
						client.send(response, ok -> {
							if (ok.getConstructor() == TdApi.Error.CONSTRUCTOR) {
								throw new TelegramError((TdApi.Error) ok);
							}
						}, exceptionHandler);
					}))
					.whenComplete((ignored, ex) -> {
						if (ex != null) {
							exceptionHandler.onException(ex);
						}
					});
		}
	}
}
