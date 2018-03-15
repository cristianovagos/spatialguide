from django.db import models
from django.forms import ModelForm
from django import forms

class Route(models.Model):
    Name = models.CharField(max_length=50)
    Description = models.CharField(max_length=100)

    def __str__(self):
        return "{'name': %s, 'description': %s}" % (self.Name,self.Description)



class Point(models.Model):
    Name = models.CharField(max_length=50)
    Latitude = models.FloatField()
    Longitude = models.FloatField()

    def __str__(self):
        return "Name: %s ( %s, %s )" %(self.Name,self.Latitude,self.Longitude)

class Route_contains_Point(models.Model):
    Route = models.ForeignKey(Route, on_delete=models.CASCADE)
    Point = models.ForeignKey(Point, on_delete=models.CASCADE)

    def __str__(self):
        return "Route '%s' contains point '%s'" %(self.route.name,self.point.name)




class RouteForm(ModelForm):
    Name = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Name'}))
    Description = forms.CharField(widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder':'Description'}))

    class Meta:
        model = Route
        fields = '__all__'
