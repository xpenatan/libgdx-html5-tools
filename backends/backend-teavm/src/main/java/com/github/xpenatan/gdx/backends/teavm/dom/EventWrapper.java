package com.github.xpenatan.gdx.backends.teavm.dom;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.EventTarget;

/**
 * @author xpenatan
 */
public interface EventWrapper extends JSObject {

    @JSProperty
    String getType();

    @JSProperty
    EventTargetWrapper getTarget();

    @JSProperty
    EventTargetWrapper getCurrentTarget();

    void preventDefault();

    void stopPropagation();

    float getDetail();
}
