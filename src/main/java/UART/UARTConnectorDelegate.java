package UART;

/**
 * Created by Jiro on 23.01.17.
 */
public interface UARTConnectorDelegate {
    public void OnConnectionClosed();
    public void OnConnectionOpened();
    public void OnConnectionDidRecivePackege(UARTPackage packege);

    public void OnDebugMessageRecived(String message);
}
