from django.shortcuts import render,redirect
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.generics import CreateAPIView
from django.contrib.auth import logout
from rest_framework.permissions import AllowAny,IsAuthenticated,IsAdminUser

from django.core.files.storage import default_storage
from django.core.files.base import ContentFile

from rest_framework import status
from .server_utils import *


import json

DRIVE_BASE_URL='http://drive.google.com/uc?export=view&id='

class ShowPoints(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        tab_names, point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'tab_names': tab_names,
            'route_list': point_list,
            'add_btn': 'add_point'

        }
        return render(request, 'tables.html', tparams)

    def post(self,request):
        if not request.user.is_staff:
            return redirect('login')
        data = request.data

        if 'Name' in list(data.keys()):
            point_id = data.get('point_id', None)
            point_obj = Point.objects.get(pk=point_id)

            form = PointEditForm(data,instance=point_obj)
            if form.is_valid():
                point=form.save()
                if "Image" in request.FILES:
                    point.Image = DRIVE_BASE_URL + request_filesaver(request.FILES['Image'])
                if "Sound" in request.FILES:
                    point.Sound = request_filesaver(request.FILES['Sound'])
                point.save()

        elif 'edit' in list(data.keys()):
            point_id = data.get('edit', None)
            point_obj = Point.objects.get(pk=point_id)

            point = PointSerializer(point_obj).data
            form = PointEditForm(instance=point_obj)

            tparams = {
                'title': 'Point',
                'point_array': json.dumps(dict(point)),
                'form_t': form,
                'point_id': point_id
            }

            return render(request,'point_edit.html',tparams)

        elif 'remove' in list(data.keys()):
            point_id = data.get('remove',None)
            point = Point.objects.get(pk=point_id)
            point.delete()

        return self.get(request)

class ShowRoutes(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')

        tab_names, route_list = get_allRoutes()

        tparams = {
            'title': 'Route',
            'tab_names': tab_names,
            'route_list': route_list,
            'add_btn': 'add_route'

        }

        return render(request, 'tables.html', tparams)

    def post(self,request):
        if not request.user.is_staff:
            return redirect('login')
        data = request.data
        print(data)
        if 'Name' in list(data.keys()):
            route_id = data.get('route_id', None)
            print(route_id)
            route_obj = Route.objects.get(pk=route_id)

            form = RouteEditForm(data, instance=route_obj)
            if form.is_valid():
                route = form.save()
                if "Image" in request.FILES:
                    route.Image = DRIVE_BASE_URL + request_filesaver(request.FILES['Image'])
                route.save()

        elif 'edit' in list(data.keys()):
            route_id = data.get('edit', None)
            route_obj = Route.objects.get(pk=route_id)

            form = RouteEditForm(instance=route_obj)

            tparams = {
                'title': 'Point',
                'form_t': form,
                'route_id': route_id
            }

            return render(request, 'route_edit.html', tparams)

        return self.get(request)

class ShowRoute(APIView):
    permission_classes = [AllowAny]

    def get(self,request,route_id):
        if not request.user.is_staff:
            return redirect('login')

        points = get_route_points(route_id)
        route = get_route(route_id)

        excluded_points = get_ExcludedPoints(route_id)

        tparams = {
            'title': 'Route',
            'route': route,
            'points': points,
            'all_points': excluded_points,
            'point_array': json.dumps(points)
        }
        return render(request, 'route.html', tparams)

    def post(self,request,route_id):
        if not request.user.is_staff:
            return redirect('login')
        data = request.data

        route = Route.objects.filter(pk=route_id).first()

        if 'add' in list(data.keys()):
            point_id = data.get('add',None)

            if point_id:
                point = Point.objects.filter(pk=point_id).first()

                exists = Route_contains_Point.objects.filter(Q(Route_id=route.pk)).filter(Q(Point_id=point.pk)).first()
                if not exists:
                    Route_contains_Point(Route=route,Point=point).save()
                    generate_mapImage(route_id)

        if 'remove' in list(data.keys()):
            point_id = data.get('remove',None)

            if point_id:
                point = Point.objects.filter(pk=point_id).first()

                Route_contains_Point.objects.filter(Q(Route=route) and Q(Point=point)).first().delete()
                generate_mapImage(route_id)

        if 'removeRoute' in list(data.keys()):
            route.delete()
            return redirect('show_routes')


        return self.get(request,route_id)

# route/ or route/<id>
class RouteList(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request,route_id=None):
        if route_id:
            route = Route.objects.filter(pk=route_id).first()

            if not route:
                raise ValidationError('Route does not exist')

            route.Number_Downloads = route.Number_Downloads + 1
            route.save()

            points = Point.objects.filter(route_contains_point__Route_id=route_id)

            serializer_route = RouteSerializer(route)
            serializer_point = PointSerializer(points,many=True)

            rest_response = dict(serializer_route.data)
            rest_response['Points']= serializer_point.data

            return Response(rest_response,status=status.HTTP_200_OK)

        else:
            routes = Route.objects.all()
            serializer = RouteSerializer(routes,many=True)

            return Response(serializer.data)

# point/
class PointList(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request,point_id=None):
        if point_id:
            point = Point.objects.filter(pk=point_id).first()

            if not point:
                raise ValidationError('Point does not exist')

            response = PointSerializer(point).data

            return Response(response,status=status.HTTP_200_OK)

        else:
            points = Point.objects.all()
            serializer = PointSerializer(points,many=True)
            return Response(serializer.data)

class addRoute(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        tparams = {
            'title': 'Route',
            'form_t': RouteForm()
        }
        return render(request, 'form_route.html', tparams)

    def post(self, request):
        if not request.user.is_staff:
            return redirect('login')
        form = RouteForm(request.data, request.FILES)

        if form.is_valid():
            route = form.save()
            route.Image = DRIVE_BASE_URL+request_filesaver(request.FILES['Image'])
            route.save()
            return redirect('show_routes')

        tparams = {
            'title': 'Route',
            'form_t': RouteForm()
        }
        return render(request, 'form_route.html', tparams)

class addPoint(APIView):
    permission_classes = [IsAdminUser]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        tab_names, point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'point_array': json.dumps(point_list),
            'form_t': PointForm()
        }

        return render(request, 'form_point.html', tparams)

    def post(self, request):
        if not request.user.is_staff:
            return redirect('login')
        form = PointForm(request.data, request.FILES)
        if form.is_valid():
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
        if not request.user.is_staff:
            return redirect('login')
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

# visitpoint/
class UserVisit(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        data = request.data

        point = data.get('point',None)

        error = {}
        try:
            point = int(point)
            point = Point.objects.filter(pk=point).first()
        except:
            point = None
            error['point'] = 'Point does not Exist!'
            return Response(error,status=status.HTTP_400_BAD_REQUEST)

        user = User.objects.filter(username=request.user).first()

        if user:
            user_att = User_Attributes.objects.filter(User_id__id=user.id).first()
            new_point = Point_Visited(Point_id=point)
            new_point.save()

            user_att.Visited_points.add(new_point)


        return Response(error, status=status.HTTP_200_OK)

# useraddfavourite/
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
            user_att = User_Attributes.objects.filter(User_id__id=user.id).first()
            if point:
                user_att.Favorite_points.add(point)
            else:
                error['point']='Point does not Exist!'

            if route:
                user_att.Favorite_routes.add(route)
            else:
                error['route']='Route does not Exist!'

        return Response(error,status=status.HTTP_200_OK)

# userremovefavourite/
class RemoveFavourite(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        data = request.data

        point = data.get('point', None)
        route = data.get('route', None)

        error = {}
        try:
            point = int(point)
            point = Point.objects.filter(pk=point).first()
        except:
            point = None
            error['point'] = 'Point does not Exist!'

        try:
            route = int(route)
            route = Route.objects.filter(pk=route).first()
        except:
            route = None
            error['route'] = 'Route does not Exist!'

        user = User.objects.filter(username=request.user).first()
        if user:
            user_att = User_Attributes.objects.filter(User_id__id=user.id).first()
            if point:
                user_att.Favorite_points.remove(point)
            else:
                error['point'] = 'Point does not Exist!'

            if route:
                user_att.Favorite_routes.remove(route)
            else:
                error['route'] = 'Route does not Exist!'

        return Response(error, status=status.HTTP_200_OK)

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

            if not request.user_agent.is_mobile:
                user = User.objects.filter(username=new_data['username']).first()
                user.is_superuser=1
                user.is_staff=1
                user.save()
                return redirect('show_routes')

            return Response(new_data, status=status.HTTP_200_OK)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

# login/
class UserLoginView(APIView):
    permission_classes = [AllowAny]
    # serializer_class = UserLoginSerializer

    def get(self,request):
        return render(request, 'login.html')

    def post(self,request, *args, **kwargs):
        serializer = UserLoginSerializer(data=request.data)

        username = request.data.get('username',None)
        password = request.data.get('password',None)

        if not username or not password:
            return render(request, 'login.html', {'error': 'Credentials are Invalid!'})

        if serializer.is_valid(raise_exception=True):
            new_data = serializer.data
            if 'username' not in list(new_data.keys()):
                return render(request, 'login.html', {'error': 'Credencials are Invalid!'})

            username = new_data['username']
            password = request.data['password']

            user = User.objects.filter(Q(username=username) | Q(email=username)).first()

            if user:
                user = authenticate(username=user.username, password=password)
            if user:
                login(request,user)
                if request.user_agent.is_mobile:
                    return Response(new_data, status=status.HTTP_200_OK)
                else:
                    return redirect('show_routes')
            elif request.user_agent.is_mobile:
                return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
        else:
            return render(request,'login.html',{'error':'Credencials are Invalid!'})

# userinfo/
class UserInfo(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request):
        user = User.objects.filter(username=str(request.user)).first()
        if not user:
            return Response({'error':'User doesn\'t Exist!'},status=status.HTTP_400_BAD_REQUEST)

        user_att = User_Attributes.objects.filter(User_id__id=user.id).first()

        user_serializer = UserSerializer(user).data
        user_att_serializer = UserAttrSerializer(user_att).data

        full_user = user_serializer
        for key in list(user_att_serializer.keys()):
            full_user[key]=user_att_serializer[key]

        points_visited = Point.objects.filter(point_visited__user_attributes__User_id_id=user.pk)
        points_visited = PointSerializer(points_visited, many=True).data

        points_id = []
        for p in points_visited:
            points_id.append(p['id'])

        full_user['Visited_points']=points_id

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
    permission_classes = [AllowAny]

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

class UserSuggestionView(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        data = request.data
        latitude = data.get('latitude',None)
        longitude = data.get('longitude',None)
        comment = data.get('comment',None)

        if not latitude or not longitude or not comment:
            return Response(status=status.HTTP_400_BAD_REQUEST)

        suggestion = User_Suggestions(Latitude=latitude,Longitude=longitude,Comment=comment)
        suggestion.save()

        return Response(status=status.HTTP_200_OK)

class UserSuggestionsAdminView(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        tab_names, user_suggestion = get_Suggestions()

        tparams = {
            'title': 'User Sugestions',
            'tab_names': tab_names,
            'route_list': user_suggestion,

        }

        return render(request, 'tables.html', tparams)
