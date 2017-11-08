package Net;

import UART.UARTPackage;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

import static UART.UARTGateProtocolEnum.PACKAGE_ROUTING_TABLE;

/**
 * Created by Jiro on 24.01.17.
 */
public class WirelessNode {

    private final SimpleIntegerProperty address;
    private final SimpleIntegerProperty dB;
    private final SimpleStringProperty state;

    private List<Route> RoutingTable;

    private int lifecicle = 4;

    public WirelessNode(int iAddress,int iDB, String iState){
        this.address = new SimpleIntegerProperty(iAddress);
        this.dB = new SimpleIntegerProperty(iDB);
        this.state = new SimpleStringProperty(iState);
    }

    public int getAddress(){
        return address.get();
    }
    public void setAddress(int iAddress){
        address.set(iAddress);
        increaselifeCicle();
    }

    public int getDB() {
        return dB.get();
    }
    public void setDB(int dB) {
        this.dB.set(dB);
        increaselifeCicle();
    }

    public String getState() {
        return state.get();
    }
    public void setState(String state) {
        this.state.set(state);
        increaselifeCicle();
    }

    public void updateRoutingTable (List<Route> table){
        RoutingTable = table;
        increaselifeCicle();
    }

    public List<Route>getRoutingTable(){return RoutingTable;}

    public boolean isAlive(){return (lifecicle>0)?true:false;}
    public void decreaseCicle(){lifecicle -= 2;}
    private void increaselifeCicle(){lifecicle = 6;}

    public static class Route {
        public int TargetAddress = -1;
        public int HopeAddress   = -1;

        public Route(int targetAddress, int hopeAddress){
            TargetAddress = targetAddress; HopeAddress = hopeAddress;
        }
    }
}