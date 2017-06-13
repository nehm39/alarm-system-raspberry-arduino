from nrf24pihub.nrf24 import NRF24
import thread
import subprocess
import time
import sys
import os
from mysql.connector import errorcode
from time import gmtime, strftime
from datetime import datetime
import mysql.connector
import atexit
import smtplib
import cv2.cv as cv

#Pobieramy realna sciezke skryptu:
scriptDir = os.path.dirname(os.path.realpath(sys.argv[0]))

#Jezeli plik 'running' istnieje to znaczy to, ze jest juz uruchomiona instancja skryptu i zamykamy skrypt.
if not os.path.exists('{0}/temp/running'.format(scriptDir)):
	open('{0}/temp/running'.format(scriptDir), 'w').close()
else:
	print 'System juz dziala.'
	raise SystemExit
	
#Funkcja zapisujaca wszelkie komunikaty do pliku (wraz z data) oraz na ekran za pomoca print.
def writeText(text):
	file = open('{0}/logs.txt'.format(scriptDir), 'a')
	file.write('{0} - {1}\n'.format(str(datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')), text))
	print text
	file.close()
	
#Funkcja wywolywana w momencie zamkniecia skryptu.
def exitHandler():
	writeText('Wylaczanie systemu...')
	#Usuniecie pliku 'running'.
	os.remove('{0}/temp/running'.format(scriptDir))

atexit.register(exitHandler)

#Konfiguracja serwera mySQL:
dbName = "alarmSystem"
sqlCredentials = {
  'user': 'root',
  'password': 'qazwsx123',
  'host': '127.0.0.1',
  'raise_on_warnings': True,
}
#Konfiguracja serwera SMTP:
SMTPserver = 'smtp.gmail.com'
SMTPport = '587'
SMTPuser = 'nehm.raspberrypi@gmail.com'
SMTPpasswd = 'DFG$#YGRT@'
#Sciezka do zapisu zdjec:
picturesDir = '/var/www/pictures'

#Funkcja tworzaca baze danych.
def createDatabase(cursor):
    try:
        cursor.execute(
            "CREATE DATABASE {} DEFAULT CHARACTER SET 'utf8'".format(dbName))
    except mysql.connector.Error as err:
		writeText('Blad przy tworzeniu bazy.')
		pass

#Funkcja tworzaca tabele w bazie danych.
def createTables(cursor):
	tables = {}
	tables['configuration'] = (
		"CREATE TABLE `configuration` ("
		"  `id` int(11) NOT NULL AUTO_INCREMENT,"
		"  `name` varchar(50) NOT NULL,"
		"  `value` varchar(50) NOT NULL,"
		"  PRIMARY KEY (`id`)"
		") ENGINE=InnoDB")
	tables['alarms'] = (
		"CREATE TABLE `alarms` ("
		"  `id` int(11) NOT NULL AUTO_INCREMENT,"
		"  `start_time` datetime NOT NULL,"
		"  `end_time` datetime NOT NULL,"
		"  PRIMARY KEY (`id`)"
		") ENGINE=InnoDB")

	for name, ddl in tables.iteritems():
		try:
			cursor.execute(ddl)
		except mysql.connector.Error as err:
			#Blad przy tworzeniu tabel. Tabele juz istnieja.
			pass

#Funkcja dodajaca rekordy konfiguracji do bazy jezeli zadne nie istnieja:
def createConfiguration(cnx, cursor):
	cursor.execute("SELECT * from configuration limit 1")
	result = cursor.fetchone()
	if result is None:
		for line in open('config.sql'):
			cursor.execute(line)
		cnx.commit()

#Nawiazujemy polaczenie z baza.
try:
	cnx = mysql.connector.connect(**sqlCredentials)
except:
	#Jezeli nie mozemy polaczyc sie z baza danych to wylaczamy program.
	writeText('Blad polaczenia z baza danych.')
	raise SystemExit

cursor = cnx.cursor(buffered=True)
#Wybieramy baze danych. Jezeli nie istnieje to tworzymy ja.
try:
	cnx.database = dbName
except mysql.connector.Error as err:
	if err.errno == errorcode.ER_BAD_DB_ERROR:
		createDatabase(cursor)
		cnx.database = dbName

#Probujemy utworzyc tabele.
createTables(cursor)
createConfiguration(cnx, cursor)
cursor.close()
cnx.close()

#Funkcja zapisujaca zdarzenie (alarm) do bazy danych.
def saveAlarmLogToSql(startTime, endTime):
	try:
		cnx = mysql.connector.connect(**sqlCredentials)
		cursor = cnx.cursor(buffered=True)
		cnx.database = dbName
		cursor.execute("INSERT INTO alarms (start_time, end_time) VALUES ('{0}', '{1}')".format(startTime, endTime))
		cnx.commit()
	except:
		writeText('Wystapil blad przy zapisywaniu zdarzenia do bazy danych')
	finally:
		cursor.close()
		cnx.close()

#Funkcja pobierajaca wartosc danej konfiguracji z bazy danych.
def getConfigFromSql(name):
	try:
		cnx = mysql.connector.connect(**sqlCredentials)
		cursor = cnx.cursor(buffered=True)
		cnx.database = dbName
		cursor.execute("SELECT value FROM configuration WHERE name = '{}'".format(name))
		row = cursor.fetchone()
		value = row[0]
	except:
		writeText('Wystapil blad przy pobieraniu danych z bazy danych.')
		value = -1
	finally:
		cursor.close()
		cnx.close()
	return value
		
#Funkcja wydajaca polecania zapisu zdjec.
def makePictures(delay):
	while currentState == 111:
		thread.start_new_thread(savePicture, ())
		#time.sleep(1. * 1/25)
		time.sleep(1. * 1/delay)

#Funkcja zapisujaca zdjecia.
def savePicture():
	fileName = '{0}.jpg'.format(str(datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')))
	filePath = os.path.join(picturesDir, fileName)
	img = cv.QueryFrame(capture)
	cv.SaveImage(filePath, img)

#Funkcja wysylaja email.
def sendMail(time):
	msg = """From: %s <%s> \nTo: %s \nSubject: %s\n\nWykryto alarm o godzinie: %s.""" % (getConfigFromSql('senderEmail'), SMTPuser, getConfigFromSql('receiverEmail'), getConfigFromSql('emailSubject'), time)
	try:
		server = smtplib.SMTP(SMTPserver, SMTPport)
		server.ehlo()
		server.starttls()
		server.login(SMTPuser, SMTPpasswd)
		server.sendmail(getConfigFromSql('senderEmail'), getConfigFromSql('receiverEmail'), msg)
		server.close()
	except smtplib.SMTPException:
		writeText('Nie mozna wyslac wiadomosci e-mail.')

#Obecny stan (alarmu) - stan poczatkowy.
currentState = 0

#Znalezienie kamery i nawiazanie polaczenia.
try:
	capture = cv.CaptureFromCAM(0)
except:
	writeText('Nie udalo sie nawiazac polaczenia z kamera.')
	raise SystemExit

#Konfiguracja modulu wifi.
pipes = [[0xf0, 0xf0, 0xf0, 0xf0, 0xe1], [0xf0, 0xf0, 0xf0, 0xf0, 0xd2]]

radio = NRF24()
radio.begin(0,0,25,18)
radio.setRetries(15,10)
radio.setPayloadSize(1)
radio.setChannel(0x64)
radio.setDataRate(NRF24.BR_250KBPS)
radio.setPALevel(NRF24.PA_MAX)
radio.setAutoAck(1)
radio.openWritingPipe(pipes[0])
radio.openReadingPipe(1, pipes[1])
radio.startListening()

#Powiadomienie o gotowosci systemu.
writeText('System wlaczony.')

while True:		
	pipe = [0]
	#W przypadku, gdy nie jest odbierany zaden sygnal:
	while not radio.available(pipe, True):
		#Zamykamy system, jezeli zostal utworzony plik 'stop'.
		if os.path.exists('{0}/temp/stop'.format(scriptDir)):
			writeText('Otrzymano polecenie zamkniecia systemu.')
			os.remove('{0}/temp/stop'.format(scriptDir))
			raise SystemExit
		#Opozniamy petle.
		time.sleep(0.001)
	receivedCode = []
	radio.read(receivedCode)
	#Jezeli zostal odebrany kod o wykryciu ruchu.
	if receivedCode[0] == 111:
		currentState = 111
		startDate = datetime.now()
		#Rozpoczynamy proces zapisu zdjec.
		thread.start_new_thread(makePictures, (int(getConfigFromSql('picturesPerSecond')),))
		writeText('Wykryto ruch.')
		#Wysylamy email jezeli tak zostalo ustawione w konfiguracji.
		if getConfigFromSql('sendEmail') == 'true':
			thread.start_new_thread(sendMail, (str(datetime.now().strftime('%H:%M:%S')),))
	else:
		#Jezeli zostal odebrany kod o zakonczeniu ruchu.
		if receivedCode[0] == 110:
			currentState = 110
			writeText('Zakonczono ruch.')
			#Zapisujemy w bazie zdarzenie jezeli tak zostalo ustawione w konfiguracji.
			if getConfigFromSql('logAlarms') == 'true':
				try:
					saveAlarmLogToSql(startDate, datetime.now())
				except:
					writeText('Nie udalo sie zapisac logu alarmu do bazy.')