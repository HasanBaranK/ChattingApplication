package Common;

import Common.Configuration;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

/**
 * This encapsulates the process of setting up a multicast
 * communication endpoint, as well as sending and receiving
 * from that endpoint.
 */
public class MulticastEndpoint {
    public MulticastSocket mSocket_;
    Configuration c_;

    /**
     * Initial configuration for the socket and group
     * @param config Common.Configuration file
     * */
    public MulticastEndpoint(Configuration config) {
        InetAddress mGroup;
        c_ = config;

        try {
            mGroup = InetAddress.getByName(c_.mAddr_);
            mSocket_ = new MulticastSocket(c_.mPort_);

            mSocket_.setLoopbackMode(c_.loopbackOff_);
            mSocket_.setReuseAddress(c_.reuseAddr_);
            mSocket_.setTimeToLive(c_.ttl_);
            mSocket_.setSoTimeout(c_.soTimeout_); // non-blocking

            config.mGroup_ = mGroup;
        } catch (IOException e) {
            System.out.println("Common.MulticastEndpoint() problem: " + e.getMessage());
        }
    }
    /**
     * Joins to group
     * */
    public void join() {
        try {
            mSocket_.joinGroup(c_.mGroup_);
        } catch (IOException e) {
            System.out.println("join() problem: " + e.getMessage());
        }
    }
    /**
     * A method for receiving datagram packages
     * */
    public boolean rx(byte b[]) {
        boolean done;
        DatagramPacket d;

        done = false;
        d = new DatagramPacket(b, b.length);

        try {
            mSocket_.receive(d);
            done = true;
        } catch (SocketTimeoutException e) {
            // do nothing
        } catch (IOException e) {
            System.out.println("rx() problem: " + e.getMessage());
        }

        return done;
    }
    /**
     * A method for sending datagram packages
     * */
    public boolean tx(byte b[]) {
        boolean done;
        DatagramPacket d;

        done = false;
        try {
            d = new DatagramPacket(b, b.length, c_.mGroup_, c_.mPort_);
            mSocket_.send(d);
            done = true;
        } catch (SocketTimeoutException e) {
            System.out.println("tx() problem: could not send - " + e.getMessage());
        } catch (IOException e) {
            System.out.println("tx() problem: " + e.getMessage());
        }

        return done;
    }
}
