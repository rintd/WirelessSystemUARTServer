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

    //header
    public int length = 0;
    public int sourceAddress;
    public int destAddress;

    public int type = 0x00;
    public byte[] data;

}