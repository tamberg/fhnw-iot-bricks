import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

class JsonTest {
	static final String MQTT_CONFIG_TTN_JSON =
        "{" +
            "\"mqtt-config\": {" +
	            "\"id\": \"ttn\"," +
	            "\"java-type\": \"ch.fhnw.imvs.bricks.MqttConfig\"," +
	            "\"mqtt-host\": \"eu.thethings.network\"," +
	            "\"mqtt-user\": \"app-id\"," +
	            "\"mqtt-pass\": \"app-access-key\"," +
	            "\"mqtt-sub\": \"fhnw-iot-bricks/devices/{id}/up\"," +
	            "\"mqtt-pub\": \"fhnw-iot-bricks/devices/{id}/down\"" +
	        "}" +
        "}";

	static final String BRICK_CONFIG_0000_0001_JSON =
        "{" +
            "\"brick-config\": {" +
	            "\"id\": \"0000-0001\"," +
    	        "\"java-type\": \"ch.fhnw.iot-bricks.HumiTempBrick\"," +
    	        "\"mqtt-config\": \"/mqtt-config/ttn.json\"" +
   	        "}" +
        "}";

    static final String BRICK_CONFIG_0000_0001_JSON_URL =
    	"http://tamberg-mac.try.yaler.io/brick-config/0000-0001.json";

    static final String MQTT_CONFIG_TTN_JSON_URL =
    	"http://tamberg-mac.try.yaler.io/mqtt-config/ttn.json";

	public static void main(String[] args) {
    	JsonObject brickConfigJson; // = Json.parse(BRICK_CONFIG_0000_0001_JSON).asObject();
    	try {
	    	InputStream in = new URL(BRICK_CONFIG_0000_0001_JSON_URL).openStream();
	  		// Files.copy(in, Paths.get(FILE_NAME), StandardCopyOption.REPLACE_EXISTING);    	
	    	InputStreamReader r = new InputStreamReader(in);
			brickConfigJson = Json.parse(r).asObject();
  		} catch (IOException e) {
  			System.exit(-1);
  			brickConfigJson = null;
  		}
    	System.out.println(brickConfigJson);
    	JsonObject brickConfig = brickConfigJson.get("brick-config").asObject();
    	String id = brickConfig.get("id").asString();
    	String javaType = brickConfig.get("java-type").asString();

    	JsonObject mqttConfigJson; // = Json.parse(MQTT_CONFIG_TTN_JSON).asObject();
    	try {
	    	InputStream in = new URL(MQTT_CONFIG_TTN_JSON_URL).openStream();
	  		// Files.copy(in, Paths.get(FILE_NAME), StandardCopyOption.REPLACE_EXISTING);    	
	    	InputStreamReader r = new InputStreamReader(in);
			mqttConfigJson = Json.parse(r).asObject();
  		} catch (IOException e) {
  			System.exit(-1);
  			mqttConfigJson = null;
  		}
    	System.out.println(mqttConfigJson);
    	JsonObject mqttConfig = mqttConfigJson.get("mqtt-config").asObject();
    	String mqttHost = mqttConfig.get("mqtt-host").asString();
    	String mqttUser = mqttConfig.get("mqtt-user").asString();
    	String mqttPass = mqttConfig.get("mqtt-pass").asString();
    	String mqttSub = mqttConfig.get("mqtt-sub").asString();
    	String mqttPub = mqttConfig.get("mqtt-pub").asString();

    	System.out.println(id);
    	System.out.println(javaType);
    	System.out.println(mqttHost);
    	System.out.println(mqttUser);
    	System.out.println(mqttPass);
    	System.out.println(mqttSub);
    	System.out.println(mqttPub);
	}
}