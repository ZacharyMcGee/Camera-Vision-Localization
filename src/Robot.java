import com.fazecast.jSerialComm.SerialPort;

import arduino.Arduino;

public class Robot {
	static Arduino obj;

	public void CreateObj(String comport) {
		obj = new Arduino(comport, 9600);
	}
	
	public boolean ConnectBluetooth() {
		if(obj.openConnection()) {
			System.out.println("CONNECTED");
			return true;
		}
		else
		{
			System.out.println("DID NOT CONNECT");
			return false;
		}
	}
	
	public void Forward() {
		obj.serialWrite('F');
		System.out.println("HM");
	}
	
	public void Backward() {
		obj.serialWrite('S');
	}
	
	public void Left() {
		obj.serialWrite('L');
	}
	
	public void Right() {
		obj.serialWrite('R');
	}
	
	public void Stop() {
		obj.serialWrite('S');
	}
}
