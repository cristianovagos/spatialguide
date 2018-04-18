from __future__ import print_function

import httplib2
import os
import io


from apiclient.http import MediaFileUpload, MediaIoBaseDownload
from apiclient import discovery
from oauth2client import client
from oauth2client import tools
from oauth2client.file import Storage

try:
    # import argparse
    # flags = argparse.ArgumentParser(parents=[tools.argparser]).parse_args()
    flags = tools.argparser.parse_args([])
except ImportError:
    flags = None

class GoogleDriveConnector():

    def __init__(self, CREDENTIAL_PATH):
        self.CREDENTIAL_PATH = CREDENTIAL_PATH

        self.credencials=None
        self.drive_service=None
        self.connect()

    def connect(self):
        self.credencials = self.getCredentials()
        http = self.credencials.authorize(httplib2.Http())
        self.drive_service = discovery.build('drive', 'v3', http=http)

    def getCredentials(self):
        store = Storage(self.CREDENTIAL_PATH)
        credentials = store.get()

        return credentials
    
    def listFiles(self, size):
        results = self.drive_service.files().list(
            pageSize=size,fields="nextPageToken, files(id, name)").execute()
        items = results.get('files', [])
        if not items:
            print('No files found.')
        else:
            print('Files:')
            for item in items:
                print(item)
                print('\t{0} ({1})'.format(item['name'], item['id']))

    def upload_file(self,filename,filepath,folder=None):
        file_metadata = {'name': filename}
        if folder:
            file_metadata['parents'] = [folder]

        media = MediaFileUpload(filepath, mimetype='application/octet-stream', resumable=True)
        request = self.drive_service.files().create(body=file_metadata, media_body=media)

        response = None
        while response is None:
            status, response = request.next_chunk()
            if status:
                print("Uploaded %d%%." % int(status.progress() * 100))

        print ('File ID: %s' % response.get('id'))
        return response.get('id')

    def download(self, file_id,file_path):
        request = self.drive_service.files().get_media(fileId=file_id)
        fh = io.BytesIO()
        downloader = MediaIoBaseDownload(fh, request)
        done = False
        while done is False:
            status, done = downloader.next_chunk()
            print ("Download %d%%." % int(status.progress() * 100))
        
        # write file to file_path
        with io.open(file_path,'wb') as f:
            fh.seek(0)
            f.write(fh.read())

    def createFolder(self, folder_name):
        file_metadata = {
            'name': folder_name,
            'mimeType': 'application/vnd.google-apps.folder'
        }
        file = self.drive_service.files().create(body=file_metadata,
                                            fields='id').execute()
        print('Folder ID: %s' % file.get('id'))