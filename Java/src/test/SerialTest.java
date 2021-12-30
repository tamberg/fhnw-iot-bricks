import java.io.DataInputStream;
import java.io.DataOutputStream;
import gnu.io.NRSerialPort;

class SerialTest {
    public static void main(String[] args) {
        String port = "";
        for (String s : NRSerialPort.getAvailableSerialPorts()){
            System.out.println("Availible port: " + s);
            port = s;
        }

        int baudRate = 115200;
        NRSerialPort serial = new NRSerialPort(port, baudRate);
        serial.connect();

        DataInputStream ins = new DataInputStream(serial.getInputStream());
        DataOutputStream outs = new DataOutputStream(serial.getOutputStream());
        try {
	        //while (ins.available() == 0 && !Thread.interrupted()); // wait for a byte
	        while (!Thread.interrupted()) { // read all bytes
		        if (ins.available() > 0) {
			        int b = ins.read();
			        //outs.write((byte) b);
			        System.out.print((char) b);
                }
                Thread.sleep(5);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        serial.disconnect();
    }
}
