from django.shortcuts import render
from django.http import HttpRequest
from django.http import HttpResponse
from datetime import datetime



#########################################


from django.shortcuts import get_object_or_404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .models import *
from .serializer import *


# route/
class RouteList(APIView):

    def get(self,request,route_id=None):
        if route_id:
            routes = Route.objects.filter(id=route_id)
        else:
            routes = Route.objects.all()

        print(route_id)
        serializer = RouteSerializer(routes,many=True)
        return Response(serializer.data)

    def post(self,request):
        pass

# point/
class PointList(APIView):

    def get(self,request):
        points = Point.objects.all()
        serializer = PointSerializer(points,many=True)
        return Response(serializer.data)

    def post(self,request):
        pass

##########################################


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

