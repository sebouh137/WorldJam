package worldjam.time;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class MutableClock {
	private ArrayList<ClockSubscriber> subscribers = new ArrayList();
	public void changeSettingsNow(ClockSetting settings){
		for(ClockSubscriber subscriber : subscribers){
			subscriber.changeClockSettingsNow(settings);
		}
	}
	public void addChangeSubscriber(ClockSubscriber subs){
		this.subscribers.add(subs);
	}
	ClockSetting settings;
	public MutableClock(ClockSetting settings){
		this.settings = settings;
	}
	public ClockSetting getSetting(){
		return settings;
	}
	public void adjustTempo(int msPerBeat) {
		this.changeSettingsNow(settings.createWithDifferentTempo(msPerBeat));
	}
	public void adjustTimeSignature(int num, int denom){
		this.changeSettingsNow(settings.createWithDifferentBeatCount(num));
	}
	List<ClockSettingChange> clockSettingChanges = new ArrayList();
}
