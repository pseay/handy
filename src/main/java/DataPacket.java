import java.io.Serializable;

public class DataPacket implements Serializable {

    private final Double[] fingerCurls = new Double[5];

    public DataPacket(Double[] fingerCurls) {
        System.arraycopy(fingerCurls, 0, this.fingerCurls, 0, 5);
    }

    public String serialize() {
        StringBuilder b = new StringBuilder();
        for (Double fingerCurl : fingerCurls) {
            b.append(Math.round(fingerCurl));
            b.append(',');
        }
        return b + "\n";
    }
}
