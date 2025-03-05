package com.github.xpenatan.gdx.backends.teavm.dom;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.TouchEventTarget;

/**
 * @author xpenatan
 */
public interface TouchWrapper extends JSObject {
    @JSProperty
    int getIdentifier();

    @JSProperty
    int getScreenX();

    @JSProperty
    int getScreenY();

    @JSProperty
    int getClientX();

    @JSProperty
    int getClientY();

    @JSProperty
    int getPageX();

    @JSProperty
    int getPageY();

    @JSProperty
    TouchEventTarget getTarget();

    @JSProperty
    float getRadiusX();

    @JSProperty
    float getRadiusY();

    @JSProperty
    float getRotationAngle();

    @JSProperty
    float getForce();

    default int getRelativeX(HTMLCanvasElementWrapper target) {
        return this.getClientX() - target.getAbsoluteLeft() + target.getScrollLeft() + target.getOwnerDocument().getScrollLeft();
    }

    default int getRelativeY(HTMLCanvasElementWrapper target) {
        return this.getClientY() - target.getAbsoluteTop() + target.getScrollTop() + target.getOwnerDocument().getScrollTop();
    }
}
