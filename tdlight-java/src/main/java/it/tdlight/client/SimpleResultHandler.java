package it.tdlight.client;

import it.tdlight.ConstructorDetector;
import it.tdlight.ResultHandler;
import it.tdlight.jni.TdApi.Object;
import it.tdlight.jni.TdApi.Update;

public class SimpleResultHandler<T extends Update> implements ResultHandler<Update> {

	private final int updateConstructor;
	private final GenericUpdateHandler<? super T> handler;

	public SimpleResultHandler(Class<T> type, GenericUpdateHandler<? super T> handler) {
		this.updateConstructor = ConstructorDetector.getConstructor(type);
		this.handler = handler;
	}

	@Override
	public void onResult(Object update) {
		if (update.getConstructor() == updateConstructor) {
			//noinspection unchecked
			handler.onUpdate((T) update);
		}
	}
}
