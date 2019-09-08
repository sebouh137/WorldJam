package worldjam.time;

public class DelaySetting {
	public int measures = 1;
	public int additionalDelayMS_global;
	public int additionalDelayMS_visual;
	public int additionalDelayMS_audio;
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
		additionalDelayMS_global = 0;
		additionalDelayMS_visual = 0;
		additionalDelayMS_audio = 0;
	}
	
	public DelaySetting(int measures, int global){
		this.measures = measures;
		additionalDelayMS_global = global;
		additionalDelayMS_visual = 0;
		additionalDelayMS_audio = 0;
	}
	
	public DelaySetting(int measures, int global, int audio, int visual){
		this.measures = measures;
		additionalDelayMS_global = global;
		additionalDelayMS_audio = audio;
		additionalDelayMS_visual = visual;
	}
	public static DelaySetting defaultDelaySetting = new DelaySetting(1); 
}
