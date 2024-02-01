# Remote Image Viewer
An image viewer for the WiC64 ( https://wic64.net/web/ )

Download of the C64 application: https://github.com/EgonOlsen71/ImageViewer/blob/main/basic/build/imageviewer.d64

To simply use the C64 application, you don't need to host and run the server yourself. The application will use an existing server if you don't modify it.

The application is also part of the WiC64 portal.



This project consists of two parts:

* an application that runs on the C64
* a server application that handles the image processing

The C64 application requires a WiC64 to access the internet, the server application need Java 11+ and a working configuration for the Google custom search API.

The C64 application can take either an image URL, a web page URL or some search string. Depending on the input, it will let the user choose between up to 22 images. The images will be downloaded and processed by the server application and then transferred to and displayed by the C64 in multicolor/Koala painter format.


You can save the images in Koala Painter format as well. There's also a localviewer.prg application included that doesn't require a WiC64 to work. It's sole purpose is to display the saved Koala Painter images.



To build the server, you need the Petsciiator project as well: https://github.com/EgonOlsen71/petsciiator

For the Google image search to work, the server expects the required information to access the API in /webdata/imageviewer/apikey.ini with a "cx" and a "key" entry just like the REST-call needs them. 

To build the C64 application, you need to have MOSpeed installed: https://github.com/EgonOlsen71/basicv2/tree/master/dist