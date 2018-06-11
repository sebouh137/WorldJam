package worldjam.audio;

import javax.sound.sampled.AudioFormat;

public abstract class AudioFilter{

	protected abstract byte[] process(byte[] sampleData, AudioFormat format);

}
