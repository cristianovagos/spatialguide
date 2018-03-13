from django.db import models

class Route(models.Model):
    name = models.CharField(max_length=50)
    description = models.CharField(max_length=100)

    def __str__(self):
        return "{'name': %s, 'description': %s}" % (self.name,self.description)

class Point(models.Model):
    name = models.CharField(max_length=50)
    x = models.FloatField()
    y = models.FloatField()

    def __str__(self):
        return "Name: %s ( %s, %s )" %(self.name,self.x,self.y)

class Route_contains_Point(models.Model):
    route = models.ForeignKey(Route)
    point = models.ForeignKey(Point)

    def __str__(self):
        return "Route '%s' contains point '%s'" %(self.route.name,self.point.name)