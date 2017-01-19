/*
 * Copyright (C) 2017 Ushakov Danil
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * SystemMain is part of WirelessSystemUARTServer.
 *
 * WirelessSystemUARTServer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WirelessSystemUARTServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with WirelessSystemUARTServer. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 */

import jssc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jiro on 18.01.17.
 */
public class SystemMain {

    private static final Logger log = LoggerFactory.getLogger(SystemMain.class);

    private static SerialPort serialPort;

    public static void main(String... args) {

        String[] portNames = SerialPortList.getPortNames();

        if (portNames.length > 0) {
            serialPort = new SerialPort(portNames[0]);
            try {
                serialPort.openPort();

                serialPort.setParams(SerialPort.BAUDRATE_57600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                serialPort.addEventListener(new PortReader(), SerialPort.MASK_RXCHAR);

                // serialPort.writeString("Get data");
                final byte[] bytes = { (byte) 0xff, (byte) 1, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x00,
                        (byte) 0x0B, (byte) 0x00 };

                Timer u = new Timer();
                u.schedule(new TimerTask() {
                    @Override public void run() {
                        try {
                            // System.out.print("writed: ");
                            // for (byte b: bytes)
                            // System.out.print(b+":");
                            // System.out.println();
                            serialPort.writeBytes(bytes);
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                            log.error("SerialPortException in Timer#schedule", e);
                        }
                    }
                }, 0, 1000);
            } catch (SerialPortException e) {
                log.error("SerialPortException", e);
            }
        }
    }

    private static class PortReader implements SerialPortEventListener {

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