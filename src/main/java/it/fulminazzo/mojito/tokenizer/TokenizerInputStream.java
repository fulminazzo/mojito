package it.fulminazzo.mojito.tokenizer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

/**
 * A wrapper for a generic {@link InputStream}.
 * It provides several methods to push back data into the buffer.
 */
class TokenizerInputStream extends InputStream {
    private final @NotNull InputStream inputStream;
    private final @NotNull LinkedList<Integer> buffer = new LinkedList<>();

    /**
     * Instantiates a new Tokenizer input stream.
     *
     * @param inputStream the input stream
     */
    public TokenizerInputStream(final @NotNull InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        if (this.buffer.isEmpty()) return this.inputStream.read();
        else return this.buffer.poll();
    }

    /**
     * Allows to push the given data back to the stream.
     * It will be then returned after the next {@link #read()}.
     *
     * @param data the data
     */
    public void push(final byte @NotNull ... data) {
        for (int d : data) this.buffer.add(d);
    }

    /**
     * Pushes the given string of data to the internal buffer.
     * They will be then read FIRST, as they have precedence over
     * other buffered data.
     *
     * @param data the data
     */
    public void push(final @NotNull String data) {
        byte[] chars = data.getBytes();
        for (int i = chars.length - 1; i >= 0; i--)
            this.buffer.addFirst((int) chars[i]);
    }

    /**
     * Clears any previously cached data in the buffer.
     */
    public void flush() {
        this.buffer.clear();
    }

    @Override
    public int read(byte @NotNull [] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte @NotNull [] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || length > bytes.length - offset)
            throw new IndexOutOfBoundsException(String.format(
                    "Range [%1$s, %1$s + %2$s) out of bounds for length %3$s",
                    offset, length, bytes.length));

        int index = 0;
        while (length > index && !this.buffer.isEmpty())
            bytes[offset + index++] = (byte) (int) this.buffer.poll();
        return index + this.inputStream.read(bytes, offset + index, length - index);
    }

    @Override
    public long skip(long bytes) throws IOException {
        long skipped = 0;
        while (bytes > 0 && !this.buffer.isEmpty()) {
            this.buffer.remove();
            bytes--;
            skipped++;
        }
        return skipped + this.inputStream.skip(bytes);
    }

    @Override
    public int available() throws IOException {
        return this.buffer.size() + this.inputStream.available();
    }

    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }

    @Override
    public synchronized void mark(int i) {
        this.inputStream.mark(i);
    }

    @Override
    public synchronized void reset() throws IOException {
        this.inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return this.inputStream.markSupported();
    }

}
