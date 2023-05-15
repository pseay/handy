import java.io.Serializable;

public class DataPacket implements Serializable {

    private final double[] fingerCurls = new double[5];

    public DataPacket(double fingerCurl) {
        this.fingerCurls[0] = 0;
        this.fingerCurls[1] = fingerCurl;
        this.fingerCurls[2] = 0;
        this.fingerCurls[3] = 0;
        this.fingerCurls[4] = 0;
    }

    public String serialize() {
        StringBuilder b = new StringBuilder();
        for (double fingerCurl : fingerCurls) {
            b.append(Math.round(fingerCurl));
            b.append(',');
        }
        return b + "\n";
    }
}
