import java.io.Serializable;

public class DataPacket implements Serializable {

    private double fingerCurl;

    public DataPacket(double fingerCurl) {
        this.fingerCurl = fingerCurl;
    }

    public String serialize() {
        return "" + Math.round(fingerCurl) + "\n";
    }

    public void setFingerCurl(float fingerCurl) {
        this.fingerCurl = fingerCurl;
    }

    public double getFingerCurl() {
        return fingerCurl;
    }
}
