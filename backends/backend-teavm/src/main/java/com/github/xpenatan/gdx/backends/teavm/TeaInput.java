
package com.github.xpenatan.gdx.backends.teavm;

import com.badlogic.gdx.Input;

public interface TeaInput extends Input {

    /**
     * Resets all Input events (called on main loop after rendering)
     */
    void reset();
}