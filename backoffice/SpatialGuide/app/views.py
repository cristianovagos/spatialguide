from django.shortcuts import render
from django.http import HttpRequest
from django.http import HttpResponse
from datetime import datetime
from .utils import *


def show_routes(request):
    assert isinstance(request, HttpRequest)

    ######################################

    if request.method == 'POST':
        # create a form instance and populate it with data from the request:
        form = RouteForm(request.POST)
        # check whether it's valid:
        if form.is_valid():
            # process the data in form.cleaned_data as required
            # ...
            # redirect to a new URL:
            return HttpResponseRedirect('/thanks/')

    ######################################

    tab_names,route_list = get_allRoutes()

    tparams = {
        'title': 'Route',
        'tab_names': tab_names,
        'route_list': route_list
    }

    return render(request, 'tables.html', tparams)

def add_route(request):
    assert isinstance(request, HttpRequest)

    tparams = {
        'type': 'Route',
    }

    return render(request, 'form.html', tparams)



def show_points(request):
    assert isinstance(request, HttpRequest)

    tab_names,point_list = get_allPoints()

    tparams = {
        'title': 'Point',
        'tab_names': tab_names,
        'route_list': point_list
    }

    return render(request, 'tables.html', tparams)


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


def contact(request):
    """Renders the contact page."""
    assert isinstance(request, HttpRequest)
    tparams = {
        'title':'Contact',
        'message':'Your contact page.',
        'year':datetime.now().year,
    }
    return render(request, 'contact.html', tparams)


def about(request):
    assert isinstance(request, HttpRequest)
    tparams = {
        'title': 'About',
        'message': 'Your application description page.',
        'year': datetime.now().year,
    }
    return render(
        request,
        'about.html',
        {
            'title': 'About',
            'message': 'Your application description page.',
            'year': datetime.now().year,
        }
    )

