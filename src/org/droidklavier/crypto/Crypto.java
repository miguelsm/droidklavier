package org.droidklavier.crypto;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Locale;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

public class Crypto {

	public static String encrypt(String str, String password) {

		String enc_msg = getPlainText(str);
		String key = getKeyText(str);

		AESEngine blockCipher = new AESEngine();

		CBCBlockCipher cbcCipher = new CBCBlockCipher(blockCipher);

		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcCipher);

		byte[] salt = new byte[8];
		SecureRandom secure = new SecureRandom();
		secure.nextBytes(salt);

		cipher.init(true, getKeyParamWithIv(password + key, salt, 128));

		byte[] encryptedData = new byte[cipher
				.getOutputSize(enc_msg.getBytes().length)];
		int noOfBytes = cipher.processBytes(enc_msg.getBytes(), 0,
				enc_msg.getBytes().length, encryptedData, 0);

		byte[] encryptedConfigData = null;

		try {
			cipher.doFinal(encryptedData, noOfBytes);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write("Salted__".getBytes());
			bos.write(salt);
			bos.write(encryptedData);
			encryptedConfigData = bos.toByteArray();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (encryptedConfigData != null) {
			encryptedData = Base64.encode(encryptedConfigData);
			return new String(encryptedData);
		}

		return "";
	}

	private static String getPlainText(String txt) {

		if ((txt.trim().equals("")) || (txt.length() < 50))
			return "";
		String[] str_tbl = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
				"m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
				"y", "z" };

		String strEnd = txt.substring(49, 50);

		int intStart = 0;
		for (int i = 0; i < str_tbl.length; i++) {
			if (!str_tbl[i].equals(strEnd.toLowerCase(Locale.getDefault())))
				continue;
			intStart = i;
			break;
		}

		return txt.substring(intStart, intStart + 10);
	}

	private static String getKeyText(String txt) {

		if ((txt.trim().equals("")) || (txt.length() < 50))
			return "";
		return txt.substring(30, 40);
	}

	private static ParametersWithIV getKeyParamWithIv(String keyphrase,
			byte[] salt, int aes_bit) {

		int iterationCount = 1;

		PBEParametersGenerator generator = new OpenSSLPBEParametersGenerator();

		generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(keyphrase
				.toCharArray()), salt, iterationCount);

		ParametersWithIV paramWithIv = (ParametersWithIV) generator
				.generateDerivedParameters(aes_bit, 128);

		return paramWithIv;
	}

}
