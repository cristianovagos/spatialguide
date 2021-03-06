from django.conf import settings
from django.contrib import messages
from django.http import HttpResponse
from django.core.mail import send_mail
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
from pusher_push_notifications import PushNotifications

import json

DRIVE_BASE_URL='http://drive.google.com/uc?export=view&id='

pn_client = PushNotifications(
    instance_id='1be0bfa7-2af5-4fe7-9e84-e78d07244959',
    secret_key='9BEC72D2C157102FD2493D3D4521F72',
)

class pushNotification(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        pass

    def post(self,request):
        if not request.user.is_staff:
            return redirect('login')

        data = request.data

        form = NotificationForm(data)

        if form.is_valid():
            notification = form.data

            title = notification['Title']
            message = notification['Message']

            try:
                response = pn_client.publish(
                    interests=['spatialguide'],
                    publish_body={
                        'fcm': {
                            'notification': {
                                'title': title,
                                'body': message,
                                'tag': 'route',
                                'color': '#2196F3',
                                'click_action': 'OPEN_APP'
                            },
                        },
                    },
                )
            except:
                print('Failed to send notification of route created.')

            return Response(status=status.HTTP_200_OK)
        return Response(status=status.HTTP_400_BAD_REQUEST)

class ShowPoints(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        point_list = get_allPoints()

        tparams = {
            'title': 'Point',
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
                point.LastUpdate = int(round(time.time() * 1000))
                point.save()

                routes = Route_contains_Point.objects.filter(Point=point).all()
                for route in routes:
                    route.Route.LastUpdate = int(round(time.time() * 1000))
                    route.Route.save()


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

        route_list = get_allRoutes()


        tparams = {
            'title': 'Route',
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
                route.LastUpdate = int(round(time.time() * 1000))
                route.save()


        elif 'edit' in list(data.keys()):
            route_id = data.get('edit', None)
            route_obj = Route.objects.get(pk=route_id)

            form = RouteEditForm(instance=route_obj)

            tparams = {
                'title': 'Route',
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
                    route.LastUpdate = int(round(time.time() * 1000))
                    route.save()
                    generate_mapImage(route_id)

        if 'remove' in list(data.keys()):
            point_id = data.get('remove',None)

            if point_id:
                point = Point.objects.filter(pk=point_id).first()

                Route_contains_Point.objects.filter(Q(Route=route) and Q(Point=point)).first().delete()
                route.LastUpdate = int(round(time.time() * 1000))
                route.save()
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

            try:
                response = pn_client.publish(
                    interests=['spatialguide'],
                    publish_body={
                        'fcm': {
                            'notification': {
                                'title': 'New Route created!',
                                'body': 'Hey, the route "' + route.Name + '" has just been created!\nDownload it now!',
                                'tag': 'route',
                                'color': '#2196F3',
                                'click_action': 'OPEN_APP'
                            },
                        },
                    },
                )
            except:
                print('Failed to send notification of route created.')

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
        point_list = get_allPoints()

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

        print(request.data)

        image_name = str(request.FILES['Image']).split('.')
        image_name_type = image_name[len(image_name) - 1]

        image_name_type = test_imageType(image_name_type)

        sound_name = str(request.FILES['Sound']).split('.')
        sound_name_type = sound_name[len(sound_name)-1] == 'wav'



        if form.is_valid() and sound_name_type and image_name_type:
            point = form.save()
            point.Image = DRIVE_BASE_URL+request_filesaver(request.FILES['Image'])
            point.Sound = request_filesaver(request.FILES['Sound'])
            point.save()

        point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'point_array': json.dumps(point_list),
            'form_t': PointForm()
        }

        return render(request, 'form_point.html', tparams)

def test_imageType(image_type):
    return image_type == 'png' or image_type == 'jpg' or image_type == 'jpeg'

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

        if user and point:
            user_att = User_Attributes.objects.get(User_id=user)

            visited_point = user_att.Visited_points.filter(Point_id=point)
            print(visited_point)
            if not visited_point.exists():
                new_point = Point_Visited(Point_id=point)
                new_point.save()

                user_att.Visited_points.add(new_point)

            return Response(error, status=status.HTTP_200_OK)
        else:
            error={'error':'Wrong Parameter.'}
            return Response(error,status=status.HTTP_400_BAD_REQUEST)

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
                subject = 'SpatialGuide Confirmation Email'
                message = 'Thank you %s for registering in the SpatialGuide App' % user.username
                from_email = settings.EMAIL_HOST_USER
                to_list = [user.email, settings.EMAIL_HOST_USER]
                send_mail(subject, message, from_email, to_list)

                return redirect('show_routes')
            else:
                user = User.objects.filter(username=new_data['username']).first()
                user_att = User_Attributes(User_id=user)
                user_att.save()
                return Response(serializer.errors, status=status.HTTP_200_OK)

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
                if not user:
                    return render(request, 'login.html', {'error': 'Credencials are Invalid!'})
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

# userinfo/ or userinfo/( id )
class UserInfo(APIView):
    permission_classes = [IsAuthenticated]

    def get(self,request,user_id=None):
        
        if user_id:
            user = User.objects.filter(pk=user_id).first()
        else:
            user = User.objects.filter(username=str(request.user)).first()

        if not user:
            return Response({'error':'User doesn\'t Exist!'},status=status.HTTP_400_BAD_REQUEST)

        user_att = User_Attributes.objects.filter(User_id__id=user.id).first()

        user_serializer = UserSerializer(user).data
        user_att_serializer = UserAttrSerializer(user_att).data

        full_user = user_serializer
        for key in list(user_att_serializer.keys()):
            full_user[key]=user_att_serializer[key]

        points_visited = user_att.Visited_points.all()

        points_id = []
        for p in points_visited:
            tmp = {}
            tmp['id']=p.Point_id.id
            tmp['Visit_Data']=p.Visit_Date
            points_id.append(tmp)

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

# changeemail/
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

# changeimage/
class ChangeUserImage(APIView):
    permission_classes = [IsAuthenticated]

    def post(self,request):
        files = request.FILES
        data = request.data

        user = User.objects.filter(username=str(request.user)).first()

        if user:
            image_name = str(request.FILES['Image']).split('.')
            image_name_type = image_name[len(image_name) - 1]

            image_name_type = test_imageType(image_name_type)

            if image_name_type:
                user_att = User_Attributes.objects.get(User_id=user)

                if user_att:
                    user_att.Image =  DRIVE_BASE_URL+request_filesaver(files['Image'])
                    user_att.save()

                    return Response(status=status.HTTP_200_OK)
            else:
                return Response({'Image': "Image Format is not Valid."}, status = status.HTTP_400_BAD_REQUEST)

        return Response({'User': "User Doesn't Exist."}, status=status.HTTP_400_BAD_REQUEST)

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

# suggest/
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

# {
#     "latitude":"20",
#     "longitude" : "20",
#     "comment": "funciona caralho"
# }

class UserSuggestionsAdminView(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        user_suggestion = get_Suggestions()

        print(user_suggestion)

        tparams = {
            'title': 'User Suggestion',
            'route_list': user_suggestion
        }

        return render(request, 'suggestion.html', tparams)

# comment/
class UserCommentsView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, point_id=None):
        if point_id:
            point = Point.objects.get(pk=point_id)

            if point:
                comments = User_Comments.objects.filter(Point__id=point_id).all()


                serializer = UserCommentsSerializer(comments, many=True)

                response = []
                for data in serializer.data:
                    tmp = dict(data)

                    user = User.objects.filter(pk=tmp['User']).first()
                    user_att = User_Attributes.objects.filter(User_id=user).first()

                    tmp['User'] = user.username
                    tmp['Image'] = user_att.Image
                    response.append(tmp)


                return Response(response)

            else:
                return Response(status=status.HTTP_400_BAD_REQUEST)
        return Response(status=status.HTTP_400_BAD_REQUEST)

    def post(self, request):
        data = request.data
        user_post = request.user
        point = data.get('point', None)
        comment = data.get('comment', None)

        point = Point.objects.get(pk=point)
        user_post = User.objects.get(username=user_post)

        if not user_post or not point or not comment:
            return Response(status=status.HTTP_400_BAD_REQUEST)

        comment = User_Comments(User=user_post, Point=point, Comment=comment)
        comment.save()

        return Response(status=status.HTTP_200_OK)

class UserCommentsAdminView(APIView):
    permission_classes = [IsAuthenticated]


    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        user_comments = get_Comments()

        tparams = {
            'title': 'User Comments',
            'comment_list': user_comments,

        }

        return render(request, 'comments.html', tparams)

    def post(self,request):
        if not request.user.is_staff:
            return redirect('login')

        data = request.data
        print(data)

        if 'removeComment' in list(data.keys()):
            comment_id = data['removeComment']

            comment = User_Comments.objects.get(pk=comment_id)

            if comment:
                comment.delete()

        return self.get(request)

class UserPageAdminView(APIView):
    permission_classes = [AllowAny]

    def get(self,request):
        if not request.user.is_staff:
            return redirect('login')
        user_info = get_UserInfo()

        tparams = {
            'title': 'User Info',
            'user_info': user_info,
        }

        return render(request, 'UserPage.html', tparams)
