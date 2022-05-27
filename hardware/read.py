import RPi.GPIO as GPIO
from mfrc522 import SimpleMFRC522

import time
import pymysql as mysql

reader = SimpleMFRC522()

try:
	id, text = reader.read()
	print(id)
	print(text)

	try:
		connection = mysql.connect( user = "root", password = "", host = "192.168.100.9", port = "3306", database = "db_speedy-packages")
		cursor = connection.cursor()
		print("Conexion MySQL abierta")
		query = "INSERT INTO history (card_number) VALUES ('%i')" % id
		print(query)
		cursor.execute(query)
		connection.commit()


	except (mysql.err.OperationalError, mysql.err.InternalError) as error:
		GPIO.setmode(GPIO.BOARD)
		GPIO.setup(13,GPIO.OUT)
		GPIO.output(13,True)
		time.sleep(2)
		GPIO.output(13,False)
		time.sleep(1)
		print(error)

	finally:
		if(connection):
			cursor.close()
			connection.close()
			print("Conexion MySQL cerrada")
finally:

	GPIO.cleanup()
