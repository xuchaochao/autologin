package com.xcc.autologin;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TCPClientSocket {
	Socket client = null;
	InputStream in;
	PrintStream out;

	int readbuffsize = 1024;
	int connectTimeout = 3 * 1000;

	public TCPClientSocket() {
		this.readbuffsize = 1024;
	}

	public TCPClientSocket(int _readbuffsize) {
		this.readbuffsize = _readbuffsize;
	}

	public interface IReadBytesCallBack {
		void onRead(byte[] b);
	}

	public IReadBytesCallBack iReadBytesCallBack = null;

	public void ReadBytesThreadStart() {
		new Thread() {

			public void run() {
				int count = 0;

				try {
					int ch;
					byte[] buffer = new byte[readbuffsize];//??��?34
					while (!Thread.interrupted()) {
						count = in.read(buffer, 0, buffer.length);
						if (count > 0) {
							byte[] readbuffer = new byte[count];
							System.arraycopy(buffer, 0, readbuffer, 0, count);
							iReadBytesCallBack.onRead(readbuffer);
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

			};
		}.start();
	}

	public void connect(String hostname, int port) throws Exception {
		client = new Socket();
		client.connect(new InetSocketAddress(hostname, port), connectTimeout);

		in = client.getInputStream();
		out = new PrintStream(client.getOutputStream());
		//out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
		//client.setOOBInline(true);
		if (iReadBytesCallBack != null)
			ReadBytesThreadStart();
	}

	public void disconnect() {
		try {
			in.close();
			out.close();
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void startSendCMDThread() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					try {
						Thread.sleep(10);
						int count = lstCmd.size();
						if (count == 0)
							continue;
						Iterator<String> it = lstCmd.iterator();
						while (it.hasNext()) {
							Thread.sleep(400);
							  String value=it.next();
							  writestring(value);
							  it.remove();
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}.start();
	}

	public List lstCmd = new ArrayList<String>();

	public void write(byte[] buff) throws Exception {
		out.write(buff);
		out.flush();
	}

	public void writestring(String cmd) {
		out.println(cmd);
		out.flush();
	}
	
	public void addWriteQueue(String cmd) {
		lstCmd.add(cmd);
	}

	public boolean isServerClose() {
		try {
			client.sendUrgentData(0xFF);//????1?????????????????????????????????��??????????????????????????  
			return false;
		} catch (Exception se) {
			return true;
		}
	}

}
