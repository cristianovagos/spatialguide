from django.shortcuts import render
from django.core.files.storage import default_storage
from django.core.files.base import ContentFile
from django.http import HttpRequest
from django.http import HttpResponse
from datetime import datetime
from .utils import *
from .models import *
from .rest import *


import json

def show_routes(request):
    assert isinstance(request, HttpRequest)

    tab_names,route_list = get_allRoutes()

    tparams = {
        'title': 'Route',
        'tab_names': tab_names,
        'route_list': route_list,
        'add_btn': 'add_route',

    }

    return render(request, 'tables.html', tparams)


def show_route(request,route_id):
    assert isinstance(request, HttpRequest)

    points = get_route_points(route_id)
    route = get_route(route_id)

    tparams = {
        'title': 'Route',
        'route': route,
        'point_array': json.dumps(points)
    }

    return render(request, 'route.html', tparams)



def add_route(request):
    assert isinstance(request, HttpRequest)

    if request.method == 'POST':

        form = RouteForm(request.POST, request.FILES)

        if form.is_valid():
            data = request.FILES['Image']

            path = default_storage.save(str(data), ContentFile(data.read()))
            tmp_file_path = os.path.join(settings.MEDIA_ROOT, path)
            img_type = str(data).split('.')[1]

            route = form.save()
            route.Image = save_image(str(data),tmp_file_path,img_type)
            route.save()

    tparams = {
        'title': 'Route',
        'form_t': RouteForm()
    }

    return render(request, 'form_route.html', tparams)


def show_points(request):
    assert isinstance(request, HttpRequest)

    tab_names,point_list = get_allPoints()

    tparams = {
        'title': 'Point',
        'tab_names': tab_names,
        'route_list': point_list,
        'add_btn': 'add_point'

    }

    return render(request, 'tables.html', tparams)

def add_point(request):
    assert isinstance(request, HttpRequest)

    if request.method == 'POST':
        # create a form instance and populate it with data from the request:
        form = PointForm(request.POST,request.FILES)
        if form.is_valid():
            data = request.FILES['Image']

            path = default_storage.save(str(data), ContentFile(data.read()))
            tmp_file_path = os.path.join(settings.MEDIA_ROOT, path)
            img_type = str(data).split('.')[1]

            point = form.save()
            point.Image = save_image(str(data), tmp_file_path, img_type)
            point.save()

        return show_points(request)

    tab_names, point_list = get_allPoints()

    tparams = {
        'title': 'Point',
        'point_array': json.dumps(point_list),
        'form_t': PointForm()
    }

    return render(request, 'form_point.html', tparams)




# Create your views here.
def home(request):
    """Renders the home page."""
    assert isinstance(request, HttpRequest)
    tparams = {
        'title':'Home Page',
        'year':datetime.now().year,
    }
    return render(request, 'index.html', tparams)

def tables(request):
    """Renders the contact page."""
    assert isinstance(request, HttpRequest)

    return render(request, 'tables.html', {})

def charts(request):
    """Renders the contact page."""
    assert isinstance(request, HttpRequest)

    return render(request, 'charts.html', {})

def cards(request):
    """Renders the contact page."""
    assert isinstance(request, HttpRequest)

    return render(request, 'cards.html', {})

def register(request):
    """Renders the contact page."""
    assert isinstance(request, HttpRequest)

    return render(request, 'register.html', {})


