#include <Servo.h>


int servoPorts[5] = {3, 5, 6, 9, 10};
double servoRanges[5][2] = {
  {20, 160}, // thumb
  {20, 160}, // pointer
  {20, 160}, // middle
  {20, 160}, // ring
  {20, 160}  // pinky
};
Servo servos[5];

void setup() {
  // Initialize the motor control pins as output
  pinMode(LED_BUILTIN, OUTPUT);

  // Initialize the Serial communication
  Serial.begin(9600);
  for (int i = 0; i < 5; i++) {
    servos[i].attach(servoPorts[i]);
  }
}

void loop() {
  // Check if there is data available to read
  if (Serial.available() > 0) {
    // Read the incoming data
    String dataPacket = Serial.readStringUntil('\n');
    double fingerCurls[5];
    for (int i = 0; i < 5; i++) {
      if (i < 4) {
        String part = dataPacket.substring(0, dataPacket.indexOf(","));
        dataPacket = dataPacket.substring(dataPacket.indexOf(",") + 1);
        fingerCurls[i] = part.toDouble();
      } else {//last one
        fingerCurls[i] = dataPacket.toDouble();
      }
    }
    // Normalize the data
    for (int i = 0; i < 5; i++) {
      fingerCurls[i] = max(0, min(90, fingerCurls[i]))/90.0; // [0, 1]
    }
    // Control finger (or just light)
    for (int i = 0; i < 5; i++) {
//      if (i != 1) continue; // Only testing pointer finger
      double servoRotation = servoRanges[i][0] /* (min) */ + (servoRanges[i][1]-servoRanges[i][0]) /* (range) */ * fingerCurls[i];
      servos[i].write(servoRotation);
      if (fingerCurls[i] > 0.5) {
        digitalWrite(LED_BUILTIN, HIGH);
      } else {
        digitalWrite(LED_BUILTIN, LOW);
      }
    }
    // Respond w/ ack
    Serial.write("ack");// + (char)(int)servoRotation);
    Serial.flush();
  }
}
