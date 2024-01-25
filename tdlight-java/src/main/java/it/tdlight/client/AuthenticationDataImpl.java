package it.tdlight.client;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class AuthenticationDataImpl implements SimpleAuthenticationSupplier<AuthenticationDataImpl> {

	private final String userPhoneNumber;
	private final String botToken;
	private final boolean test;
	/**
	 * Safe string representation of the bot token
	 */
	private final String botTokenId;

	AuthenticationDataImpl(String userPhoneNumber, String botToken, boolean test) {
		if ((userPhoneNumber == null) == (botToken == null)) {
			throw new IllegalArgumentException("Please use either a bot token or a phone number");
		}
		if (botToken != null) {
			if (botToken.length() < 5 || botToken.length() > 200) {
				throw new IllegalArgumentException("Bot token is invalid: " + botToken);
			}
		}
		this.userPhoneNumber = userPhoneNumber;
		this.botToken = botToken;
		this.test = test;
		if (botToken != null) {
			String[] parts = botToken.split(":", 2);
			if (parts.length > 0) {
				botTokenId = parts[0];
			} else {
				botTokenId = "";
			}
		} else {
			botTokenId = "";
		}
	}

	@Override
	public boolean isQrCode() {
		return false;
	}

	@Override
	public boolean isBot() {
		return botToken != null;
	}

	public boolean isTest() {
		return test;
	}

	@Override
	public String getUserPhoneNumber() {
		if (userPhoneNumber == null) {
			throw new UnsupportedOperationException("This is not a user");
		}
		return userPhoneNumber;
	}

	@Override
	public String getBotToken() {
		if (botToken == null) {
			throw new UnsupportedOperationException("This is not a bot");
		}
		return botToken;
	}

	@Override
	public String toString() {
		String value;
		if (userPhoneNumber != null) {
			value = userPhoneNumber;
		} else  {
			value = botTokenId;
		}
		if (test) {
			return value + " (test)";
		} else {
			return value;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AuthenticationDataImpl that = (AuthenticationDataImpl) o;
		return Objects.equals(userPhoneNumber, that.userPhoneNumber) && Objects.equals(botToken, that.botToken)
				&& Objects.equals(test, that.test);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userPhoneNumber, botToken, test);
	}

	@Override
	public CompletableFuture<AuthenticationDataImpl> get() {
		return completedFuture(this);
	}
}
