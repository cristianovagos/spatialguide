import os

from .serializer import *
from django.forms.models import model_to_dict
from django.conf import settings
from .googleDrive import googledrive


CREDENTIAL_PATH=os.path.join(settings.BASE_DIR,'app/googleDrive/google-drive-credencials.json')

def get_allRoutes():
    routes = Route.objects.all()
    serializer = RouteSerializer(routes, many=True)
    tmp_list = serializer.data

    tab_names = []
    if len(tmp_list) > 0:
        tab_names = list(tmp_list[0].keys())

    route_list = []
    for route in tmp_list:
        route_list.append(route)

    return (tab_names,route_list)


def get_route(route_id):
    route = Route.objects.get(pk=route_id)

    route = model_to_dict(route)
    return route


def get_route_points(route_id):
    points = Point.objects.filter(route_contains_point__Route_id=route_id)
    serializer = PointSerializer(points, many=True)
    points = serializer.data

    return points


def get_allPoints():
    points = Point.objects.all()
    serializer = PointSerializer(points, many=True)
    tmp_list = serializer.data

    tab_names = []
    if len(tmp_list) > 0:
        tab_names = list(tmp_list[0].keys())

    point_list = []
    for point in tmp_list:
        point_list.append(point)

    return (tab_names,point_list)

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


################  Google Drive  ################

def save_media(name,path):
    connection=googledrive.GoogleDriveConnector(CREDENTIAL_PATH)

    file_id=connection.upload_file(name,path)

    return file_id


