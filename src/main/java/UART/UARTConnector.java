package UART;

import FX.JFxUIMain;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Jiro on 23.01.17.
 */
public class UARTConnector {

    private static final Logger log = LoggerFactory.getLogger(JFxUIMain.class);

    private SerialPort serialPort;

    public UARTConnector(){

    }
    public void Init(String portName){
        serialPort = new SerialPort(portName);
    }

    public boolean Connect(){
        if (serialPort == null){
            return false;
        } else {
            try {
                serialPort.openPort();

                serialPort.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);


            } catch (SerialPortException e) {
                log.error("SerialPortException", e);
            }
        }
        return false;
    }

    private class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    byte[] data = serialPort.readBytes();
                    System.out.print("recived: ");
                    for (byte b : data) {
                        System.out.print(b);
                    }
                } catch (SerialPortException e) {
                    log.error("SerialPortException", e);
                }
            }
        }
    }
}
