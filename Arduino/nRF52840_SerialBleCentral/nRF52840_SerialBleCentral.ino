void setup() {
  Serial.begin(115200);
}

int state = 0;

void loop() {
    if (Serial.available()) {
        int ch = Serial.read();

        //Serial.readBytesUntil(character, buffer, length)

        // S; => ble_addr ble_addr;
        // C ble_addr; => svc_uuid svc_uuid;
        // D ble_addr/svc_uuid; => chr_uuid chr_uuid;
        // R ble_addr/svc_uuid/chr_uuid; => 00 00 00 00;
        // W ble_addr/svc_uuid/chr_uuid 00 00 00 00; => ;

        if ((state == 0) && (ch == '?')) {
            state = 1;  
        } else if ((state == 1) && (ch == '!')) {
            state = 2;
        } else { //if (ch != -1) { 
            state = 0;  
        }
        Serial.println(state);
    }
}
