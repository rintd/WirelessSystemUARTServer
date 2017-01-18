/**
 * Created by Jiro on 18.01.17.
 */

import jssc.*;

import java.util.Timer;
import java.util.TimerTask;

public class SystemMain {

    private static SerialPort serialPort;

    public static void main(String... args) {

        String[] portNames = SerialPortList.getPortNames();

        if(portNames.length>0){
            serialPort = new SerialPort(portNames[0]);
            try {
                serialPort.openPort();

                serialPort.setParams(SerialPort.BAUDRATE_57600,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

//                serialPort.writeString("Get data");
                byte[] bytes = {(byte)0xff, (byte)1 ,(byte)0x00, (byte)0x00, (byte)0x16, (byte)0x00, (byte)0x0B,(byte)0x00};

                Timer u = new Timer();
                u.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
//                            System.out.print("writed: ");
//                            for (byte b: bytes)
//                                System.out.print(b+":");
//
//                            System.out.println();
                            serialPort.writeBytes(bytes);
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                    }
                },0,1000);
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        }
    }

    private static class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    byte[] data = serialPort.readBytes();
                    System.out.print("recived: ");
                    for (byte b : data){
                        System.out.print(b);
                    }
                }
                catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
