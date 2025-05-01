package it.fulminazzo.mojito.tokenizer;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

public class MockInputStream extends InputStream {
    private final Queue<Integer> buffer = new LinkedList<>();
    private boolean closed = false;
    @Getter
    private int mark;

    public MockInputStream(final String data) {
        for (int b : data.getBytes()) this.buffer.add(b);
    }

    @Override
    public int available() {
        return this.buffer.size();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public synchronized void mark(int i) {
        this.mark = i;
    }

    @Override
    public synchronized void reset() {
        this.mark = 0;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public int read() throws IOException {
        if (this.closed) throw new IOException("Stream is closed");
        else return this.buffer.poll();
    }

}
