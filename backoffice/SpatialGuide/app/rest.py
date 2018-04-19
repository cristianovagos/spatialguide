from django.shortcuts import render,redirect
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import CreateAPIView
from django.contrib.auth import logout
from rest_framework.permissions import AllowAny,IsAuthenticated

from django.core.files.storage import default_storage
from django.core.files.base import ContentFile

from rest_framework import status
from .server_utils import *

import json

# route/ or route/<id>
class RouteList(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request,route_id=None):
        if route_id:
            route = Route.objects.filter(pk=route_id)

            if not route.exists():
                raise ValidationError('Route does not exist')
            else:
                route = route.first()

            points = Point.objects.filter(route_contains_point__Route_id=route_id)

            serializer_route = RouteSerializer(route)
            serializer_point = PointSerializer(points,many=True)

            rest_response = dict(serializer_route.data)
            rest_response['Points']= serializer_point.data

            return Response(rest_response)

        else:
            routes = Route.objects.all()
            serializer = RouteSerializer(routes,many=True)

            return Response(serializer.data)

class addRoute(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        tparams = {
            'title': 'Route',
            'form_t': RouteForm()
        }
        return render(request, 'form_route.html', tparams)

    def post(self, request):
        form = RouteForm(request.POST, request.FILES)

        if form.is_valid():
            route = form.save()
            route.Image = request_filesaver(request.FILES['Image'])
            route.save()

        tparams = {
            'title': 'Route',
            'form_t': RouteForm()
        }
        return render(request, 'form_route.html', tparams)

# point/
class PointList(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        points = Point.objects.all()
        serializer = PointSerializer(points,many=True)
        return Response(serializer.data)


class addPoint(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        tab_names, point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'point_array': json.dumps(point_list),
            'form_t': PointForm()
        }

        return render(request, 'form_point.html', tparams)

    def post(self, request):
        form = PointForm(request.POST, request.FILES)
        if form.is_valid():
            point = form.save()
            point.Image = request_filesaver(request.FILES['Image'])
            point.Sound = request_filesaver(request.FILES['Sound'])
            point.save()

        tab_names, point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'point_array': json.dumps(point_list),
            'form_t': PointForm()
        }

        return render(request, 'form_point.html', tparams)

def request_filesaver(data):
    path = default_storage.save(str(data), ContentFile(data.read()))
    tmp_file_path = os.path.join(settings.MEDIA_ROOT, path)

    return save_media(str(data), tmp_file_path)


# route_points/{route_id}
class Route_PointList(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request,route_id):
        route_points = Point.objects.filter(route_contains_point__Route_id=route_id)
        serializer = PointSerializer(route_points, many=True)

        return Response(serializer.data)


# heatzone/
class HeatMap(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        tparams={
            'title': 'Heat Map',
            'heatpoint_array': json.dumps(get_heatPoints())
        }

        return render(request, 'heatmap.html', tparams)

    def post(self,request):
        form = HeatPointForm(request.data)

        if form.is_valid():
            form.save()
            return Response(status=status.HTTP_200_OK)
        else:
            return Response(status=status.HTTP_400_BAD_REQUEST)



class UserCreateView(CreateAPIView):
    permission_classes = [AllowAny]
    serializer_class = UserCreateSerializer
    queryset = User.objects.all()

    def get(self,request):
        return render(request, 'register.html')

    def post(self,request):
        serializer = UserCreateSerializer(data=request.data)

        if serializer.is_valid(raise_exception=True):
            serializer.save()
            new_data=serializer.data
            del new_data['password2']
            del new_data['email2']
            return Response(new_data, status=status.HTTP_200_OK)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class UserLoginView(APIView):
    permission_classes = [AllowAny]
    serializer_class = UserLoginSerializer

    def get(self,request):
        return render(request, 'login.html')

    def post(self,request, *args, **kwargs):
        serializer = UserLoginSerializer(data=request.POST)

        if serializer.is_valid(raise_exception=True):
            new_data = serializer.data
            username = new_data['username']
            password = request.POST['password']

            user = User.objects.filter(username=username).first()

            if user:
                user = authenticate(username=user.username, password=password)
            if user:
                login(request,user)
                return redirect('home')
            else:
                raise ValidationError('Password is incorrect.')

            return Response(new_data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class UserLogoutView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        logout(request)
        return redirect('login')

