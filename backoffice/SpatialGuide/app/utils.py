from .serializer import *



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
