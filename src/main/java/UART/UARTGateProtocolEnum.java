/*
 * Copyright (C) 2017 Ushakov Danil
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * UARTGateProtocolEnum is part of WirelessSystemUARTServer.
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

/**
 * Created by Jiro on 18.01.17.
 */
public class UARTGateProtocolEnum {
    public static int PACKAGE_ERROR      = 0x00;//00; //           just empty;
    public static int PACKAGE_ECHO       = 0x0A;//10; //           just return [ADRESS,1];
    public static int PACKAGE_ECHO_STATE = 0x0B;//11; //           just return [ADRESS,BATTERY,TEMP];

    public static int PACKAGE_GET_NODE_CONF     = 0x14;//20; //           just return;
    public static int PACKAGE_INIT_NODE_CONF    = 0x15;//21; //           just update NODE [ADDRESS,ect., ...];
    public static int PACKAGE_UPDATE_NODE_CONF  = 0x16;//22; //           just update NODE [ADDRESS,ect., ...];
    public static int PACKAGE_SET_NODE_ADDRESS  = 0x17;//23; //
    public static int PACKAGE_NEED_SET_ADDRESSS = 0x18;//24; //

    public static int PACKAGE_GET_ARROW_STATE_COUNT = 50;
    public static int PACKAGE_SET_ARROW_STATE       = 51;

    public static int PACKAGE_ROUTING_TABLE         = 0xEA;
    public static int PACKAGE_NODE_REQUIRED_INIT    = 0xEE;

    public static int PACKAGE_ALARM                = 100;
}
