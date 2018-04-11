from django.db import models
from django.forms import ModelForm
from django import forms

class Route(models.Model):
    Name = models.CharField(max_length=50)
    Description = models.CharField(max_length=500)
    Image = models.CharField(max_length=33)

    def __str__(self):
        return "{'name': %s, 'description': %s}" % (self.Name,self.Description)



class Point(models.Model):
    Name = models.CharField(max_length=50)
    Url = models.CharField(max_length=100)
    Description = models.CharField(max_length=500)
    Latitude = models.FloatField()
    Longitude = models.FloatField()
    Image = models.CharField(max_length=33)

    def __str__(self):
        return "Name: %s ( %s, %s )" %(self.Name,self.Latitude,self.Longitude)

class Route_contains_Point(models.Model):
    Route = models.ForeignKey(Route, on_delete=models.CASCADE)
    Point = models.ForeignKey(Point, on_delete=models.CASCADE)

    def __str__(self):
        return "Route '%s' contains point '%s'" %(self.Route.Name,self.Point.Name)




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

    class Meta:
        model = Point
        fields = ['Name','Url','Description','Latitude','Longitude']

