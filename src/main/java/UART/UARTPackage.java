/*
 * Copyright (C) 2017 Ushakov Danil
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * UARTPackage is part of WirelessSystemUARTServer.
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

package UART;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Jiro on 18.01.17.
 */
public class UARTPackage {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static int RX_STAGE_WAIT_FOR_BEGIN = 0;
    private static int RX_STAGE_READ_HEADER    = 1;
    private static int RX_STAGE_READ_BODY      = 2;
    private static char sRX_headerData[];
    private static int sRX_headerDataSize = 5;
    private static int sRX_bodyDataSize   = 0;
    private static char sRX_bodyData[];
    private static int sRX_isEscapeMode = 0;
    public int sourceAddress;
    public int destAddress;
    public char type = 0x00;
    public  byte[]            data;
    private static UARTMessageHeader header;

    public static UARTPackage getPackegeFromBytes(byte[] bytes) {

        int uart_state = RX_STAGE_WAIT_FOR_BEGIN;

        for (int i = 0; i < bytes.length; i++) {
            char value = (char) bytes[i];

            switch (uart_state) {
            case 0:
                if (value == 0xFF) {
                    uart_state = RX_STAGE_READ_HEADER;
                    sRX_headerDataSize = 0;
                    sRX_bodyDataSize = 0;
                    sRX_isEscapeMode = 0;
                }
                break;
            case 1:
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

                sRX_headerData[sRX_headerDataSize++] = value;

                if (sRX_headerDataSize >= sRX_headerDataSize) {
                    //TODO: дописать обработку хедеров
                    if (header.length > 0) {
                        uart_state = RX_STAGE_READ_BODY;
                        sRX_bodyDataSize = 0;
                    } else {
                        uart_state = RX_STAGE_WAIT_FOR_BEGIN;
                    }
                }

                break;
            case 2:
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

                sRX_bodyData[sRX_bodyDataSize++] = value;

                //TODO: дописать обработку хедеров
                // UartMsgHeader_t header;
                // memcpy(&header,sRX_headerData,sizeof(UartMsgHeader_t));

                if (sRX_bodyDataSize >= header.length) {
                    uart_state = RX_STAGE_WAIT_FOR_BEGIN;
                    sRX_headerDataSize = 0;
                    sRX_bodyDataSize = 0;
                    sRX_isEscapeMode = 0;
                }

                break;
            }
        }

        return null;
    }

    public class UARTMessageHeader {
        int length = 0;
        int from = 0;
        int to   = 0;

        public UARTMessageHeader() {}
    }
}