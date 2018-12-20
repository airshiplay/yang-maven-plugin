package demo;

import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;
import com.tailf.jnc.JNCException;
import com.tailf.jnc.NodeSet;

import java.io.IOException;

public class NetconfClientTest {
    public static void main(String args[]) throws IOException, JNCException {
        Device device = new Device("admin", new DeviceUser("admin", "admin", "cErtusnEt@2018"), "172.19.200.203", 22);

        device.connect("admin", new ServerHostKeyVerifier() {
            @Override
            public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
                String hexFingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
                String bubblebabbleFingerprint = KnownHosts.createBubblebabbleFingerprint(serverHostKeyAlgorithm,
                        serverHostKey);
                return true;
            }
        },0);
        device.newSession("admin");
        NodeSet nodeSet = device.getSession("admin").get("/interfaces");
        System.out.println(nodeSet.toXMLString());
    }
}
