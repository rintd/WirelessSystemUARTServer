package UART;

import FX.JFxUIMain;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static UART.UARTGateProtocolEnum.*;


/**
 * Created by Jiro on 23.01.17.
 */

public class UARTConnector {

    private static final Logger log = LoggerFactory.getLogger(JFxUIMain.class);
    private SerialPort serialPort;
    private UARTConnectorDelegate delegate;

    public boolean isDebug = false;

    public UARTConnector(){}

    public UARTConnector(UARTConnectorDelegate iDelegate){
        delegate = iDelegate;
    }

    public void setDelegate(UARTConnectorDelegate iDelegate) {delegate = iDelegate;}

    public void Init(String portName){
        serialPort = new SerialPort(portName);
    }

    public boolean IsConnected(){
        if (serialPort == null)
            return false;
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

    public void SetNodeNewAddress(int address) {

        byte[] aByte = ByteBuffer.allocate(4).putInt(address).array();

        byte[] bytes = {(byte) 0xff, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x80, (byte) 0x17, (byte) aByte[3], (byte) aByte[2]};

        this.sendBytes(bytes);
    }

    public void GetNodeStatistic(int address){
        byte[] aByte = ByteBuffer.allocate(4).putInt(address).array();

        byte[] bytes = {(byte)0xff, (byte)1 ,(byte)0x00, (byte)0x00, aByte[3], aByte[2], (byte)0x0B,(byte)0x00};
        this.sendBytes(bytes);
    }


    public void SetArrowDisable(int address){
        byte[] aByte = ByteBuffer.allocate(4).putInt(address).array();

        byte[] bytes = {(byte)0xff, (byte)2 ,(byte)0x00, (byte)0x00, aByte[3], aByte[2], (byte)51,(byte)0x00};
        this.sendBytes(bytes);
    }

    public void SetArrowRight(int address){
        byte[] aByte = ByteBuffer.allocate(4).putInt(address).array();

        byte[] bytes = {(byte)0xff, (byte)2 ,(byte)0x00, (byte)0x00, aByte[3], aByte[2], (byte)51,(byte)0x01};
        this.sendBytes(bytes);
    }

    public void SetArrowLeft(int address){
        byte[] aByte = ByteBuffer.allocate(4).putInt(address).array();

        byte[] bytes = {(byte)0xff, (byte)2 ,(byte)0x00, (byte)0x00, aByte[3], aByte[2], (byte)51,(byte)0x02};
        this.sendBytes(bytes);
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

        System.out.println(data.length);

        for (int i = 0; i < data.length; i++) {
            byte value = data[i];

            switch (uart_state) {
                case RX_STAGE_WAIT_FOR_BEGIN: {
                    if (value == (byte)0xFF) {
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
                        if (value == (byte) 0xFE)//Escape byte
                        {
                            sRX_isEscapeMode = 1;
                            break;
                        } else if (value == (byte) 0xFF)//New message
                        {
                            uartPackage = new UARTPackage();
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

                    if (sRX_headerDataSize == 5) {
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
                        if (value == (byte) 0xFE)//Escape byte
                        {
                            sRX_isEscapeMode = 1;
                            break;
                        } else if (value == (byte) 0xFF)//New message
                        {
                            uartPackage = new UARTPackage();
                            uart_state = RX_STAGE_READ_HEADER;
                            sRX_headerDataSize = 0;
                            sRX_bodyDataSize = 0;
                            sRX_isEscapeMode = 0;
                            break;
                        }
                    }

                    if(sRX_bodyDataSize == 0) {
                        uartPackage.type = value & 0xFF;
                        uartPackage.data = new byte[uartPackage.length - 1];
                    } else {
                        uartPackage.data[sRX_bodyDataSize - 1] = value;
                    }

                    sRX_bodyDataSize++;

                    if (sRX_bodyDataSize == uartPackage.length) {
                        uart_state = RX_STAGE_WAIT_FOR_BEGIN;
                        sRX_headerDataSize = 0;
                        sRX_bodyDataSize = 0;
                        sRX_isEscapeMode = 0;

                        if (delegate != null) {
                            delegate.OnConnectionDidRecivePackege(uartPackage);
                        }
                    }
                } break;
            }
        }
    }

    private void sendBytes(byte[]bytes){
        try {
            //TODO: лог отправленного пакета
            serialPort.writeBytes(bytes);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }


    private class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    byte[] c = serialPort.readBytes();
                    byte[] iBytes = Arrays.copyOf(c,c.length);

                    if (UARTConnector.this.isDebug && delegate != null){

                        String s = "";
                        for (byte b : iBytes){
                            s.concat(String.format("%02x",b & 0xFF));
                        }

                        delegate.OnDebugMessageRecived(s);
                    }

                    for (byte b : iBytes){
                        System.out.printf("%02x",b & 0xFF);
                    }
                    System.out.println();

                    if(iBytes.length>0){
                        UARTConnector.this.processUARTData(iBytes);
                    }

                } catch (SerialPortException e) {
                    log.error("SerialPortException", e);
                }
            }
        }
    }
}
