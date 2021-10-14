package com.example.ledControl.model.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class LedService {
	@Resource
	private ThreadPoolTaskExecutor asyncService;

	private final Logger logger = LoggerFactory.getLogger(LedService.class);

	public String hex10To16(int num) {
		return String.format("%08X", num);
	}

	public String area(int areaNum) {
		String result = "0";
		switch (areaNum) {
		case 0:
			result = "30";
			break;
		case 1:
			result = "31";
			break;
		case 2:
			result = "32";
			break;
		case 3:
			result = "33";
			break;
		case 4:
			result = "34";
			break;
		}
		return result;

	}

	public String effects(int effectsNum) {
		String result = "0";
		switch (effectsNum) {
		case 0:
			result = "30";
			break;
		case 1:
			result = "31";
			break;
		case 2:
			result = "32";
			break;
		case 3:
			result = "33";
			break;
		case 4:
			result = "34";
			break;
		case 5:
			result = "35";
			break;
		case 6:
			result = "36";
			break;
		case 7:
			result = "37";
			break;
		case 8:
			result = "38";
			break;
		case 9:
			result = "39";
			break;
		}
		return result;

	}

	public String gb2312(String a) {
		byte[] data = null;
		try {
			data = a.getBytes("GB2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String str = byteToHex(data);
		return str;

	}

	public String byteToHex(byte[] bytes) {
		char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] buf = new char[bytes.length * 2];
		int a = 0;
		int index = 0;
		for (byte b : bytes) {
			if (b < 0) {
				a = 256 + b;
			} else {
				a = b;
			}
			buf[index++] = HEX_CHAR[a / 16];
			buf[index++] = HEX_CHAR[a % 16];
		}
		return new String(buf);
	}

	public static byte[] hexStringToByteArray(String hexString) {
		hexString = hexString.replaceAll(" ", "");
		int len = hexString.length();
		byte[] bytes = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return bytes;
	}

	public byte[] byteMerger(byte[] bt1, byte[] bt2) {
		byte[] bt3 = new byte[bt1.length + bt2.length];
		System.arraycopy(bt1, 0, bt3, 0, bt1.length);
		System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
		return bt3;
	}

	public void send(String ip, int port, String news, int area, int effects) throws Exception {

		logger.info("ip:" + ip + "  port:" + port + "  发送的消息:" + news + "  更新的区域:" + area + " 更新的效果:" + effects);
//		LedService led = new LedService();
		Socket socket = null;
		try {
			socket = new Socket(ip, port);
			String str1 = "55 aa 00 00 01 01 00 d9 00 00 00 00 00 00 00 00 00 00 00 00 25 64 69 73 70";
			byte[] byte1 = hexStringToByteArray(str1);
			byte[] byte2 = hexStringToByteArray(area(area - 1));
			byte[] byte3 = byteMerger(byte1, byte2);
			String str2 = "3a";
			byte[] byte4 = hexStringToByteArray(str2);
			byte[] byte5 = byteMerger(byte3, byte4);
			byte[] byte6 = hexStringToByteArray(effects(effects));
			byte[] byte7 = byteMerger(byte5, byte6);
			byte[] data = hexStringToByteArray(gb2312(news));
			byte[] byte8 = byteMerger(byte7, data);
			String str3 = "00 00 0D 0A";
			byte[] byte9 = hexStringToByteArray(str3);
			byte[] byte10 = byteMerger(byte8, byte9);

			java.io.OutputStream os = socket.getOutputStream();
			os.write(byte10);
			os.close();

		} catch (IOException e) {
			logger.error(e.toString());
//			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		}

	}
}
