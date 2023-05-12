#include <Servo.h>

Servo server;

void setup() {
  // Initialize the motor control pins as output
  pinMode(LED_BUILTIN, OUTPUT);

  // Initialize the Serial communication
  Serial.begin(9600);
  server.attach(9);
}

void loop() {
  // Check if there is data available to read
  if (Serial.available() > 0) {
    // Read the incoming data
    String dataPacket = Serial.readStringUntil('\n');
    double fingerCurl = max(0, min(90, dataPacket.toDouble()))/90.0;//[0, 1]
    // Control finger (or just light)
    float servoRotation = 20 + (fingerCurl)*140;
    server.write(servoRotation);
    if (fingerCurl > 45) {
      digitalWrite(LED_BUILTIN, HIGH);
    } else {
      digitalWrite(LED_BUILTIN, LOW);
    }
    //respond w/ ack
    Serial.write("ack");// + (char)(int)servoRotation);
    Serial.flush();
  }
}
