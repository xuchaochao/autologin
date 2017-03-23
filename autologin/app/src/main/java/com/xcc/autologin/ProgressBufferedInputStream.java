package com.xcc.autologin;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressBufferedInputStream extends BufferedInputStream {

	public interface IProgressListener {

		void onProgress(long len);

	}

	private IProgressListener listener;
	private long progress;
	private long lastUpdate;


	public ProgressBufferedInputStream(InputStream in) {
		super(in);
		// TODO Auto-generated constructor stub
	}

	public ProgressBufferedInputStream(InputStream in, IProgressListener listener) {
		super(in);
		progress = 0;
		lastUpdate = 0;
		this.listener = listener;

		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		// TODO Auto-generated method stub
		int count = super.read(buffer, byteOffset, byteCount);
		
		if(listener!=null){
			progress += count; 
			this.listener.onProgress(progress);
		}
		return count;
	}

}
