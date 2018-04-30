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

DRIVE_BASE_URL='http://drive.google.com/uc?export=view&id='

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
        form = RouteForm(request.data, request.FILES)

        if form.is_valid():
            route = form.save()
            route.Image = DRIVE_BASE_URL+request_filesaver(request.FILES['Image'])
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
        form = PointForm(request.data, request.FILES)
        if form.is_valid():
            print(form)
            point = form.save()
            point.Image = DRIVE_BASE_URL+request_filesaver(request.FILES['Image'])
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


# userfavourite/
class AddFavorite(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        data = request.data

        point = data.get('point',None)
        route = data.get('route',None)

        error ={}
        try:
            point = int(point)
            point = Point.objects.filter(pk=point).first()
        except:
            point=None
            error['point'] = 'Point does not Exist!'

        try:
            route = int(route)
            route = Route.objects.filter(pk=route).first()
        except:
            route = None
            error['route'] = 'Route does not Exist!'

        user = User.objects.filter(username=request.user).first()
        if user:
            user_att = User_Attributes.objects.filter(pk=user.id).first()
            if point:
                user_att.Favorite_points.add(point)
            else:
                error['point']='Point does not Exist!'

            if route:
                user_att.Favorite_routes.add(route)
            else:
                error['route']='Route does not Exist!'

        return Response(error,status=status.HTTP_200_OK)


# register/
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

# login/
class UserLoginView(APIView):
    permission_classes = [AllowAny]
    serializer_class = UserLoginSerializer

    def get(self,request):
        return render(request, 'login.html')

    def post(self,request, *args, **kwargs):
        serializer = UserLoginSerializer(data=request.data)

        if serializer.is_valid(raise_exception=True):
            new_data = serializer.data
            username = new_data['username']
            password = request.data['password']

            user = User.objects.filter(Q(username=username) | Q(email=username)).first()

            if user:
                user = authenticate(username=user.username, password=password)
            if user:
                login(request,user)
            else:
                raise ValidationError('Password is incorrect.')

            return Response(new_data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

# userinfo/
class UserInfo(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        user = User.objects.filter(username=str(request.user)).first()
        user_att = User_Attributes.objects.filter(pk=user.id).first()

        user_serializer = UserSerializer(user).data
        user_att_serializer = UserAttrSerializer(user_att).data

        full_user = user_serializer
        for key in list(user_att_serializer.keys()):
            full_user[key]=user_att_serializer[key]

        return Response(full_user,status=status.HTTP_200_OK)

# changepass/
class ChangePassword(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        old_pass = request.data['old_pass']
        new_pass = request.data['new_pass']
        pass_confirmation = request.data['pass_confirmation']

        if new_pass != pass_confirmation:
            return Response(status=status.HTTP_304_NOT_MODIFIED)

        user = User.objects.filter(username=str(request.user)).first()

        if user:
            user = authenticate(username=user.username, password=old_pass)
        else:
            return Response(status=status.HTTP_304_NOT_MODIFIED)

        if user:
            user.set_password(new_pass)
            user.save()
            return Response(status=status.HTTP_200_OK)
        else:
            return Response(status=status.HTTP_304_NOT_MODIFIED)


# changepass/
class ChangeEmail(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        password = request.data['password']
        old_email = request.data['old_email']
        new_email = request.data['new_email']
        email_confirmation = request.data['email_confirmation']

        if new_email != email_confirmation:
            return Response(status=status.HTTP_304_NOT_MODIFIED)

        user = User.objects.filter(email=old_email).first()

        if user:
            user = authenticate(username=user.username, password=password)
        else:
            return Response(status=status.HTTP_304_NOT_MODIFIED)

        if user:
            user.email=new_email
            user.save()
            return Response(status=status.HTTP_200_OK)
        else:
            return Response(status=status.HTTP_304_NOT_MODIFIED)


# recoverpass/
class RecoverPassword(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        data = request.data
        username = data.get('username',None)
        new_pass = data.get('new_pass',None)
        confirm_pass = data.get('confirm_pass',None)


        if (not username) or (not new_pass) or (not confirm_pass):
            return Response({'Parameters':'One or more parameters are missing!'},status=status.HTTP_400_BAD_REQUEST)

        if new_pass != confirm_pass:
            return Response({'new_pass':'Passwords must match!'},status=status.HTTP_400_BAD_REQUEST)

        user = User.objects.filter(username=username).first()

        if user:
            user.set_password(new_pass)
            user.save()
            return Response(status=status.HTTP_200_OK)
        else:
            return Response(status=status.HTTP_400_BAD_REQUEST)

class UserLogoutView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        logout(request)
        return redirect('login')

