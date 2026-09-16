package demo.usercart;

import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class GenerateRSAKeys {

	public static void main(String[] args) throws Exception {

		// 建立 RSA 金鑰產生器
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

		// RSA 2048 bit
		generator.initialize(2048);

		// 產生一組公私鑰
		KeyPair keyPair = generator.generateKeyPair();

		// 私鑰
		String privateKey = Base64.getMimeEncoder(64, "\n".getBytes())
				.encodeToString(keyPair.getPrivate().getEncoded());

		// 公鑰
		String publicKey = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded());

		// 建立 private.pem
		try (FileWriter writer = new FileWriter("private.pem")) {

			writer.write("-----BEGIN PRIVATE KEY-----\n" + privateKey + "\n-----END PRIVATE KEY-----");
		}

		// 建立 public.pem
		try (FileWriter writer = new FileWriter("public.pem")) {

			writer.write("-----BEGIN PUBLIC KEY-----\n" + publicKey + "\n-----END PUBLIC KEY-----");
		}

		System.out.println("RSA 金鑰產生完成");
	}
}
