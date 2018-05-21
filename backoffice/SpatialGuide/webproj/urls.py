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
    # url(r'^admin/', admin.site.urls),

    url(r'^$', rest.ShowRoutes.as_view(), name='home'),
    url(r'^show_routes/$', rest.ShowRoutes.as_view(), name='show_routes'),
    url(r'^display_route/((?P<route_id>\d+))/$', rest.ShowRoute.as_view() , name='display_route'),
    url(r'^show_points/$', rest.ShowPoints.as_view(), name='show_points'),

    url(r'^add_route/$', rest.addRoute.as_view(), name='add_route'),
    url(r'^add_point/$', rest.addPoint.as_view(), name='add_point'),

    url(r'^route/((?P<route_id>\d+))/$', rest.RouteList.as_view()),
    url(r'^route/$', rest.RouteList.as_view(),),
    url(r'^route_points/((?P<route_id>\d+))/$', rest.Route_PointList.as_view()),

    url(r'^point/$', rest.PointList.as_view()),
    url(r'^point/((?P<point_id>\d+))/$', rest.PointList.as_view()),

    url(r'^heatmap/$', rest.HeatMap.as_view(),name='heatmap'),

    url(r'^register/$', rest.UserCreateView.as_view(), name='register'),
    url(r'^login/$', rest.UserLoginView.as_view(), name='login'),
    url(r'^logout/$', rest.UserLogoutView.as_view(), name='logout'),

    url(r'^userinfo/$', rest.UserInfo.as_view(), name='userinfo'),
    url(r'^useraddfavourite/$', rest.AddFavorite.as_view(), name='useraddfavourite'),
    url(r'^userremovefavourite/$', rest.RemoveFavourite.as_view(), name='userremovefavourite'),

    url(r'^visitpoint/$', rest.UserVisit.as_view(), name='visitpoint'),

    url(r'^changepass/$', rest.ChangePassword.as_view(), name='changepass'),
    url(r'^changeemail/$', rest.ChangeEmail.as_view(), name='changeemail'),
    url(r'^recoverpass/$', rest.RecoverPassword.as_view(), name='recoverpass'),


    url(r'^suggest/$', rest.UserSuggestionView.as_view()),
    url(r'^suggestions/$', rest.UserSuggestionsAdminView.as_view(), name='suggestions'),


    url(r'^comment/$', rest.UserCommentsView.as_view()),
    url(r'^comments/$', rest.UserCommentsAdminView.as_view(), name='comments'),
]
if settings.DEBUG:
    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_URL)
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

urlpatterns = format_suffix_patterns(urlpatterns)