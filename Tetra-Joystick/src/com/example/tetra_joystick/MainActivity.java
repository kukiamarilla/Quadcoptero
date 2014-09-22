package com.example.tetra_joystick;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	private static final String TAG = "ToggleLed";
	private static final int ENABLE_BLUETOOTH = 1;
	
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>();
	private BluetoothSocket clientSocket;
	private BroadcastReceiver discoveryMonitor;
	
	private ArrayAdapter<String> mArrayAdapter;
	private Button buttonToggle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// TODO: Get Bluetooth Adapter.

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// TODO: Check smatphone support Bluetooth
		if(mBluetoothAdapter == null){
			// Device does not support Bluetooth.	
			Toast.makeText(getApplicationContext(), "Not support bluetooth", 1).show();
			Log.d(TAG, "Not support Bluetooth");
			finish();
		}			
		// Componentes de la interface grafica.
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		setListAdapter(mArrayAdapter);
	}
			
	@Override
	protected void onResume() {
		super.onResume();
		
		// TODO:En caso de que el bluetooth no este activo, hacemos un Intent al sistema
		// para que nos lleve a la pantalla de activacion. Al volver de esta pantalla
		// ejecutara el metodo onActivityResult.
		
		if(!mBluetoothAdapter.isEnabled()){
			Log.d(TAG, "Bluetooth apagado: Pedimos permiso para encenderlo.");	
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLUETOOTH);
	    }else{
	    	Log.d(TAG, "Bluetooth encendido.");	
	    	dicoveryBluetooth();	 
	    }
		// TODO: Registramos el BroadcasReceiver
		
		if(this.discoveryMonitor !=null){
			registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
			registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
			registerReceiver(discoveryMonitor, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			
		if (requestCode == ENABLE_BLUETOOTH){
			if (resultCode == RESULT_OK){
				Log.d(TAG, "Bluetooth: el usuario acepta encenderlo");
				// Ejecutamos el metodo dicoveryBluetooth
				dicoveryBluetooth();
			}else{
				Log.d(TAG, "Bluetooth: el usuario NO acepta encenderlo");
			}
		}
	}

	// Metodo para buscar dispositibos bluetooth cercanos.
	// Este metodo es asincrono y devuelve los resultados mediante
	// un BroadcastReceiver.
	private void dicoveryBluetooth() {	
		
		// Limpiamos la lista de dispositivos detectados.
		mArrayAdapter.clear();
		btDeviceList.clear();
		
		// Aqui implementamos el BrodcastReceiver
		this.discoveryMonitor = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
			
				// TODO: Acciones, al iniciar el dicovery, finalizar y cuando encuentra un dispositivo
				
				if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
				    Log.d(TAG, "Discovery started...");
				}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
				    Log.d(TAG, "Discovery complete.");
				}else if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){

				    // Añadimos el dispositivo encontrado al adaptador del ListView.
				    String remoteName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				    mArrayAdapter.add(remoteName);
				    Log.d(TAG, "Dispositivo detectado :" + remoteName);

				    // Recuperamos el dispositivo detectado y lo guardamos en el array de dispositivos.
				    BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				    btDeviceList.add(remoteDevice);
				}
			}		
		};
		
		// TODO: Iniciamos la busqueda de dispositvos bluetooth.
		// Este metodo es muy lento y consumo mucha bateria
		// en otros capitulos veremos como usar otra tecnica.

		mBluetoothAdapter.startDiscovery();
	}
		
	
	@Override
	protected void onPause() {
		
		if (this.discoveryMonitor != null){
			unregisterReceiver(discoveryMonitor);
		}
		super.onPause();
	}
	
	private boolean connectRemoteDevice(BluetoothDevice device){
		Log.d(TAG, "Connectando");
		boolean connect = false;
		try {
			// TODO: Conexion socket cliente.
			// String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
			try {
			    String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
			    this.clientSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
			    clientSocket.connect();
			    SensorManager sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
				List<Sensor> sensors=sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
				if(sensors.size()>0){
					Sensor acelerometro=sensors.get(0);
					sensorManager.registerListener(SensorListener, acelerometro, SensorManager.SENSOR_DELAY_GAME);
				}
			    connect = true;
			} catch (Exception e) {
			    Log.d(TAG,e.getMessage());
			    connect = false;
			}

			return connect;		
		} catch (Exception e) {
			Log.d(TAG,e.getMessage());
			connect = false;
		}
		
		return connect;
	}
	
	
//	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		BluetoothDevice device = btDeviceList.get(position);
		Log.d(TAG, "Dispositivo seleccionado: "  + device.getName());
		
		// Intentamos conectar con el dispositivo remoto.
		if(connectRemoteDevice(device)){
			buttonToggle.setEnabled(true);
		}
		
	}

	
	
	private final SensorEventListener SensorListener=new SensorEventListener(){

		@Override
		public void onSensorChanged(SensorEvent event) {
			float posX=event.values[0];
			float posY=event.values[1];
			// TODO: Enviando informacion del Movil hacia el Arduino.
			
			OutputStream mmOutStream = null;
			try {
			
				if (clientSocket.isConnected()){
			        mmOutStream = clientSocket.getOutputStream();
			        try{
				        mmOutStream.write(new String("L").getBytes("UTF-8"));
			        }catch(UnsupportedEncodingException e){
			        	Log.d(TAG, "Error de codificación");
			        }
			    }else{
			        Toast.makeText(getApplicationContext(), "Not connected",0).show();
			    }	
			} catch (IOException e) {
				Log.d(TAG,e.getMessage());
				buttonToggle.setEnabled(false);
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};
}; 