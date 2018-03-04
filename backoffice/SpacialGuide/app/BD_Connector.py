import mysql.connector
import json

conn = mysql.connector.connect(user='root',password='',host='localhost',database='SpacialGuide_DB')
mycursor = conn.cursor()

#mycursor.execute('SHOW TABLES')
mycursor.execute('Select * from Route')

#Get the execute response
print(mycursor.fetchall())

# Converts Response in json
#json.dumps(dict(mycursor.fetchall()))


# Call Stored Procedure
#mycursor.callproc('Procedure_Name', args=()