from django.db import models
from django.forms import ModelForm
from django import forms
from django.contrib.auth.models import User
import time

from datetime import date

class Route(models.Model):
    Name = models.CharField(max_length=50)
    Description = models.CharField(max_length=500)
    Image = models.CharField(max_length=150)
    Map_image = models.CharField(max_length=150)
    Route_Date = models.DateField(default=date.today())
    LastUpdate = models.BigIntegerField(default=int(round(time.time() * 1000)))
    Number_Downloads = models.IntegerField(default=0)

    def __str__(self):
        return "{'name': %s, 'description': %s}" % (self.Name,self.Description)


class Point(models.Model):
    Name = models.CharField(max_length=50)
    Url = models.CharField(max_length=100)
    Description = models.CharField(max_length=500)
    Point_Date = models.DateField(default=date.today())
    Latitude = models.FloatField()
    Longitude = models.FloatField()
    Image = models.CharField(max_length=150)
    Sound = models.CharField(max_length=33)
    LastUpdate = models.BigIntegerField(default=int(round(time.time() * 1000)))

    def __str__(self):
        return "Name: %s ( %s, %s )" %(self.Name,self.Latitude,self.Longitude)

class Route_contains_Point(models.Model):
    Route = models.ForeignKey(Route, on_delete=models.CASCADE)
    Point = models.ForeignKey(Point, on_delete=models.CASCADE)

    def __str__(self):
        return "Route '%s' contains point '%s'" %(self.Route.Name,self.Point.Name)

class Point_Visited(models.Model):
    Point_id = models.ForeignKey(Point, on_delete=models.CASCADE)
    Visit_Date = models.DateField(default=date.today())

class Heat_Point(models.Model):
    Latitude = models.FloatField()
    Longitude = models.FloatField()


class User_Attributes(models.Model):
    User_id = models.ForeignKey(User, on_delete=models.CASCADE)
    Favorite_points = models.ManyToManyField(Point)
    Favorite_routes = models.ManyToManyField(Route)
    Visited_points = models.ManyToManyField(Point_Visited)
    Image = models.CharField(max_length=150)



#################  FORMS  #############################

class RouteForm(ModelForm):
    Name = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Name'}))
    Description = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Description'}))
    Image = forms.FileField()

    class Meta:
        model = Route
        fields = ['Name','Description']


class PointForm(ModelForm):
    Name = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Name'}))
    Url = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Url'}))
    Description = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Description'}))
    Latitude = forms.FloatField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Latitude', 'readonly':'readonly'}))
    Longitude = forms.FloatField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Longitude', 'readonly':'readonly'}))
    Image = forms.FileField()
    Sound = forms.FileField()

    class Meta:
        model = Point
        fields = ['Name','Url','Description','Latitude','Longitude']


class HeatPointForm(ModelForm):
    class Meta:
        model = Heat_Point
        fields = '__all__'
