package it.fulminazzo.mojito.tokenizer;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A wrapper for a generic {@link InputStream}.
 * It provides several methods to push back data into the buffer.
 */
@RequiredArgsConstructor
class TokenizerInputStream extends InputStream {
    private final @NotNull InputStream inputStream;
    private final @NotNull Queue<Integer> buffer = new LinkedList<>();

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
     * Allows to push the given data back to the stream.
     * It will be then returned after the next {@link #read()}.
     *
     * @param data the data
     */
    public void push(final @NotNull String data) {
        push(data.getBytes());
    }

    @Override
    public int read(byte @NotNull [] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    @Override
    public int read(byte @NotNull [] bytes, int offset, int length) throws IOException {
        return this.inputStream.read(bytes, offset, length);
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
        return this.inputStream.available();
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
