"""webproj URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.11/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url
from django.contrib import admin, auth

from rest_framework.urlpatterns import format_suffix_patterns
from app import views,rest

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^$', views.home, name='home'),
    url(r'^about', views.about, name='about'),
    url(r'^login/$', auth.login, name='login'),
    url(r'^tables$', views.tables, name='tables'),
    url(r'^charts', views.charts, name='charts'),
    url(r'^register', views.register, name='register'),
    url(r'^cards', views.cards, name='cards'),


    url(r'^show_routes/$', views.show_routes, name='show_routes'),
    url(r'^show_points/$', views.show_points, name='show_points'),

    url(r'^route/((?P<route_id>\d+))/$', rest.RouteList.as_view()),
    url(r'^route/$', rest.RouteList.as_view()),
    url(r'^add_route/$', views.add_route, name='add_route'),

    url(r'^point/$', rest.PointList.as_view()),

]

urlpatterns = format_suffix_patterns(urlpatterns)