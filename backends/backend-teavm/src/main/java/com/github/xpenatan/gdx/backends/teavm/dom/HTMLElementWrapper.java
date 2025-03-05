package com.github.xpenatan.gdx.backends.teavm.dom;

import org.teavm.jso.JSProperty;

/**
 * @author xpenatan
 */
public interface HTMLElementWrapper extends ElementWrapper {

    HTMLElementWrapper getOffsetParent();

    int getOffsetTop();

    @JSProperty
    int getOffsetLeft();

    @JSProperty
    int getAbsoluteLeft();

    @JSProperty
    int getAbsoluteTop();
}
