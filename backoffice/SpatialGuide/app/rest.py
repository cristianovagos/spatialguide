from rest_framework.views import APIView
from rest_framework.response import Response

from .serializer import *
from rest_framework import status


# route/ or route/<id>
class RouteList(APIView):

    def get(self,request,route_id=None):
        if route_id:
            routes = Route.objects.filter(id=route_id)
        else:
            routes = Route.objects.all()

        serializer = RouteSerializer(routes,many=True)
        route_list=serializer.data

        route_list=route_list[0]['id']

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

# route_points/{route_id}
class Route_PointList(APIView):

    def get(self,request,route_id):
        route_points = Point.objects.filter(route_contains_point__Route_id=route_id)
        serializer = PointSerializer(route_points, many=True)

        return Response(serializer.data)


    def post(self,request):
        pass



class HeatZoneForm(APIView):

    def post(self,request):

        print(request.data)

        return Response(status=status.HTTP_200_OK)
