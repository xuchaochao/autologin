package com.xcc.autologin;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProgressBufferedOutputStream extends BufferedOutputStream {

	public interface IProgressListener {

		void onProgress(long len);

	}

	private static final int TEN_KILOBYTES = 1024 * 10;
	private IProgressListener listener;
	private long progress;
	private long lastUpdate;

	
	public ProgressBufferedOutputStream(OutputStream out) {
		super(out);
		// TODO Auto-generated constructor stub
	}

	public ProgressBufferedOutputStream(OutputStream out, IProgressListener listener) {
		super(out);
		progress = 0;
		lastUpdate = 0;
		this.listener = listener;
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized void write(byte[] buffer, int offset, int length) throws IOException {
		// TODO Auto-generated method stub

		super.write(buffer, offset, length);

		if (this.listener != null) {
			if (length > 0) progress += length;
			this.listener.onProgress(progress);
		}

	}

}
