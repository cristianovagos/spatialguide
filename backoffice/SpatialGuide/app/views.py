from django.shortcuts import render,redirect
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from django.http import HttpRequest
from datetime import datetime

from .server_utils import *
from .rest import *

import json

def show_routes(request):
    assert isinstance(request, HttpRequest)
    if request.user.is_superuser:
        tab_names,route_list = get_allRoutes()

        print(tab_names)
        print(route_list)
        tparams = {
            'title': 'Route',
            'tab_names': tab_names,
            'route_list': route_list,
            'add_btn': 'add_route'

        }

        return render(request, 'tables.html', tparams)
    else:
        return redirect('login')

def show_points(request):
    assert isinstance(request, HttpRequest)

    if request.user.is_superuser:
        tab_names,point_list = get_allPoints()

        tparams = {
            'title': 'Point',
            'tab_names': tab_names,
            'route_list': point_list,
            'add_btn': 'add_point'

        }
        return render(request, 'tables.html', tparams)

    else:
        return redirect('login')
