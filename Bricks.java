import java.util.Date;
import java.lang.String;

class Bricks {

    public static final class Backend {
        public Backend() {}
        public void setHost(String host) {}
        public void setUser(String user) {}
        public void setPassword(String password) {}
        public Brick getBrick(String token) {
            Brick result;
            if ("TOKEN_PRINTED_ON_TEMP_BRICK".equals(token)) {
                result = new TemperatureBrick();
            } else if ("TOKEN_PRINTED_ON_DIPLAY_BRICK".equals(token)) {
                result = new DisplayBrick();
            } else {
                result = null;
            }
            return result;
        }
    }

    public abstract class Brick {
        public int getBatteryLevel() { return 100; }
    }

    public final class TemperatureBrick extends Brick {
        private TemperatureBrick() {}
        public double getValue() { return 0; }
        public Date getTimestamp() { return new Date(); }
    }

    public final class DisplayBrick extends Brick {
        private DisplayBrick() {}
        public void setValue(double Value) {}
    }

    public static void main() {
        Backend backend = new Backend();
        backend.setHost("FHNW_IOT_BRICKS_HOST");
        backend.setUser("FHNW_IOT_BRICKS_USER");
        backend.setPassword("FHNW_IOT_BRICKS_PASSWORD");

        TemperatureBrick tempBrick = (TemperatureBrick) backend.getBrick("TOKEN_PRINTED_ON_TEMP_BRICK");
        DisplayBrick displayBrick = (DisplayBrick) backend.getBrick("TOKEN_PRINTED_ON_DIPLAY_BRICK");

        while (Math.min(tempBrick.getBatteryLevel(), displayBrick.getBatteryLevel()) > 20) {
            double temp = tempBrick.getValue();
            displayBrick.setValue(temp);
        }
    }
}
