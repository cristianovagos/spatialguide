

from django.shortcuts import get_object_or_404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import *
from .serializer import *


# route/ or route/<id>
class RouteList(APIView):

    def get(self,request,route_id=None):
        if route_id:
            routes = Route.objects.filter(id=route_id)
        else:
            routes = Route.objects.all()

        ## Add to DB Example
        # if route_id==200:
        #     r = Route(name='Teste1',description='Lets try')
        #     r.save()

        serializer = RouteSerializer(routes,many=True)
        route_list=serializer.data

        route_list=route_list[0]['id']
        print("Route ID: %s" % route_list)


        return Response(serializer.data)

    def post(self,request):
        pass

# point/
class PointList(APIView):

    def get(self,request):
        points = Point.objects.all()
        serializer = PointSerializer(points,many=True)
        return Response(serializer.data)

    def post(self,request):
        pass

