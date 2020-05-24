import java.nio.ByteBuffer;
import java.util.Base64;

class Test {
    static String mqttPayload = "{\"app_id\":\"fhnw-iot\",\"dev_id\":\"fhnw-iot-0\",\"hardware_serial\":\"003B6B567F66163D\",\"port\":1,\"counter\":2647,\"payload_raw\":\"EagIjg==\",\"payload_fields\":{\"h\":45.2,\"t\":21.9},\"metadata\":{\"time\":\"2020-04-27T16:53:25.764642126Z\",\"frequency\":868.5,\"modulation\":\"LORA\",\"data_rate\":\"SF7BW125\",\"airtime\":51456000,\"coding_rate\":\"4/5\",\"gateways\":[{\"gtw_id\":\"eui-fcc23dfffe0b75e6\",\"timestamp\":3316516179,\"time\":\"\",\"channel\":2,\"rssi\":-110,\"snr\":-2.5,\"rf_chain\":0}]}}";

    static String getValueOf(String json, String name) {
        int p = json.indexOf(name);
        int q = json.indexOf('"', p + 1);
        q = json.indexOf('"', q + 1);
        int r = json.indexOf('"', q + 1);
        return json.substring(q + 1, r);
    }

    public static void main(String[] args) {
        String ttnPayloadBase64 = getValueOf(mqttPayload, "payload_raw");
        System.out.println(ttnPayloadBase64);
        byte[] ttnPayload = Base64.getDecoder().decode(ttnPayloadBase64);
        ByteBuffer buf = ByteBuffer.wrap(ttnPayload);
		System.out.println(buf.getShort() / 100.0);
		System.out.println(buf.getShort() / 100.0);
    }
}
