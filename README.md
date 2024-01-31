# Remote Image Viewer
An image viewer for the WiC64

This project consists of two parts:

* an application that runs on the C64
* a server application that handles the image processing

The C64 application requires a WiC64 to access the internet, the server application need Java 11+ and a working configuration for the Google custom search API.

The C64 application can take either an image URL, a weg page URL or some search string. Depending on the input, it will let the user choose between up to 22 images.The images will be downloaded and processed by the server application and then transfered and displayed by the C64 in multicolor/Koala painter format.


You can save the images in Koala Painter format as well. There's also a localviewer.prg application included that doesn't require a WiC64 to work. It's sole purpose is to display the saved Koala Painter images.
