package in.masr.soundeffect.player;

import in.masr.soundeffect.AudioFormatNotSupportException;
import in.masr.soundeffect.FilteredSoundStream;
import in.masr.soundeffect.StreamUtils;
import in.masr.soundeffect.filter.EchoFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * The SimpleSoundPlayer encapsulates a sound that can be opened from the file
 * system and later played.
 */

public class SimpleSoundPlayer {

	public static void main(String[] args) {

		File file = new File(
				"/home/coolmore/Desktop/JavaSoundDemo/audio/1-welcome.wav");

		AudioInputStream stream = StreamUtils.getAudioStream(file);

		FilteredSoundStream filteredStream = new FilteredSoundStream(stream,
				new EchoFilter(0.3f, 0.5f, stream.getFormat()));

		SimpleSoundPlayer soundPlayer = new SimpleSoundPlayer(filteredStream,
				stream.getFormat());

		// play the sound
		soundPlayer.play();

		// exit
		System.exit(0);
	}

	private AudioFormat format;
	private InputStream stream;

	/**
	 * Receive a sound from stream.
	 * 
	 * @throws AudioFormatNotSupportException
	 */
	public SimpleSoundPlayer(InputStream stream, AudioFormat format) {
		this.stream = stream;
		this.format = format;
		try {
			if (!checkFormat(format)) {
				throw new AudioFormatNotSupportException();
			}

		} catch (AudioFormatNotSupportException ex) {
			System.err
					.println("Please make sure that the wav format is little-endian and 16 bit sampled.");
			ex.printStackTrace();
		}
	}

	public boolean checkFormat(AudioFormat format) {
		return !format.isBigEndian() && format.getSampleSizeInBits() == 16;
	}

	/**
	 * Plays a stream. This method blocks (doesn't return) until the sound is
	 * finished playing.
	 */
	public void play() {

		// use a short, 100ms (1/10th sec) buffer for real-time
		// change to the sound stream
		int bufferSize = format.getFrameSize()
				* Math.round(format.getSampleRate() / 10);
		byte[] buffer = new byte[bufferSize];

		// create a line to play to
		SourceDataLine line;
		try {
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format, bufferSize);
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
			return;
		}

		// start the line
		line.start();

		// copy data to the line
		try {
			int numBytesRead = 0;
			while (numBytesRead != -1) {
				numBytesRead = stream.read(buffer, 0, buffer.length);
				if (numBytesRead != -1) {
					line.write(buffer, 0, numBytesRead);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		// wait until all data is played, then close the line
		line.drain();
		line.close();

	}

}