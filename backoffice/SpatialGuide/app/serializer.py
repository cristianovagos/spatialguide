from rest_framework.serializers import (
                                        CharField,
                                        ModelSerializer,
                                        EmailField,
                                        ValidationError
                                )
from django.contrib.auth import get_user_model, authenticate, login
from django.db.models import Q

from .models import *

class RouteSerializer(ModelSerializer):

    class Meta:
        model = Route
        exclude = ['Image','Map_image']

class PointSerializer(ModelSerializer):

    class Meta:
        model = Point
        exclude = ['Image','Sound']

class HeatPointSerializer(ModelSerializer):

    class Meta:
        model = Heat_Point
        fields = '__all__'

class UserAttrSerializer(ModelSerializer):

    class Meta:
        model = User_Attributes
        fields = ['Image','Favorite_routes','Favorite_points']

User = get_user_model()

class UserSerializer(ModelSerializer):

    class Meta:
        model = User
        fields = ['username','first_name','last_name','email','last_login']

class UserCreateSerializer(ModelSerializer):
    password2 = CharField()
    email = EmailField(label='Email')
    email2 = EmailField(label='Confirm Email')

    class Meta:
        model = User
        fields = [
            'username',
            'password',
            'password2',
            'email',
            'email2',
            'first_name',
            'last_name'
        ]
        extra_kwargs = {'password':
                            {"write_only":True},
                        'password2':
                            {"write_only": True}
                        }

    def validate_username(self, value):
        username = value

        user_queryset = User.objects.filter(username=username)

        if user_queryset.exists():
            raise ValidationError('Username already exists')

        return value

    def validate_password(self,value):
        data = self.get_initial()

        password = value
        password2 = data.get('password2')

        if password != password2:
            raise ValidationError('Passwords do not match!')
        return password

    def validate_email(self,value):
        data = self.get_initial()
        email1 = value
        email2 = data.get('email2')

        user_queryset = User.objects.filter(email=email1)

        if user_queryset.exists():
            raise ValidationError('Email already exists')
        elif email1 != email2:
            raise ValidationError('Emails must Match')

        return value

    def create(self, validated_data):
        username = validated_data['username']
        password = validated_data['password']
        email = validated_data['email']

        user_obj = User(
                username = username,
                email = email
        )

        if 'first_name' in list(validated_data.keys()):
            user_obj.first_name = validated_data['first_name']
        if 'last_name' in list(validated_data.keys()):
            user_obj.last_name = validated_data['last_name']

        user_obj.set_password(password)
        user_obj.save()

        user_att = User_Attributes(User_id=user_obj)
        user_att.save()

        return validated_data


class UserLoginSerializer(ModelSerializer):
    username = CharField(required=False,allow_blank=True)
    email = EmailField(required=False,allow_blank=True)

    class Meta:
        model = User
        fields = [
            'username',
            'email',
            'password'
        ]
        extra_kwargs = {'password':
                            {"write_only":True}
                    }

    def validate(self,data):
        username = data.get('username', None)
        email = data.get('email', None )

        if not username and not email:
            raise ValidationError('A username or email is required to login.')

        user = User.objects.filter(Q(username=username) | Q(email=email) | Q(username=email) | Q(email=username)).distinct()
        user_object = None

        if user.exists() and user.count() == 1:
            user_object = user.first()
            if not user_object.is_active:
                return {}
        else:
            return {}

        return data
