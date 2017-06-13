#include <SPI.h>
#include "nRF24L01.h"
#include "RF24.h"

#define PIR 2 //PIN czujnika PIR
#define CS 9 //PIN CS modulu NRF24l01+
#define CSN 10 //PIN CSN modulu NRF24l01+

int calibrationTime = 20; //czas (s) kalibrowania czujnika PIR     
long unsigned int waitTime = 5000;  //czas (ms) do odczekania na zakonczenie ruchu
long unsigned int alarmStart; 
long unsigned int alarmEnd;   
boolean alarmEnded = true;
boolean motionInProgress = false;
RF24 radio(CS,CSN);
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL,0xF0F0F0F0D2LL };
uint8_t alarmStartCode = 111;
uint8_t alarmEndCode = 110;

void setup(void) {
	Serial.begin(9600);
	//konfiguracja nRF24:
	radio.begin();
	radio.setChannel(0x64); //kanal komunikacji (100)
	radio.setAutoAck(1);
	radio.setRetries(15,10);
	radio.setDataRate(RF24_250KBPS);
	radio.setPayloadSize(1); //wysylamy 1 bajt
	radio.openReadingPipe(1,pipes[0]);
	radio.openWritingPipe(pipes[1]);
	pinMode(PIR, INPUT);

	Serial.print("Kalibracja");
	for(int i = 0; i < calibrationTime; i++){
		Serial.print(".");
		delay(1000);
	}
	Serial.print("\nKalibracja zakonczona. Sensor aktywny.");
}

void loop() {
	if(digitalRead(PIR) == HIGH){
		if(alarmEnded){  
			alarmEnded = false;       
			alarmStart = millis();
			Serial.print("\n\nWykryto ruch. ");	 
			bool status = radio.write(&alarmStartCode,sizeof(alarmStartCode)); 
			delay(50);
		}         
		motionInProgress = true;
	}

	if(digitalRead(PIR) == LOW){       
		if(motionInProgress){
			alarmEnd = millis();
			motionInProgress = false;
		}
		if(!alarmEnded && millis() - alarmEnd > waitTime){  
			alarmEnded = true;                        
			Serial.print("|| Ruch zakonczony. Trwal: ");
			Serial.print((alarmEnd - alarmStart)/1000);
			Serial.print(" sekund.");
			bool status = radio.write(&alarmEndCode,sizeof(alarmEndCode));
			delay(50);
		}
	}
}