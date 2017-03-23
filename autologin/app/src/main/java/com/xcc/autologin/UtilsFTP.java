package com.xcc.autologin;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;


public class UtilsFTP {

	private static String LOCAL_CHARSET = "GBK";
	// FTP协议里面，规定文件名编码为iso-8859-1
	private static String SERVER_CHARSET = "ISO-8859-1";

	public static final String ANONYMOUS_LOGIN = "anonymous";
	private FTPClient client;
	private boolean is_connected;

	public interface IProgressListener {
		void onProgress(long bytescount, long bytestotal);
	}

	public interface UploadFinishCallback {

		void notification(String dst, String src);
	}

	public String host;
	public int port;
	public String user;
	public String password;
	public boolean isTextMode;

	public UtilsFTP() {
		client = new FTPClient();
		is_connected = false;
	}

	public UtilsFTP(String _host, int _port, String _user, String _password, boolean isTextMode) {

		this();
		this.host = _host;
		this.port = _port;
		this.user = _user;
		this.password = _password;
		this.isTextMode = isTextMode;
	}

	public void connect() throws IOException {
		// Connect to server.
		try {
			client.connect(host, port);
		} catch (UnknownHostException ex) {
			throw new IOException("Can't find FTP server '" + host + "'");
		}

		// Check rsponse after connection attempt.
		int reply = client.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			disconnect();
			throw new IOException("Can't connect to server '" + host + "'");
		}

		if (user == "") {
			user = ANONYMOUS_LOGIN;
		}

		// Login.
		if (!client.login(user, password)) {
			is_connected = false;
			disconnect();
			throw new IOException("Can't login to server '" + host + "'");
		} else {
			is_connected = true;
		}

		//处理编码问题
		if (FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) {// 开启服务器对UTF-8的支持，如果服务器支持就用UTF-8编码，否则就使用本地编码（GBK）.
			LOCAL_CHARSET = "UTF-8";
		}

		client.setControlEncoding(LOCAL_CHARSET);

		// Set data transfer mode.
		if (isTextMode) {
			client.setFileType(FTP.ASCII_FILE_TYPE);
		} else {
			client.setFileType(FTP.BINARY_FILE_TYPE);
		}

	}

	public void sendSiteCommand(String args) throws IOException {

		client.sendSiteCommand(args);
	}

	public boolean isConnected() {
		return is_connected;
	}

	public void disconnect() throws IOException {

		if (client.isConnected()) {
			try {
				client.logout();
				client.disconnect();
				is_connected = false;
			} catch (IOException ex) {
			}
		}
	}

	public FTPClient getClient() {
		return client;
	}

	public String ChangeEncode(String s) throws UnsupportedEncodingException {
		return new String(s.getBytes(LOCAL_CHARSET), SERVER_CHARSET);
	}

	public void upload(String dstpath, String srcpath) throws IOException {
		upload(dstpath, new File(srcpath));
	}

	public void upload(String ftpFileName, File localFile) throws IOException {
		String mdkdir = ftpFileName.substring(0, ftpFileName.lastIndexOf("/") + 1);

		// File check.
		if (!localFile.exists()) {
			throw new IOException("Can't upload '" + localFile.getAbsolutePath() + "'. This file doesn't exist.");
		}

		final long fileTotal = localFile.length();
		// Upload.
		InputStream in = null;
		try {
			client.makeDirectory(ChangeEncode(mdkdir));
			// Use passive mode to pass firewalls.
			client.enterLocalPassiveMode();

			in = new FileInputStream(localFile);

			if (!client.storeFile(ChangeEncode(ftpFileName), in)) {
				throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
			}

		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {
			}
		}
	}

	public void uploadWithProgress(String ftpFileName, final File localFile, final IProgressListener listener) throws IOException {

		String mdkdir = ftpFileName.substring(0, ftpFileName.lastIndexOf("/") + 1);

		// File check.
		if (!localFile.exists()) {
			throw new IOException("Can't upload '" + localFile.getAbsolutePath() + "'. This file doesn't exist.");
		}

		final long fileTotal = localFile.length();
		// Upload.
		InputStream in = null;
		try {
			client.makeDirectory(ChangeEncode(mdkdir));
			// Use passive mode to pass firewalls.
			client.enterLocalPassiveMode();

			in = new ProgressBufferedInputStream(new FileInputStream(localFile),
					new ProgressBufferedInputStream.IProgressListener() {
						@Override
						public void onProgress(long len) {
							// TODO Auto-generated method stub
							if (listener != null)
								listener.onProgress(len, fileTotal);
						}
					});
			if (!client.storeFile(ChangeEncode(ftpFileName), in)) {
				throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
			}

		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {
			}
		}
	}
	
	public void uploadWithProgress(String ftpFileName, final InputStream in2, final IProgressListener listener) throws IOException {

		String mdkdir = ftpFileName.substring(0, ftpFileName.lastIndexOf("/") + 1);

		// File check.



		// Upload.
		InputStream in=null;
		try {
			final long fileTotal =in2.available();
			client.makeDirectory(ChangeEncode(mdkdir));
			// Use passive mode to pass firewalls.
			client.enterLocalPassiveMode();

			in = new ProgressBufferedInputStream(in2,
					new ProgressBufferedInputStream.IProgressListener() {
						@Override
						public void onProgress(long len) {
							// TODO Auto-generated method stub
							if (listener != null)
								listener.onProgress(len, fileTotal);
						}
					});
			if (!client.storeFile(ChangeEncode(ftpFileName), in)) {
				throw new IOException("Can't upload file '" + ftpFileName + "' to FTP server. Check FTP permissions and path.");
			}

		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ex) {
			}
		}
	}

	public void downloadWithProgress(String ftpFileName, final File localFile, final IProgressListener listener)
			throws IOException {
		// Download.
		OutputStream out = null;
		//final long fileTotal=localFile.length();
		try {
			// Use passive mode to pass firewalls.
			client.enterLocalPassiveMode();

			ftpFileName = ChangeEncode(ftpFileName);

			// Get file info.
			FTPFile[] fileInfoArray = client.listFiles(ftpFileName);
			if (fileInfoArray == null) {
				throw new FileNotFoundException("fileInfoArray=null,File " + ftpFileName + " was not found on FTP server.");
			}
			if (fileInfoArray.length == 0) {
				throw new FileNotFoundException("fileInfoArray.length==0,File " + ftpFileName + " was not found on FTP server.");
			}
			//
			// Check file size.
			FTPFile fileInfo = fileInfoArray[0];
			Log.d("fileInfo--xccdown->",fileInfo.toString());


			final long size = fileInfo.getSize();
			if (size > Integer.MAX_VALUE) {
				throw new IOException("File " + ftpFileName + " is too large.");
			}

			// Download file.
			out = new ProgressBufferedOutputStream(new FileOutputStream(localFile),
					new ProgressBufferedOutputStream.IProgressListener() {

						@Override
						public void onProgress(long len) {
							// TODO Auto-generated method stub
							listener.onProgress(len, size);
						}
					});
			if (!client.retrieveFile(ftpFileName, out)) {
				throw new IOException("Error loading file " + ftpFileName + " from FTP server. Check FTP permissions and path.");
			}

			out.flush();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
				}
			}
		}
	}




}
