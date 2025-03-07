package emulate.java.io;

import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.Float32ArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.Int32ArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.Int8ArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.TypedArrays;
import com.github.xpenatan.gdx.backends.teavm.gen.Emulate;

@Emulate(valueStr = "java.io.Numbers")
public class NumbersEmu {

    static final double LN2 = Math.log(2);

    public static final int floatToIntBits(float f) {
        wfa.set(0, f);
        return wia.get(0);
    }

    static Int8ArrayWrapper wba = TypedArrays.createInt8Array(4);
    static Int32ArrayWrapper wia = TypedArrays.createInt32Array(wba.getBuffer(), 0, 1);
    static Float32ArrayWrapper wfa = TypedArrays.createFloat32Array(wba.getBuffer(), 0, 1);

    public static final float intBitsToFloat(int i) {
        wia.set(0, i);
        return wfa.get(0);
    }

    public static final long doubleToLongBits(Double d) {
        throw new RuntimeException("NYI");
    }

    public static final double longBitsToDouble(long l) {
        throw new RuntimeException("NYI");
    }

    public static long doubleToRawLongBits(double value) {
        throw new RuntimeException("NYI: Numbers.doubleToRawLongBits");
    }
}