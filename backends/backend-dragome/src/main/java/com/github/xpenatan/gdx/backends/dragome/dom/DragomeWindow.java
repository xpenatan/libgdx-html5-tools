package com.github.xpenatan.gdx.backends.dragome.dom;

import org.w3c.dom.Document;
import com.dragome.commons.javascript.ScriptHelper;
import com.dragome.web.enhancers.jsdelegate.JsCast;
import com.dragome.web.html.dom.Window;
import com.github.xpenatan.gdx.backend.web.dom.DocumentWrapper;
import com.github.xpenatan.gdx.backend.web.dom.LocationWrapper;
import com.github.xpenatan.gdx.backend.web.dom.WindowWrapper;

/**
 * @author xpenatan
 */
public class DragomeWindow implements WindowWrapper {

	Window window;

	WindowWrapper wrapper;

	public DragomeWindow() {
		window = Window.getInstance();
		Object windowObj = ScriptHelper.eval("window", null);
		wrapper = JsCast.castTo(windowObj, WindowWrapper.class);
	}

	@Override
	public DocumentWrapper getDocument() {
		Document document = Window.getDocument();
		DocumentWrapper document2 = JsCast.castTo(document, DocumentWrapper.class);
		return document2;
	}

	@Override
	public void requestAnimationFrame(Runnable runnable) {
		window.requestAnimationFrame(runnable);
	}

	@Override
	public int setTimeout(Runnable run, int delay) {
		//TODO needs to impl
		ScriptHelper.put("timeoutDelay", delay, this);
		int retValue = ScriptHelper.evalInt("wrapper.node.timeout(function() {ra.$run$void();}, timeoutDelay)", this);
		return retValue;
	}

	@Override
	public LocationWrapper getLocation () {
		LocationWrapper location = wrapper.getLocation();
		return location;
	}
}
