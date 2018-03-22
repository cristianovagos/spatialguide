Django backoffice

Versions:

  mysql => 14.14
  django => 2.0.3


Mysql:
	
	terminal:
		service mysql start
		mysql -u root
	Mysql:
		create database spatialguide_db;


pip3 install django==2.0.3
pip3 install djangorestframework

python3 manage.py makemigrations
python3 manage.py migrate