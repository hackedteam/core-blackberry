/*
 * Copyright  2004 Mass Dosage
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package za.co.massdosage.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Ant task for generating symmetric encryption keys and encrypting and
 * decrypting files.
 * 
 * @author mass
 */
public class Crypter extends Task {

	/**
	 * The default cipher transformation used for encryption or decryption.
	 */
	public static final String DEFAULT_CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

	/**
	 * The default algorithm used if generating a key.
	 */
	public static final String DEFAULT_KEY_ALGORITHM = "AES/CBC";

	private String keyFile;
	private File inputFile;
	private File outputFile;
	private boolean encrypt = true;
	private boolean generateKey = false;
	private String cipherTransformation = DEFAULT_CIPHER_TRANSFORMATION;
	private String keyAlgorithm = DEFAULT_KEY_ALGORITHM;

	private IvParameterSpec ivSpec;

	/**
	 * Logs an informational message. This method is required so that this task
	 * can be used outside of ant.
	 * 
	 * @param message
	 *            Message to log.
	 */
	private void logInfo(String message) {
		if (this.getProject() != null) { // we are running in ant, so use ant
											// log
			this.log(message, Project.MSG_INFO);
		} else { // we are running outside of ant, log to System.out
			System.out.println(message);
		}
	}

	/**
	 * Reads the contents of the key file and converts this into a
	 * <code>Key</code>.
	 * 
	 * @return The <code>Key</code> object.
	 * @throws BuildException
	 *             If the contents of the key file cannot be read.
	 */
	private SecretKey readKey() throws BuildException {
		if (this.keyFile == null) {
			throw new BuildException("No 'keyFile' specified, cannot continue.");
		}

		try {

			this.logInfo(" key: " + keyFile + " " + keyFile.length());

			/*KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(keyFile.getBytes());
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();*/
			
			String salt="sale";
						
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			
			for(int i=0; i<128;i++){
				digest.update(salt.getBytes());
				digest.update(keyFile.getBytes());
				digest.update(digest.digest());
			}
			
			byte[] sha1 = digest.digest();
			
			byte[] aes_key = new byte[16];
			System.arraycopy(sha1, 0, aes_key, 0, aes_key.length);
									
			SecretKey secret = new SecretKeySpec(aes_key, "AES");

			return secret;

		} catch (Exception e){
if(Cfg.EXCEPTION){Check.log(e);}

			this.logInfo("readKey error: " + e);
			return null;
		}

	}

	/**
	 * Initialises a <code>Cipher</code> in the mode set in the ant task
	 * (encrypt or decrypt) with the passed <code>Key</code>.
	 * 
	 * @param key
	 *            The <code>Key</code> which the <code>Cipher</code> will use
	 *            for encryption or decryption.
	 * @return The initialised <code>Cipher</code>.
	 * @throws BuildException
	 *             If an error occurs initialising the cipher.
	 */
	private Cipher initialiseCipher(Key key) throws BuildException {
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(this.cipherTransformation);
			final byte[] iv = new byte[16];
			Arrays.fill(iv, (byte) 0);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);

			if (encrypt) {
				cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
				this.logInfo("Initialised cipher to perform encryption using " + this.cipherTransformation);
				this.logInfo("key: " + hex(key.getEncoded()));
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
				this.logInfo("Initialised cipher to perform decryption using " + this.cipherTransformation);
			}
		} catch (NoSuchAlgorithmException e){
if(Cfg.EXCEPTION){Check.log(e);}

			throw new BuildException("Cipher transformation algorithm [" + this.cipherTransformation
					+ "] not supported", e);
		} catch (NoSuchPaddingException e){
if(Cfg.EXCEPTION){Check.log(e);}

			throw new BuildException("Cipher padding scheme not supported", e);
		} catch (InvalidKeyException e){
if(Cfg.EXCEPTION){Check.log(e);}

			this.logInfo("Error: " + e);
			throw new BuildException("Invalid key for cipher", e);
		} catch (InvalidAlgorithmParameterException e){
if(Cfg.EXCEPTION){Check.log(e);}

			this.logInfo("Error: " + e);
			throw new BuildException("Invalid AlgorithmParameter for cipher", e);
		}
		return cipher;
	}

	private String hex(byte[] data) {
		int offset = 0;
		int length = data.length;
		final StringBuffer buf = new StringBuffer();
		for (int i = offset; i < offset + length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int twohalfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (twohalfs++ < 1);
		}
		return buf.toString();
	}

	/**
	 * Performs the encryption/decryption according to the state of the passed
	 * <code>Cipher</code>, using the input and output files set in the ant
	 * task.
	 * 
	 * @param cipher
	 *            An initialised <code>Cipher</code> to use for the
	 *            encryption/decryption.
	 * @throws BuildException
	 *             If the input or output files cannot be found, read, or
	 *             written to; or if an error occurs performing the
	 *             encryption/decryption.
	 */
	private void crypt(Cipher cipher) throws BuildException {
		FileInputStream in = null;
		try {
			in = new FileInputStream(this.inputFile);
		} catch (FileNotFoundException e){
if(Cfg.EXCEPTION){Check.log(e);}

			throw new BuildException("Could not find input file " + this.inputFile, e);
		}
		FileOutputStream fileout = null;
		try {
			fileout = new FileOutputStream(this.outputFile);
		} catch (FileNotFoundException e){
if(Cfg.EXCEPTION){Check.log(e);}

			throw new BuildException("Invalid output file " + this.outputFile, e);
		}

		CipherOutputStream out = new CipherOutputStream(fileout, cipher);
		byte[] buffer = new byte[8192];
		int length;
		try {
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
		} catch (IOException e){
if(Cfg.EXCEPTION){Check.log(e);}

			throw new BuildException("Error writing output file " + this.outputFile, e);
		}
		this.logInfo("Performed cryptographic transformation on " + this.inputFile.getAbsolutePath() + " to "
				+ this.outputFile.getAbsolutePath());
	}

	/**
	 * Called by the project to perform the encryption or decryption using the
	 * parameters set in the task.
	 * 
	 * @throws BuildException
	 *             If something goes wrong executing this task.
	 */
	public void execute() throws BuildException {

		if (!(this.inputFile == null && this.outputFile == null)) { // if input
																	// or output
																	// files
																	// specified,
																	// need to
																	// attmept
																	// enc/dec
			Key key = this.readKey();
			this.logInfo("key read " + (key != null));
			Cipher cipher = this.initialiseCipher(key);
			this.crypt(cipher);
		}
	}

	/**
	 * Sets the algorithm used to generate a <code>Key</code>. If this is not
	 * set, then the default value specified by
	 * <code>DEFAULT_KEY_ALGORITHM</code> will be used.
	 * 
	 * @param keyAlgorithm
	 *            The standard name of the requested key algorithm.
	 */
	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	/**
	 * Sets the location of the <code>File</code> containing the
	 * <code>Key</code> to be used for encryption/decryption.
	 * 
	 * @param keyFile
	 *            The location of the key file.
	 */
	public void setKeyFile(String key) {

		this.keyFile = key;
	}

	/**
	 * Sets the location of the input <code>File</code> that is to be
	 * encrypted/decrypted.
	 * 
	 * @param inputFile
	 *            The location of the input file.
	 */
	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * Sets the location of the output <code>File</code> that is the results of
	 * the encryption/decryption.
	 * 
	 * @param outputFile
	 *            The location of the output file.
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Sets the mode that is used to determine whether encryption or decryption
	 * will be performed. If the value "true" is passed to this method then
	 * encryption will be performed, if the value "false" is passed then
	 * decryption will be performed. If this value is not set, encryption will
	 * be performed by default.
	 * 
	 * @param encrypt
	 *            Whether to perform encryption or decryption.
	 */
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	/**
	 * Sets the cipher transformation that will be used to perform the
	 * encryption/decryption. If this is not set, then the default value
	 * specified by <code>DEFAULT_CIPHER_TRANSFORMATION</code> will be used.
	 * 
	 * @param transformation
	 *            The name of the transformation, for example
	 *            <i>Blowfish/ECB/PKCS5Padding</i>.
	 */
	public void setCipherTransformation(String transformation) {
		this.cipherTransformation = transformation;
	}

	/**
	 * Calcola il SHA1 del messaggio, usando la crypto api.
	 * 
	 * @param message
	 *            the message
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 * @return the byte[]
	 */
	public static byte[] SHA1(final byte[] message, final int offset, final int length) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
			digest.update(message, offset, length);
			final byte[] sha1 = digest.digest();

			return sha1;
		} catch (final NoSuchAlgorithmException e){
if(Cfg.EXCEPTION){Check.log(e);}

			
		}
		return null;
	}

	/**
	 * SH a1.
	 * 
	 * @param message
	 *            the message
	 * @return the byte[]
	 */
	public static byte[] SHA1(final byte[] message) {
		return SHA1(message, 0, message.length);
	}
}
