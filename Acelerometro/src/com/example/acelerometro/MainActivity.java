package com.example.acelerometro;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{
	TextView x,y,res;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		x=(TextView)findViewById(R.id.tvx);
		y=(TextView)findViewById(R.id.tvy);
		res=(TextView)findViewById(R.id.result);
		SensorManager sm=(SensorManager) getSystemService(SENSOR_SERVICE);
		List<Sensor>Sensors=sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(Sensors.size()>0){
			sm.registerListener(this, Sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float posX=event.values[0];
		float posY=event.values[1];
		x.setText("X="+posX);
		y.setText("Y="+posY);
		if(posX > 3){
			res.setText("d");
		}
		if(posX < -1){
			res.setText("u");
		}
		if(posY < -3){
			res.setText("l");
		}
		if(posY > 3){
			res.setText("r");
		}
		if(posX > -1 && posX < 4 && posY > -3 && posY < 3){
			res.setText("o");
		}
	}
}