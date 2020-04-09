package worldjam.time;

public class DelaySetting {
	private int measures = 1;
	public int getAdditionalDelayGlobal() {
		return additionalDelayMS_global;
	}
	public int getAdditionalDelayVisual() {
		return additionalDelayMS_visual;
	}
	public int getAdditionalDelayAudio() {
		return additionalDelayMS_audio;
	}
	private int additionalDelayMS_global;
	private int additionalDelayMS_visual;
	private int additionalDelayMS_audio;
	public int totalDelayGlobal(ClockSetting clockSetting){
		return additionalDelayMS_global + measures*clockSetting.getMsPerMeasure();		
	}
	public int totalDelayVisual(ClockSetting clockSetting){
		return additionalDelayMS_global + additionalDelayMS_visual + measures*clockSetting.getMsPerMeasure();		
	}
	public int totalDelayAudio(ClockSetting clockSetting){
		return additionalDelayMS_global + additionalDelayMS_audio + measures*clockSetting.getMsPerMeasure();	
	}
	
	public DelaySetting(int measures){
		this.measures = measures;
		additionalDelayMS_global = 0;//Configurations.getIntValue("calib.delays.global");
		additionalDelayMS_visual = 0;//Configurations.getIntValue("calib.delays.video");
		additionalDelayMS_audio = 0;//Configurations.getIntValue("calib.delays.audio");
	}
	
	public DelaySetting(int measures, int global){
		this.measures = measures;
		additionalDelayMS_global = global;
		additionalDelayMS_visual = 0;//Configurations.getIntValue("calib.delays.video");
		additionalDelayMS_audio = 0;//Configurations.getIntValue("calib.delays.audio");
	}
	
	public DelaySetting(int measures, int global, int audio, int visual){
		this.measures = measures;
		additionalDelayMS_global = global;
		additionalDelayMS_audio = audio;
		additionalDelayMS_visual = visual;
	}
	public static DelaySetting defaultDelaySetting = new DelaySetting(1);
	public int getMeasuresDelay() {
		// TODO Auto-generated method stub
		return measures;
	} 
	
	public String toString(){
		return "delay setting:\n Measures: "+measures 
				+"\nAdditionalDelay (general): " + additionalDelayMS_global
				+"\nAdditionalDelay for audio: " + additionalDelayMS_audio
				+"\nAdditionalDelay for video: " + additionalDelayMS_visual;
	}
	public DelaySetting createWithNewAudioDelay(int delay) {
		return new DelaySetting(measures, additionalDelayMS_global, delay, additionalDelayMS_visual);
	}
}
