/*
 *  Copyright 2015 Alexey Andreev.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.teavm.classlib.java.nio;

import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.HasArrayBufferView;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.Int8ArrayNative;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.Int8ArrayWrapper;
import java.util.Objects;
import org.teavm.classlib.java.lang.TComparable;

public abstract class TByteBuffer extends TBuffer implements TComparable<TByteBuffer>, HasArrayBufferView {
    int start;
    Int8ArrayNative array;
    byte[] bkArray;
    TByteOrder order = TByteOrder.BIG_ENDIAN;

    TByteBuffer(int start, int capacity, byte[] array, int position, int limit) {
        super(capacity);
        this.start = start;
        int length = array.length;
        bkArray = new byte[length];
        this.array = new Int8ArrayNative(length);
        putArray(array);
        this.position = position;
        this.limit = limit;
    }

    public void putArray(byte[] array) {
        for(int i = 0; i < array.length; i++) {
            byte b = array[i];
            this.array.set(i, b);
        }
    }

    @Override
    public Int8ArrayWrapper getArrayBufferView() {
        return array.getBuffer();
    }

    @Override
    public void setInt8ArrayNative(Int8ArrayNative array) {
        this.array = array;
        if(capacity == 0) {
            // Only update buffer data if capacity is 0.
            // The reason for this is to keep the original data if javascript buffer recreates
            capacity = array.getLength();
            limit(capacity);
            position(0);
        }
    }

    public static TByteBuffer allocateDirect(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity is negative: " + capacity);
        }
        return new TByteBufferImpl(capacity, true);
    }

    public static TByteBuffer allocate(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity is negative: " + capacity);
        }
        return new TByteBufferImpl(capacity, false);
    }

    public static TByteBuffer wrap(byte[] array, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, array.length);
        return new TByteBufferImpl(0, array.length, array, offset, offset + length, false, false);
    }

    public static TByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public abstract TByteBuffer slice();

    public abstract TByteBuffer duplicate();

    public abstract TByteBuffer asReadOnlyBuffer();

    public abstract byte get();

    public abstract TByteBuffer put(byte b);

    public abstract byte get(int index);

    public abstract TByteBuffer put(int index, byte b);

    public TByteBuffer get(byte[] dst, int offset, int length) {
        if (offset < 0 || offset > dst.length) {
            throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + dst.length + ")");
        }
        if (offset + length > dst.length) {
            throw new IndexOutOfBoundsException("The last byte in dst " + (offset + length) + " is outside "
                    + "of array of size " + dst.length);
        }
        if (remaining() < length) {
            throw new TBufferUnderflowException();
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
        }
        int pos = position + start;
        for (int i = 0; i < length; ++i) {
            dst[offset++] = array.get(pos++);
        }
        position += length;
        return this;
    }

    public TByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public TByteBuffer put(TByteBuffer src) {
        return put(src.array, src.start + src.position, src.remaining());
    }

    public TByteBuffer put(byte[] src, int offset, int length) {
        if (length == 0) {
            return this;
        }
        if (isReadOnly()) {
            throw new TReadOnlyBufferException();
        }
        if (remaining() < length) {
            throw new TBufferOverflowException();
        }
        if (offset < 0 || offset > src.length) {
            throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + src.length + ")");
        }
        if (offset + length > src.length) {
            throw new IndexOutOfBoundsException("The last byte in src " + (offset + length) + " is outside "
                    + "of array of size " + src.length);
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
        }
        int pos = position + start;
        for (int i = 0; i < length; ++i) {
            byte b = src[offset++];
            array.set(pos++, b);
        }
        position += length;
        return this;
    }

    public TByteBuffer put(Int8ArrayNative src, int offset, int length) {
        if (length == 0) {
            return this;
        }
        if (isReadOnly()) {
            throw new TReadOnlyBufferException();
        }
        if (remaining() < length) {
            throw new TBufferOverflowException();
        }
        if (offset < 0 || offset > src.getLength()) {
            throw new IndexOutOfBoundsException("Offset " + offset + " is outside of range [0;" + src.getLength() + ")");
        }
        if (offset + length > src.getLength()) {
            throw new IndexOutOfBoundsException("The last byte in src " + (offset + length) + " is outside "
                    + "of array of size " + src.getLength());
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("Length " + length + " must be non-negative");
        }
        int pos = position + start;
        for (int i = 0; i < length; ++i) {
            byte b = src.get(offset++);
            array.set(pos++, b);
        }
        position += length;
        return this;
    }

    public final TByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    @Override
    public boolean hasArray() {
        return true;
    }

    @Override
    public final byte[] array() {
        int length = array.getLength();
        for(int i = 0; i < length; i++) {
            bkArray[i] = array.get(i);
        }
        return bkArray;
    }

    @Override
    public int arrayOffset() {
        return start;
    }

    public abstract TByteBuffer compact();

    @Override
    public abstract boolean isDirect();

    @Override
    public String toString() {
        return "[ByteBuffer position=" + position + ", limit=" + limit + ", capacity=" + capacity + ", mark "
                + (mark >= 0 ? " at " + mark : " is not set") + "]";
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        int pos = position + start;
        for (int i = position; i < limit; ++i) {
            hashCode = 31 * hashCode + array.get(pos++);
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TByteBuffer)) {
            return false;
        }
        TByteBuffer other = (TByteBuffer) obj;
        int sz = remaining();
        if (sz != other.remaining()) {
            return false;
        }
        int a = position + start;
        int b = other.position + other.start;
        for (int i = 0; i < sz; ++i) {
            if (array.get(a++) != other.array.get(b++)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(TByteBuffer other) {
        if (this == other) {
            return 0;
        }
        int sz = Math.min(remaining(), other.remaining());
        int a = position + start;
        int b = other.position + other.start;
        for (int i = 0; i < sz; ++i) {
            int r = Byte.compare(array.get(a++), other.array.get(b++));
            if (r != 0) {
                return r;
            }
        }
        return Integer.compare(remaining(), other.remaining());
    }

    public final TByteOrder order() {
        return order;
    }

    public final TByteBuffer order(TByteOrder bo) {
        order = bo;
        return this;
    }

    public abstract char getChar();

    public abstract TByteBuffer putChar(char value);

    public abstract char getChar(int index);

    public abstract TByteBuffer putChar(int index, char value);

    public abstract TCharBuffer asCharBuffer();

    public abstract short getShort();

    public abstract TByteBuffer putShort(short value);

    public abstract short getShort(int index);

    public abstract TByteBuffer putShort(int index, short value);

    public abstract TShortBuffer asShortBuffer();

    public abstract int getInt();

    public abstract TByteBuffer putInt(int value);

    public abstract int getInt(int index);

    public abstract TByteBuffer putInt(int index, int value);

    public abstract TIntBuffer asIntBuffer();

    public abstract long getLong();

    public abstract TByteBuffer putLong(long value);

    public abstract long getLong(int index);

    public abstract TByteBuffer putLong(int index, long value);

    public abstract TLongBuffer asLongBuffer();

    public abstract float getFloat();

    public abstract TByteBuffer putFloat(float value);

    public abstract float getFloat(int index);

    public abstract TByteBuffer putFloat(int index, float value);

    public abstract TFloatBuffer asFloatBuffer();

    public abstract double getDouble();

    public abstract TByteBuffer putDouble(double value);

    public abstract double getDouble(int index);

    public abstract TByteBuffer putDouble(int index, double value);

    public abstract TDoubleBuffer asDoubleBuffer();

    @Override
    public final TByteBuffer mark() {
        super.mark();
        return this;
    }

    @Override
    public final TByteBuffer reset() {
        super.reset();
        return this;
    }

    @Override
    public final TByteBuffer clear() {
        super.clear();
        return this;
    }

    @Override
    public final TByteBuffer flip() {
        super.flip();
        return this;
    }

    @Override
    public final TByteBuffer rewind() {
        super.rewind();
        return this;
    }

    @Override
    public TByteBuffer limit(int newLimit) {
        super.limit(newLimit);
        return this;
    }

    @Override
    public TByteBuffer position(int newPosition) {
        super.position(newPosition);
        return this;
    }
}
