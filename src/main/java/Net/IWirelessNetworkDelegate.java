package Net;

/**
 * Created by jiro on 01.08.17.
 */
public interface IWirelessNetworkDelegate {
    void onNetworkConnected();
    void onNetworkDisconnected();

    void onNetworkUpdated();
    void needSetNodeAddress(WirelessNode node);
}
