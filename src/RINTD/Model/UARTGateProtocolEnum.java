package RINTD.Model;

/**
 * Created by Jiro on 18.01.17.
 */
public class UARTGateProtocolEnum {
    public static char PACKAGE_ERROR                 =   0x00;//00; //           just empty;
    public static char PACKAGE_ECHO                  =   0x0A;//10; //           just return [ADRESS,1];
    public static char PACKAGE_ECHO_STATE            =   0x0B;//11; //           just return [ADRESS,BATTERY,TEMP];

    public static char PACKAGE_GET_NODE_CONF         =   0x14;//20; //           just return;
    public static char PACKAGE_INIT_NODE_CONF        =   0x15;//21; //           just update NODE [ADDRESS,ect., ...];
    public static char PACKAGE_UPDATE_NODE_CONF      =   0x16;//22; //           just update NODE [ADDRESS,ect., ...];
    public static char PACKAGE_SET_NODE_ADDRESS      =   0x17;//23; //
    public static char PACKAGE_NEED_SET_ADDRESSS     =   0x18;//24; //

    public static char PACKAGE_GET_ARROW_STATE_COUNT =   50;
    public static char PACKAGE_SET_ARROW_STATE       =   51;
}
