package Net;

import UART.UARTConnectorDelegate;
import UART.UARTPackage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

import static UART.UARTGateProtocolEnum.*;

/**
 * Created by jiro on 01.08.17.
 */

public class WirelessNetwork implements UARTConnectorDelegate {

    private final ObservableList<WirelessNode> fxNodes = FXCollections.observableArrayList();
    public ObservableList<WirelessNode> getNodes(){return  fxNodes;}
    private IWirelessNetworkDelegate delegate;
    public void setDelegate(IWirelessNetworkDelegate iDelegate){delegate = iDelegate;}

    public WirelessNetwork(){
        WirelessNode severNode = new WirelessNode(2,-1,"Server");
        fxNodes.add(severNode);
    }

    public void dropNet(){
        List<WirelessNode> list = new ArrayList<>();
        for (WirelessNode node : fxNodes){
            if(!node.getState().equals("Server")) node.decreaseCicle();
            if(!node.isAlive()){
                list.add(node);
            }
        }

        for (WirelessNode node : list){
            fxNodes.remove(node);
        }
    }


    public void calculateLayers(){
        for (WirelessNode node : fxNodes) {
            if(node.getState().equals("Server")){
                
            } else {

            }
        }

    }

    public void updateNodesWithPackege(UARTPackage uartPackage){
        WirelessNode findedNode = null;
        for (WirelessNode node : fxNodes){
            if (node.getAddress() == uartPackage.sourceAddress){
                findedNode = node;
                break;
            }
        }

        if (findedNode == null){
            String type = (uartPackage.sourceAddress< 0x8000) ? "Router" : "Node";
            fxNodes.add(new WirelessNode(uartPackage.sourceAddress, 0, type));
            findedNode = fxNodes.get(fxNodes.size() - 1);
        }

        if (uartPackage.type == PACKAGE_NODE_REQUIRED_INIT) {
            if (delegate != null){
                delegate.needSetNodeAddress(findedNode);
            }
        } else if (uartPackage.type == PACKAGE_ECHO_STATE) {

        } else if(uartPackage.type == PACKAGE_ALARM){

        } else if(uartPackage.type == PACKAGE_ROUTING_TABLE){
            List<WirelessNode.Route> table = new ArrayList<WirelessNode.Route>();
            for (int i = 0; i<5; i++ ) {
                int targetAddress = 0;
                targetAddress += uartPackage.data[i * 4] & 0xff;
                targetAddress += uartPackage.data[i * 4 + 1] & 0xff;

                int h = 0;
                h += uartPackage.data[i * 4 + 2] & 0xff;
                h += uartPackage.data[i * 4 + 3] & 0xff;
                table.add(new WirelessNode.Route(targetAddress, h));
            }
            findedNode.updateRoutingTable(table);
        }

        if (delegate != null){
            delegate.onNetworkUpdated();
        }
    }

    private void addTestNodes(){
        fxNodes.add(new WirelessNode(66,-1,"Router"));
        List<WirelessNode.Route> table = new ArrayList<WirelessNode.Route>();
        table.add(new WirelessNode.Route(2,2));
        table.add(new WirelessNode.Route(77,77));
        table.add(new WirelessNode.Route(88,88));
        fxNodes.get(fxNodes.size() - 1).updateRoutingTable(table);

        fxNodes.add(new WirelessNode(77,-1,"Node"));
        table = new ArrayList<WirelessNode.Route>();
        table.add(new WirelessNode.Route(2,66));
        fxNodes.get(fxNodes.size() - 1).updateRoutingTable(table);

        fxNodes.add(new WirelessNode(88,-1,"Router"));
        table = new ArrayList<WirelessNode.Route>();
        table.add(new WirelessNode.Route(2,66));
        fxNodes.get(fxNodes.size() - 1).updateRoutingTable(table);
    }


    @Override
    public void OnConnectionClosed() {
        if(delegate != null){
            delegate.onNetworkDisconnected();
        }
    }

    @Override
    public void OnConnectionOpened() {
        if(delegate != null){
            delegate.onNetworkConnected();
        }
    }

    @Override
    public void OnConnectionDidRecivePackege(UARTPackage uartPackage) {
        updateNodesWithPackege(uartPackage);
    }

    @Override
    public void OnDebugMessageRecived(String message) {
        //TODO:
    }
}
