package kiyobot.util.zip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZlibDecompressor {

	private static final Logger LOGGER = LogManager.getLogger();

	public ZlibDecompressor() {
	}

	/**
	 * Decompresses zlib-compressed bytes into the resulting String
	 * @param bytes - input bytes
	 * @return String
	 */
	public String decompress(byte[] bytes) {
		Inflater decompressor = new Inflater();
		decompressor.setInput(bytes);
		byte[] result = new byte[500];
		int resultLength;
		String decompressedString = "";
		try {
			do {
				resultLength = decompressor.inflate(result);
				if(!decompressor.finished()) {
					result = resizeDecompressedResult(result);
					decompressor = new Inflater();
					decompressor.setInput(bytes);
				}
			} while(!decompressor.finished());
			decompressor.end();
			decompressedString = new String(result, 0, resultLength, "UTF-8");
		} catch (DataFormatException dfe) {
			LOGGER.error("Error with the format of the data: {},\n{}", dfe.getMessage(), dfe.getStackTrace());
		} catch (UnsupportedEncodingException uee) {
			LOGGER.error("Error with encoding: {},\n{}", uee.getMessage(), uee.getStackTrace());
		}
		return decompressedString;
	}

	/**
	 * Resize the input byte[]
	 * @param bytes - input bytes
	 * @return resized byte[]
	 */
	private byte[] resizeDecompressedResult(byte[] bytes) {
		int size = bytes.length * 3/2;
		return new byte[size];
	}
}
