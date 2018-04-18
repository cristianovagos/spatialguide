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
from django.conf.urls.static import static

from django.conf import settings
from django.contrib import admin, auth

from rest_framework.urlpatterns import format_suffix_patterns
from app import views,rest

urlpatterns = [
    #url(r'^admin/', admin.site.urls),

    url(r'^$', views.show_routes, name='home'),
    url(r'^show_routes/$', views.show_routes, name='show_routes'),
    url(r'^display_route/((?P<route_id>\d+))/$', views.show_route , name='display_route'),
    url(r'^show_points/$', views.show_points, name='show_points'),

    url(r'^add_route/$', rest.addRoute.as_view(), name='add_route'),
    url(r'^add_point/$', rest.addPoint.as_view(), name='add_point'),

    ############ Rest ####################

    url(r'^route/((?P<route_id>\d+))/$', rest.RouteList.as_view()),
    url(r'^route/$', rest.RouteList.as_view(),),
    url(r'^route_points/((?P<route_id>\d+))/$', rest.Route_PointList.as_view()),

    url(r'^point/$', rest.PointList.as_view()),

    url(r'^heatmap/$', rest.HeatMap.as_view(),name='heatmap'),

    url(r'^register/$', rest.UserCreateView.as_view(), name='register'),
    url(r'^login/$', rest.UserLoginView.as_view(), name='login'),
    url(r'^logout/$', rest.UserLogoutView.as_view(), name='logout')

]
if settings.DEBUG:
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_URL)
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

urlpatterns = format_suffix_patterns(urlpatterns)