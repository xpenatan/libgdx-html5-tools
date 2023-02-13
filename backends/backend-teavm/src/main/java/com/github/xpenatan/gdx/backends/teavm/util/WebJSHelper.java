package com.github.xpenatan.gdx.backends.teavm.util;

import com.github.xpenatan.gdx.backends.teavm.TeaAgentInfo;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLCanvasElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLImageElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.StorageWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.WindowWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.XMLHttpRequestWrapper;
import com.github.xpenatan.gdx.backends.teavm.soundmanager.SoundManagerWrapper;

/**
 * @author xpenatan
 */
@Deprecated
public abstract class WebJSHelper {
    public static WebJSHelper JSHelper;

    public static WebJSHelper get() {
        return JSHelper;
    }

    public abstract HTMLCanvasElementWrapper getCanvas();

    public abstract WindowWrapper getCurrentWindow();

    public abstract TeaAgentInfo getAgentInfo();

    public abstract HTMLImageElementWrapper createImageElement();

    public abstract XMLHttpRequestWrapper creatHttpRequest();

    public abstract StorageWrapper getStorage();

    public abstract SoundManagerWrapper createSoundManager();

    public abstract WebJSGraphics getGraphics();

    public abstract WebJSApplication getApplication();
}
