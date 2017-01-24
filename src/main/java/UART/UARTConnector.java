package UART;

import FX.JFxUIMain;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Jiro on 23.01.17.
 */

public class UARTConnector {

    private static final Logger log = LoggerFactory.getLogger(JFxUIMain.class);
    private SerialPort serialPort;
    private UARTConnectorDelegate delegate;

    private byte[] bytesBuffer;

    public UARTConnector(){
        bytesBuffer = new byte[0];
    }

    public UARTConnector(UARTConnectorDelegate iDelegate){
        bytesBuffer = new byte[0];
        delegate = iDelegate;
    }

    public void setDelegate(UARTConnectorDelegate iDelegate) {delegate = iDelegate;}

    public void Init(String portName){
        serialPort = new SerialPort(portName);
    }

    public boolean IsConnected(){
        return  serialPort.isOpened();
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

                return true;
            } catch (SerialPortException e) {
                log.error("SerialPortException", e);
                return false;
            }
        }
    }

    public boolean Disconnect (){
        if(serialPort.isOpened()){
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static final int RX_STAGE_WAIT_FOR_BEGIN = 0;
    private static final int RX_STAGE_READ_HEADER    = 1;
    private static final int RX_STAGE_READ_BODY      = 2;

    private static int sRX_headerDataSize = 5;
    private static int sRX_bodyDataSize   = 0;
    private static int sRX_isEscapeMode = 0;

    private int uart_state = RX_STAGE_WAIT_FOR_BEGIN;

    private UARTPackage uartPackage = null;
    private void processUARTData(byte[] data){
        //TODO:сделать обработку пакетов всех байт необработанных

        byte [] tData = Arrays.copyOf(data,data.length);

        for (int i = 0; i < tData.length; i++) {
            byte value = tData[i];

            switch (uart_state) {
                case RX_STAGE_WAIT_FOR_BEGIN: {
                    if (value == 0xFF) {
                        uartPackage = new UARTPackage();

                        uart_state = RX_STAGE_READ_HEADER;
                        sRX_headerDataSize = 0;
                        sRX_bodyDataSize = 0;
                        sRX_isEscapeMode = 0;
                    }
                } break;
                case RX_STAGE_READ_HEADER: {
                    if (sRX_isEscapeMode == 1) {
                        sRX_isEscapeMode = 0;
                    } else {
                        if (value == (char) 0xFE)//Escape byte
                        {
                            sRX_isEscapeMode = 1;
                            break;
                        } else if (value == (char) 0xFF)//New message
                        {
                            uart_state = RX_STAGE_READ_HEADER;
                            sRX_headerDataSize = 0;
                            sRX_bodyDataSize = 0;
                            sRX_isEscapeMode = 0;
                            break;
                        }
                    }

                    if(sRX_headerDataSize == 0){
                        uartPackage.length = 0xFF & value;
                    } else if(sRX_headerDataSize == 1) {
                        uartPackage.sourceAddress += (value & 0xFF);
                    } else if(sRX_headerDataSize == 2) {
                        uartPackage.sourceAddress += (value << 8 & 0xFF00);
                    } else if (sRX_headerDataSize == 3){
                        uartPackage.destAddress += (value & 0xFF);
                    } else if (sRX_headerDataSize == 4) {
                        uartPackage.destAddress += (value << 8 & 0xFF00);
                    }

                    sRX_headerDataSize++;

                    if (sRX_headerDataSize == sRX_headerDataSize) {
                        if (uartPackage.length > 0) {
                            uart_state = RX_STAGE_READ_BODY;
                            sRX_bodyDataSize = 0;
                        } else {
                            uart_state = RX_STAGE_WAIT_FOR_BEGIN;
                        }
                    }
                } break;
                case RX_STAGE_READ_BODY: {
                    if (sRX_isEscapeMode == 1) {
                        sRX_isEscapeMode = 0;
                    } else {
                        if (value == 0xFE)//Escape byte
                        {
                            sRX_isEscapeMode = 1;
                            break;
                        } else if (value == 0xFF)//New message
                        {
                            uart_state = RX_STAGE_READ_HEADER;
                            sRX_headerDataSize = 0;
                            sRX_bodyDataSize = 0;
                            sRX_isEscapeMode = 0;
                            break;
                        }
                    }

                    if(sRX_bodyDataSize == 0) {
                        uartPackage.type = value & 0xFF;
                        uartPackage.data = new byte[uartPackage.length - 6];
                    } else {
                        uartPackage.data[sRX_bodyDataSize - 1] = value;
                        sRX_bodyDataSize++;
                    }

                    if (sRX_bodyDataSize == uartPackage.length) {
                        uart_state = RX_STAGE_WAIT_FOR_BEGIN;
                        sRX_headerDataSize = 0;
                        sRX_bodyDataSize = 0;
                        sRX_isEscapeMode = 0;

                        if (delegate != null) {
                            delegate.OnConnectionDidRecivePackege(uartPackage);
                        }

                        data = Arrays.copyOfRange(data,i,data.length);
                    }
                } break;
            }
        }

        data = new byte[0];
    }


    private class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    byte[] iBytes = serialPort.readBytes();
                    byte[]tBuffer = new byte[UARTConnector.this.bytesBuffer.length + iBytes.length];

                    System.arraycopy(UARTConnector.this.bytesBuffer,0,tBuffer,0,UARTConnector.this.bytesBuffer.length);
                    System.arraycopy(iBytes,0,tBuffer,UARTConnector.this.bytesBuffer.length,iBytes.length);

                    UARTConnector.this.bytesBuffer = iBytes;

                    UARTConnector.this.processUARTData(UARTConnector.this.bytesBuffer);
                } catch (SerialPortException e) {
                    log.error("SerialPortException", e);
                }
            }
        }
    }
}
