from django.contrib import admin
from .models import *

admin.site.register(Route)
admin.site.register(Point)
admin.site.register(Route_contains_Point)