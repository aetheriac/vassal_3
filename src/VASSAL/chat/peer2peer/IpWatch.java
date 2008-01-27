package VASSAL.chat.peer2peer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpWatch implements Runnable {
    private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private String currentIp;
    private long wait = 1000;

    public IpWatch(long waitInterval) {
        wait = waitInterval;
        currentIp = findIp();
    }

    public IpWatch() {
        this(1000);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(l);
    }

    public void run() {
        while (true) {
            String newIp = findIp();
            propSupport.firePropertyChange("address", currentIp, newIp); //$NON-NLS-1$
            currentIp = newIp;

            try {
                Thread.sleep(wait);
            }
            catch (InterruptedException ex) {
            }
        }
    }

    public String getCurrentIp() {
        return currentIp;
    }

    private String findIp() {
        try {
            InetAddress a[] = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            final StringBuilder buff = new StringBuilder();
            for (int i = 0; i < a.length; ++i) {
                buff.append(a[i].getHostAddress());
                if (i < a.length - 1) {
                    buff.append(","); //$NON-NLS-1$
                }
            }
            return buff.toString();
        }
        catch (UnknownHostException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        IpWatch w = new IpWatch();
        w.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("Address = " + evt.getNewValue()); //$NON-NLS-1$
            }
        });
        System.out.println("Address = " + w.getCurrentIp()); //$NON-NLS-1$
        new Thread(w).start();
    }
}
