#include <Wire.h>
#include "OLED.h"
#include <I2Cdev.h>
#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>
#include <MPU6050.h>
#define RST_OLED 16
OLED display(4, 5);

/************************************************************************************/
/*This arduino application uses a Heltec wifi kit 8 and accelerometer GY521 MPU6050**/
/*Reads the values of each axis and checks in what position the device is being held*/
/*Author: Hadi Deknache                                                             */
/************************************************************************************/
MPU6050 accelgyro;
String val = "non";
const char* host = "http://192.168.8.161:8888/";
const char* ssid = "ssid";
const char* pass = "pass";

String current;
int16_t ax, ay, az;
int16_t gx, gy, gz;
double temp;

double oldAx = 0, oldAy = 0, oldAz = 0, oldGx = 0, oldGy = 0, oldGz = 0; //Used for holding the values before (state before)

int16_t difAx = 0, difAy = 0, difAz = 0, difGx = 0, difGy = 0, difGz = 0; //Used for saving the difference in each axis

int16_t Axb = 0, Ayb = 0, Azb = 0, Gxb = 0, Gyb = 0, Gzb = 0; // Used for calibrating the initial values

int window_size = 80; //The amount of times to read for generating accurate values
const int sda_pin = D3; // I2C SDA pin on Heltec wifi kit 8
const int scl_pin = D2; // I2C SCL pin on Heltec wifi kit 8

void setup() {
  pinMode(RST_OLED, OUTPUT);

  digitalWrite(RST_OLED, LOW);   // turn D2 low to reset OLED
  delay(50);
  digitalWrite(RST_OLED, HIGH);  // while OLED is running, must set D2 in high

  // Initialize display
  display.begin();

  // Test display ON and write
  display.on();
  delay(1000);
  display.print("Start Now!");

  // join I2C bus (I2Cdev library doesn’t do this automatically)
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
  Wire.begin(sda_pin, scl_pin);
#elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
  Fastwire::setup(400, true);
#endif
  
  Serial.begin(38400);
  Serial.println("Initializing I2C devices…");
  accelgyro.initialize();

  //Checks if accelerometer there is a connection with the accelerometer 
  Serial.println(accelgyro.testConnection() ? "MPU6050 connection successful" : "MPU6050 connection failed");

  /*Reads the accelerometer a few times and calculates the avrage*/
  /*value of each axis when standing still on the table*/
  
  for (int j = 0; j < window_size; j++) {
    getAcc();
    getGyro();
  }

  calibrate();

  /*Connects to the wifi so device can send the state to the java application*/
  WiFi.begin(ssid, pass);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(200);
    Serial.print(".");
  }

}

void loop() {

  for (int w = 0; w < window_size; w++) {
    getAcc();
    getGyro();
  }

  ax = (oldAx / window_size);
  ay = (oldAy / window_size);
  az = (oldAz / window_size);
  gx = (oldGx / window_size);
  gy = (oldGy / window_size);
  gz = (oldGz / window_size);

  /*Serial.print(ax); Serial.print("\t");
    Serial.print(ay); Serial.print("\t");
    Serial.print(az); Serial.print("\t");
    Serial.print(gx); Serial.print("\t");
    Serial.print(gy); Serial.print("\t");
    Serial.println(gz);*/

  difAx = abs(Axb) - abs(ax);
  difAy = abs(Ayb) - abs(ay);
  difAz = abs(Azb) - abs(az);

  difGx = abs(Gxb) - abs(gx);
  difGy = abs(Gyb) - abs(gy);
  difGz = abs(Gzb) - abs(gz);

  if (abs(difAx) > 500 || abs(difAy) > 500 || abs(difAz) > 500) {

    if (ax < 4800 && ay < -6000 && az < 14000) {
      //Serial.println("UP");
      val = "UP";
    } else if (ay > 10000 && az < 11000) {
      //Serial.println("DOWN");
      val = "DOWN";
    } else if (ax < -2400) {
      //Serial.println("LEFT");
      val = "LEFT";
    } else if (ax > 14000 && az < 3000 ) {
      //Serial.println("RIGHT");
      val = "RIGHT";
    } else {
      //Serial.println("No movement!");
      val = "non";
    }
  }
  delay(100);
  send_dir();
  reset_val();
  
  if (current != val) {
    current = val;  //save the current state so we don't have to write each time to the screen
    screen();       //Initialize the screen as i2c doesn't allow to use lcd and accelerometer at the same time
    gyro();         //Initialize the accelerometer&gyro again after writing to the screen
  }


}


/*Method used for initilizing the screen and write*/
void screen() {
  display.begin();
  display.on();
  display.print((char*) val.c_str());
}

/*Method used for initilizing the accelerometer&gyro to read the values again*/
void gyro() {
#if I2CDEV_IMPLEMENTATION == I2CDEV_ARDUINO_WIRE
  Wire.begin(sda_pin, scl_pin);
#elif I2CDEV_IMPLEMENTATION == I2CDEV_BUILTIN_FASTWIRE
  Fastwire::setup(400, true);
#endif
  accelgyro.initialize();
}

/*This method is used for sending the state of the device to the java application*/
void send_dir() {
  if (val != "non") {
    if (WiFi.status() != WL_CONNECTED) {
      WiFi.begin(ssid, pass);
      Serial.print("Connecting to WiFi");
      while (WiFi.status() != WL_CONNECTED) {
        delay(100);
      }

    }
    HTTPClient http;                //Create an object of HTTPClient
    http.begin(host+val             //Specify request destination
    http.POST("");                  //Send request to java
    http.end();                     //Close connection*/
  }


}

/*Method for reading gyroscope values*/
void getGyro()
{
  accelgyro.getRotation(&gx, &gy, &gz);
  oldGx += gx;
  oldGy += gy;
  oldGz += gz;

}

/*Method for reading accelerometer values*/
void getAcc()
{
  accelgyro.getAcceleration(&ax, &ay, &az);
  oldAx += ax;
  oldAy += ay;
  oldAz += az;

}

/*Method for reading the temperature of the MPU6050 device*/
void getTemp() {
  temp = (double) accelgyro.getTemperature() / 340 + 36.53;
}

/*This method is used for handling the values read and save the initial values at start, the calibration values*/
void calibrate() {
  int cAx = (oldAx / window_size);
  int cAy = (oldAy / window_size);
  int cAz = (oldAz / window_size);
  int cGx = (oldGx / window_size);
  int cGy = (oldGy / window_size);
  int cGz = (oldGz / window_size);

  Serial.println("Calibration values: ");
  Serial.print(cAx); Serial.print("\t");
  Serial.print(cAy); Serial.print("\t");
  Serial.print(cAz); Serial.print("\t");
  Serial.print(cGx); Serial.print("\t");
  Serial.print(cGy); Serial.print("\t");
  Serial.println(cGz);

  Axb = cAx;
  Ayb = cAy;
  Azb = cAz;
  Gxb = cGx;
  Gyb = cGy,
  Gzb = cGz;

  delay(1000);
  reset_val();

}

/*This method resets all variables after have read*/
void reset_val() {
  oldGx = 0;
  oldGy = 0;
  oldGz = 0;
  oldAx = 0;
  oldAy = 0;
  oldAz = 0;

  ax = 0;
  ay = 0;
  az = 0;
  gx = 0;
  gy = 0;
  gz = 0;
}

