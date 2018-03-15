from django.db import models
from django.forms import ModelForm

class Route(models.Model):
    name = models.CharField(max_length=50)
    description = models.CharField(max_length=100)

    def __str__(self):
        return "{'name': %s, 'description': %s}" % (self.name,self.description)

class RouteForm(ModelForm):
    class Meta:
        model = Route
        fields = '__all__'

class Point(models.Model):
    name = models.CharField(max_length=50)
    latitude = models.FloatField()
    longitude = models.FloatField()

    def __str__(self):
        return "Name: %s ( %s, %s )" %(self.name,self.latitude,self.longitude)

class Route_contains_Point(models.Model):
    route = models.ForeignKey(Route, on_delete=models.CASCADE)
    point = models.ForeignKey(Point, on_delete=models.CASCADE)

    def __str__(self):
        return "Route '%s' contains point '%s'" %(self.route.name,self.point.name)