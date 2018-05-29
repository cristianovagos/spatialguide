import os

from .serializer import *
from django.forms.models import model_to_dict
from django.conf import settings
from .googleDrive import googledrive


CREDENTIAL_PATH=os.path.join(settings.BASE_DIR,'app/googleDrive/google-drive-credencials.json')
API_KEY= 'AIzaSyDeglQrbGdL6r49ToFw2Ft1ugFJmma2oJM'

def get_allRoutes():
    routes = Route.objects.all()
    serializer = RouteTableSerializer(routes, many=True)
    tmp_list = serializer.data

    route_list = []
    for route in tmp_list:
        route_list.append(route)

    return route_list

def generate_mapImage(route_id):
    points = get_route_points(route_id)
    url = 'https://maps.googleapis.com/maps/api/staticmap?size=400x400&maptype=roadmap'

    for p in points:
        latitude = str(p['Latitude'])
        longitude = str(p['Longitude'])
        url+='&markers=color:red%7C'+latitude+','+longitude

    url+= '&key='+API_KEY

    route = Route.objects.get(pk=route_id)
    route.Map_image=url
    route.save()

def get_route(route_id):
    route = Route.objects.get(pk=route_id)

    route = model_to_dict(route)
    return route

def get_route_points(route_id):
    points = Point.objects.filter(route_contains_point__Route_id=route_id)
    serializer = PointSerializer(points, many=True)
    points = serializer.data

    return points

def get_ExcludedPoints(route_id):
    points = Point.objects.all().exclude(route_contains_point__Route_id=route_id)
    serializer = PointSerializer(points, many=True)
    points = serializer.data

    return points

def get_allPoints():
    points = Point.objects.all()
    serializer = PointTableSerializer(points, many=True)
    tmp_list = serializer.data

    point_list = []
    for point in tmp_list:
        point_list.append(point)

    return point_list

def get_heatPoints():
    heat_points = Heat_Point.objects.all()
    serializer = HeatPointSerializer(heat_points, many=True)


    point_list = []
    for p in serializer.data:
        tmp={
            'Latitude':p['Latitude'],
            'Longitude': p['Longitude']
        }
        point_list.append(tmp)


    return point_list

def get_Suggestions():
   suggestion = User_Suggestions.objects.all()
   suggestion = UserSuggestionsSerializer(suggestion,many=True).data

   suggestion_list=[]
   for s in suggestion:
       suggestion_list.append(s)

   return suggestion_list

def get_Comments():
   comment = User_Comments.objects.all()
   comment = UserCommentsSerializer(comment,many=True)
   tmp_list = comment.data

   comment_list=[]
   for s in tmp_list:
       comment_list.append(s)

   return comment_list

def get_UserInfo():
    user = User_Attributes.objects.all()
    user = UserAttrSerializer()

################  Google Drive  ################

def save_media(name,path):
    connection=googledrive.GoogleDriveConnector(CREDENTIAL_PATH)

    file_id=connection.upload_file(name,path,'1V6lrBO3J99SkKxPr20Cqiz51ddPHN7bu')

    return file_id


